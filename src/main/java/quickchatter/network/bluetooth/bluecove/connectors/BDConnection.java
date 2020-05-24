/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.connectors;

import java.util.concurrent.atomic.AtomicBoolean;
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
    
    private final @NotNull BDAdapter _adapter;
    private final @NotNull BEClient _client;
    private final @NotNull UUID _uuid;
    
    private final int _retryCount;
    private int _currentTryCount;
    
    private final @NotNull AtomicBoolean _isActive = new AtomicBoolean(false);
    
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
        if (_isActive.getAndSet(true)) {
            failure.perform(new Exception("Already started"));
            return;
        }
        
        Logger.message(this, "Trying to establish a connection with client device");
        
        connect(success, failure);
    }
    
    private void connect(@NotNull Callback<BESocket> success, @NotNull Callback<Exception> failure) {
       _currentTryCount = _retryCount;
       
       _currentTryCount -= 1;
        
       tryConnect(success, new Callback<Exception>() {
           @Override
           public void perform(Exception argument) {
               Logger.warning(this, "Failed to establish a connection, error: " + argument);
               
               // Retry
               if (_currentTryCount > 0) {
                   Logger.message(this, "Retrying establish connection with client device");
                   connect(success, failure);
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
        
        DiscoveryListener listener = new DiscoveryListener() {
            @Override
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                
            }

            @Override
            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                if (!_isActive.get()) {
                    return;
                }
                
                if (servRecord == null || servRecord.length == 0) {
                    failure.perform(new Exception("Unsupported client device"));
                    return;
                }
                
                String url = servRecord[0].getConnectionURL(ServiceRecord.AUTHENTICATE_ENCRYPT, false);
                
                startConnection(url, success, failure);
            }

            @Override
            public void serviceSearchCompleted(int transID, int respCode) {
                _isActive.set(false);
            }

            @Override
            public void inquiryCompleted(int discType) {
                
            }
        };
        
        try {
            localDevice.getDiscoveryAgent().searchServices(null, uuidSet, clientDevice.asRemoteDevice(), listener);
        } catch (Exception e) {
            failure.perform(e);
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
}
