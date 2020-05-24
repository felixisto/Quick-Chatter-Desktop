/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.connectors;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import org.jetbrains.annotations.NotNull;
import quickchatter.network.bluetooth.basic.BEClient;
import quickchatter.network.bluetooth.basic.BESocket;
import quickchatter.network.bluetooth.bluecove.BDAdapter;
import quickchatter.network.bluetooth.bluecove.BDClientDevice;
import quickchatter.network.bluetooth.bluecove.BDSocket;
import quickchatter.utilities.Callback;
import quickchatter.utilities.Logger;

/// Establishes connection between the local device and the specified client.
public class BDConnection {
    public static final int RETRY_COUNT = 10;
    
    private final Object lock = new Object();
    
    private final @NotNull BDAdapter _adapter;
    private final @NotNull BEClient _client;
    private final @NotNull UUID _uuid;
    
    private final int _retryCount;
    private @NotNull final AtomicInteger _currentTryCount = new AtomicInteger(0);
    
    private final @NotNull AtomicBoolean _isActive = new AtomicBoolean(false);
    private final @NotNull AtomicBoolean _foundService = new AtomicBoolean(false);
    private int _currentSearchID;
    
    public BDConnection(@NotNull BDAdapter adapter, @NotNull BEClient client, @NotNull UUID uuid, int retryCount) {
        _adapter = adapter;
        _client = client;
        _uuid = uuid;
        _retryCount = retryCount;
    }
    
    public BDConnection(@NotNull BDAdapter adapter, @NotNull BEClient client, @NotNull UUID uuid) {
        this(adapter, client, uuid, RETRY_COUNT);
    }
    
    public void start(@NotNull Callback<BESocket> success, @NotNull Callback<Exception> failure) {
        if (!_adapter.isAvailable()) {
            failure.perform(new Exception("Bluetooth not available"));
            return;
        }
        
        if (_isActive.getAndSet(true)) {
            failure.perform(new Exception("Already started"));
            return;
        }
        
        _currentTryCount.set(_retryCount);
        
        _foundService.set(false);
        
        Logger.message(this, "Trying to establish a connection with client device");
        
        connect(success, failure);
    }
    
    public void stop() {
        if (!_adapter.isAvailable()) {
            return;
        }
        
        synchronized (lock) {
            if (!_isActive.getAndSet(false)) {
                return;
            }
            
            LocalDevice localDevice = _adapter.getAdapter();
            localDevice.getDiscoveryAgent().cancelServiceSearch(_currentSearchID);
            
            _currentSearchID = -1;
        }
    }
    
    private void connect(@NotNull Callback<BESocket> success, @NotNull Callback<Exception> failure) {
       _currentTryCount.decrementAndGet();
        
       tryConnect(success, new Callback<Exception>() {
           @Override
           public void perform(Exception argument) {
               Logger.warning(this, "Failed to establish a connection, error: " + argument);
               
               // Retry
               if (_currentTryCount.get() > 0) {
                   Logger.message(this, "Retrying establish connection with client device");
                   connect(success, failure);
               } else {
                   Logger.message(this, "Retry exausted, stopping and finishing with error");
                   stop();
                   failure.perform(argument);
               }
           }
       });
    }
    
    private void tryConnect(@NotNull Callback<BESocket> success, @NotNull Callback<Exception> failure) {
        BDClientDevice clientDevice;
        
        LocalDevice localDevice = _adapter.getAdapter();
        
        if (localDevice == null) {
            failure.perform(new Exception("Bluetooth adapter is unavailable"));
            return;
        }
        
        if (_client.getDevice() instanceof BDClientDevice) {
            clientDevice = ((BDClientDevice)_client.getDevice());
        } else {
            failure.perform(new Exception("Unsupported client device"));
            return;
        }
        
        UUID[] uuidSet = new UUID[1];
        uuidSet[0] = _uuid;
        
        final BDConnection self = this;
        
        DiscoveryListener listener = new DiscoveryListener() {
            @Override
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                
            }

            @Override
            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                synchronized (lock) {
                    if (!_isActive.get()) {
                        return;
                    }
                    
                    if (servRecord == null || servRecord.length == 0) {
                        failure.perform(new Exception("Unsupported client device"));
                        return;
                    }
                    
                    _foundService.set(true);
                }
                
                String url = servRecord[0].getConnectionURL(ServiceRecord.AUTHENTICATE_ENCRYPT, false);
                
                startConnection(url, success, failure);
            }

            @Override
            public void serviceSearchCompleted(int transID, int respCode) {
                synchronized (lock) {
                    _isActive.set(false);
                    
                    if (_foundService.get()) {
                        return;
                    }
                    
                    endConnectionDueToUnsupportedServices(failure);
                }
            }

            @Override
            public void inquiryCompleted(int discType) {
                
            }
        };
        
        synchronized (lock) {
            try {
                _currentSearchID = localDevice.getDiscoveryAgent().searchServices(null, uuidSet, clientDevice.asRemoteDevice(), listener);
            } catch (Exception e) {
                failure.perform(e);
            }
        }
    }
    
    private void startConnection(@NotNull String url, @NotNull Callback<BESocket> success, @NotNull Callback<Exception> failure) {
        try {
            Connection connection = Connector.open(url);
            
            if (!(connection instanceof StreamConnection)) {
                throw new Exception("Unsupported client device");
            }
            
            StreamConnection streamConnection = (StreamConnection)connection;
            
            success.perform(new BDSocket(streamConnection));
        } catch (Exception e) {
            failure.perform(e);
        }
    }
    
    private void endConnectionDueToUnsupportedServices(@NotNull Callback<Exception> failure) {
        failure.perform(new Exception("Target device disconnected or does not support the required services"));
    }
}
