/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.discovery;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickchatter.network.bluetooth.basic.BEClient;
import quickchatter.network.bluetooth.bluecove.BCAdapter;
import quickchatter.network.bluetooth.bluecove.BCClientDevice;
import quickchatter.network.bluetooth.service.BEService;
import quickchatter.network.bluetooth.service.BEServiceFilter;
import quickchatter.network.bluetooth.service.BEServiceObexTransfer;
import quickchatter.utilities.Callback;
import quickchatter.utilities.CollectionUtilities;
import quickchatter.utilities.Errors;
import quickchatter.utilities.Logger;
import quickchatter.utilities.LooperService;
import quickchatter.utilities.SimpleCallback;
import quickchatter.utilities.TimeValue;

// Scans the services of a particular bluetooth client.
public class BCServiceDiscovery implements DiscoveryListener {
    private final @NotNull Object lock = new Object();

    private final @NotNull BCServiceDiscovery self;

    private final @NotNull BCAdapter _adapter;
    private final @NotNull UUID _uuid;
    private final @NotNull List<BEServiceFilter> _filters;
    private final AtomicBoolean _isServiceScanRunning = new AtomicBoolean(false);

    private final @NotNull AtomicReference<Date> _lastScanDate = new AtomicReference<>(null);
    
    private final @NotNull ArrayList<BEService> _currentScanFoundServices = new ArrayList();

    private final @NotNull AtomicReference<BEClient> _targetScanClient = new AtomicReference<>(null);
    private @Nullable Callback<List<BEService>> _scanCompletion;
    private int _currentScanID = -1;
    
    public BCServiceDiscovery(@NotNull BCAdapter adapter, @NotNull UUID uuid, @NotNull List<BEServiceFilter> filters) {
        _adapter = adapter;
        _uuid = uuid;
        _filters = CollectionUtilities.copy(filters);
        self = this;
    }

    // # Properties

    public @Nullable Date getLastScanDate() {
        synchronized (lock) {
            return _lastScanDate.get();
        }
    }

    public @NotNull TimeValue getTimePassedSinceLastScan() {
        Date date = getLastScanDate();

        if (date == null) {
            return TimeValue.zero();
        }

        Date now = new Date();
        long ms = now.getTime() - date.getTime();

        return TimeValue.buildMS((int) ms);
    }
    
    // # Operations

    public boolean isRunning() {
        return _isServiceScanRunning.get();
    }

    public void scanServices(@NotNull BEClient client, @Nullable Callback<List<BEService>> completion) throws Exception {
        Logger.message(this, "Start.");
        
        if (!(client.getDevice() instanceof BCClientDevice)) {
            Errors.throwInvalidArgument("Invalid argument, a BCClientDevice is needed");
            return;
        }

        synchronized (lock) {
            if (isRunning()) {
                return;
            }

            _scanCompletion = completion;
            _targetScanClient.set(client);
            _currentScanFoundServices.clear();
            
            startServiceScanForDiscoveredDevice(getTargetClientAsRemoteDevice());
        }
    }

    public void stop() {
        Logger.message(this, "Stop.");

        synchronized (lock) {
            stopScan();
        }
    }

    // # Internals
    
    private void startServiceScanForDiscoveredDevice(@NotNull RemoteDevice device) throws Exception {
        if (!_adapter.isAvailable()) {
            Errors.throwUnsupportedOperation("Bluetooth adapter is not available");
        }
        
        Logger.message(this, "Start service scan.");
        
        _isServiceScanRunning.set(true);

        _lastScanDate.set(new Date());
        
        UUID[] uuids = new UUID[] { _uuid };
        
        _currentScanID = _adapter.getAdapter().getDiscoveryAgent().searchServices(null, uuids, device, this);
    }

    private void stopScan() {
        if (!_adapter.isAvailable()) {
            return;
        }
        
        _scanCompletion = null;
        _targetScanClient.set(null);

        try {
            if (_currentScanID != -1) {
                _adapter.getAdapter().getDiscoveryAgent().cancelServiceSearch(_currentScanID);
                _currentScanID = -1;
            }
            
            Logger.message(this, "Scan stopped.");
        } catch (Exception e) {
            Logger.warning(this, "Failed to stop scan, error: " + e.toString());
        }
    }

    private void onServiceFound(@NotNull ServiceRecord serviceRec) {
        BEService newService = null;
        
        for (BEServiceFilter filter : _filters) {
            DataElement element = serviceRec.getAttributeValue(filter.attributeID);
            
            if (element == null) {
                continue;
            }
            
            int serviceRecordType;
            
            if (filter.requiresAuthorization && filter.requiresEncryption) {
                serviceRecordType = ServiceRecord.AUTHENTICATE_ENCRYPT;
            } else if (filter.requiresAuthorization) {
                serviceRecordType = ServiceRecord.AUTHENTICATE_NOENCRYPT;
            } else {
                serviceRecordType = ServiceRecord.NOAUTHENTICATE_NOENCRYPT;
            }
            
            String url = serviceRec.getConnectionURL(serviceRecordType, filter.requiresMaster);
            
            if (url == null) {
                continue;
            }
            
            newService = new BEServiceObexTransfer(url);
        }
        
        if (newService == null) {
            return;
        }
        
        Logger.message(this, "Found new service " + newService.toString());
        
        synchronized (lock) {
            _currentScanFoundServices.add(newService);
        }
    }
    
    // # DiscoveryListener

    @Override
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        
    }

    @Override
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        for (ServiceRecord rec : servRecord) {
            self.onServiceFound(rec);
        }
    }

    @Override
    public void serviceSearchCompleted(int transID, int respCode) {
        finishScanAndPerformCompletion();
    }

    @Override
    public void inquiryCompleted(int discType) {
        
    }
    
    // # Internals
    
    private RemoteDevice getTargetClientAsRemoteDevice() {
        BCClientDevice device;
        
        if (!(_targetScanClient.get().getDevice() instanceof BCClientDevice)) {
            return null;
        }
        
        return ((BCClientDevice) (_targetScanClient.get().getDevice())).asRemoteDevice();
    }
    
    private void finishScanAndPerformCompletion() {
        Callback<List<BEService>> completion;
        List<BEService> foundServices;
        
        synchronized (lock) {
            completion = this._scanCompletion;
            foundServices = CollectionUtilities.copy(_currentScanFoundServices);
            _currentScanID = -1;
            stopScan();
        }
        
        if (completion == null) {
            return;
        }
        
        // Perform completion
        LooperService.getShared().asyncInBackground(new SimpleCallback() {
            @Override
            public void perform() {
                completion.perform(foundServices);
            }
        });
    }
}
