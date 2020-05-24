/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.discovery;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickchatter.network.bluetooth.bluecove.BCAdapter;
import quickchatter.utilities.Callback;
import quickchatter.utilities.CollectionUtilities;
import quickchatter.utilities.Errors;
import quickchatter.utilities.Logger;
import quickchatter.utilities.LooperClient;
import quickchatter.utilities.LooperService;
import quickchatter.utilities.SafeMutableArray;
import quickchatter.utilities.TimeValue;

/// Searches for devices. The devices data is wiped when starting a new scan.
/// When searching, our device may not necessarily be visible to other devices, the visibility
/// must be broadcasted from a separate component.
public class BCDiscovery implements LooperClient, DiscoveryListener {
    private final @NotNull Object lock = new Object();

    private final @NotNull BCDiscovery self;

    private final @NotNull BCAdapter _adapter;
    private final AtomicBoolean _isInqueryScanRunning = new AtomicBoolean(false);

    private final @NotNull HashSet<RemoteDevice> _previousScanDevices = new HashSet<>();
    private final @NotNull AtomicReference<Date> _previousScanDate = new AtomicReference<>(null);

    private final @NotNull HashSet<RemoteDevice> _lastScanDevices = new HashSet<>();
    private final @NotNull AtomicReference<Date> _lastScanDate = new AtomicReference<>(null);

    private final @NotNull SafeMutableArray<Callback<Set<RemoteDevice>>> _scanCompletions = new SafeMutableArray<>();

    public BCDiscovery(@NotNull BCAdapter adapter) {
        _adapter = adapter;
        self = this;
    }

    // # Properties

    // Note: Discovery keeps track of all devices, that have ever been found, including lost devices
    public @NotNull Set<RemoteDevice> getFoundDevicesFromLastScan() {
        synchronized (lock) {
            return CollectionUtilities.copy(_lastScanDevices);
        }
    }

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

    public @NotNull Set<RemoteDevice> getFoundDevicesFromPreviousScan() {
        synchronized (lock) {
            return CollectionUtilities.copy(_previousScanDevices);
        }
    }

    public @Nullable Date getPreviousScanDate() {
        synchronized (lock) {
            return _previousScanDate.get();
        }
    }

    // # Operations

    public boolean isRunning() {
        return _isInqueryScanRunning.get();
    }

    public void runDiscoveryScan(@Nullable Callback<Set<RemoteDevice>> completion) throws Exception {
        Logger.message(this, "Start.");

        synchronized (lock) {
            if (isRunning()) {
                return;
            }

            LooperService.getShared().subscribe(this);

            startDiscovery();

            if (completion != null) {
                _scanCompletions.add(completion);
            }
        }
    }

    public void stop() {
        Logger.message(this, "Stop.");

        synchronized (lock) {
            stopDiscovery();
        }
    }

    // # LooperClient

    @Override
    public void loop() {
        Set<RemoteDevice> devices = getFoundDevicesFromLastScan();

        synchronized (lock) {
            if (!isRunning()) {
                LooperService.getShared().unsubscribe(this);

                List<Callback<Set<RemoteDevice>>> callbacks = _scanCompletions.copyData();

                _scanCompletions.removeAll();

                for (Callback<Set<RemoteDevice>> callback: callbacks) {
                    callback.perform(devices);
                }
            }
        }
    }

    // # Internals

    private void startDiscovery() throws Exception {
        if (!_adapter.isAvailable()) {
            Errors.throwUnsupportedOperation("Bluetooth adapter is not available");
        }
        
        Logger.message(this, "Start discovery.");
        
        _isInqueryScanRunning.set(true);

        _previousScanDevices.clear();
        _previousScanDevices.addAll(_lastScanDevices);
        _lastScanDevices.clear();

        _previousScanDate.set(_lastScanDate.get());
        _lastScanDate.set(new Date());

        _adapter.getAdapter().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, this);
    }

    private void stopDiscovery() {
        if (!_adapter.isAvailable()) {
            return;
        }

        try {
            _adapter.getAdapter().getDiscoveryAgent().cancelInquiry(this);
            Logger.message(this, "Discovery stopped.");
        } catch (Exception e) {
            Logger.warning(this, "Failed to stop discovery, error: " + e.toString());
        }
    }

    private void onDeviceFound(@NotNull RemoteDevice device) {
        String name = "Unknown";
        
        try {
            name = device.getFriendlyName(false);
        } catch (Exception e) {
            
        }
        
        Logger.message(this, "Device found! Name: " + name);

        synchronized (lock) {
            _lastScanDevices.remove(device);
            _lastScanDevices.add(device);
        }
    }
    
    // # DiscoveryListener

    @Override
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        self.onDeviceFound(btDevice);
    }

    @Override
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        
    }

    @Override
    public void serviceSearchCompleted(int transID, int respCode) {
        
    }

    @Override
    public void inquiryCompleted(int discType) {
        _isInqueryScanRunning.set(false);
    }
}

