/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.connectors;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.bluetooth.UUID;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickchatter.network.bluetooth.basic.BEConnector;
import quickchatter.network.bluetooth.basic.BEError;
import quickchatter.network.bluetooth.basic.BESocket;
import quickchatter.network.bluetooth.bluecove.BCAdapter;
import quickchatter.network.bluetooth.bluecove.BCSocket;
import quickchatter.network.bluetooth.bluecove.BCUUID;
import quickchatter.utilities.Callback;
import quickchatter.utilities.Errors;
import quickchatter.utilities.Logger;
import quickchatter.utilities.LooperService;
import quickchatter.utilities.SimpleCallback;

public class BCServerConnector implements BEConnector.Server {
    private final @NotNull String _serverName;
    private final @NotNull BCAdapter _adapter;

    private final @NotNull AtomicBoolean _running = new AtomicBoolean(false);

    private final @NotNull AtomicReference<StreamConnectionNotifier> _serverSocket = new AtomicReference<>();

    public BCServerConnector(@NotNull String serverName, @NotNull BCAdapter adapter) {
        _serverName = serverName;
        _adapter = adapter;
    }

    // # Properties

    private @Nullable StreamConnectionNotifier getOpenedServerSocket() {
        return _serverSocket.get();
    }

    // # BEConnector.Server

    public @NotNull UUID getUUID() {
        return BCUUID.getGeneric();
    }

    @Override
    public void start(final @NotNull Callback<BESocket> success, final @NotNull Callback<Exception> failure) throws Exception, BEError {
        if (_running.getAndSet(true)) {
            return;
        }

        final BCServerConnector self = this;

        Logger.message(self, "Starting server...");

        SimpleCallback completion = new SimpleCallback() {
            @Override
            public void perform() {
                startServer(success, failure);
            }
        };

        LooperService.getShared().asyncInBackground(completion);
    }

    @Override
    public synchronized void stop() {
        if (!_running.get()) {
            return;
        }
        
        resetServerSocket(true);
        
        // Does not support fresh restart
    }

    // # Internals
    
    private void startServer(final @NotNull Callback<BESocket> success, final @NotNull Callback<Exception> failure) {
        StreamConnectionNotifier serverSocket = getOpenedServerSocket();
        
        try {
            if (serverSocket == null) {
                serverSocket = openServerSocket();
                _serverSocket.set(serverSocket);
            }
            
            Logger.message(this, "Opened server socket and listening for incoming connections...");
            
            StreamConnection connection = serverSocket.acceptAndOpen();
            
            if (connection == null) {
                Errors.throwTimeoutError("Timeout");
            }
            
            Logger.message(this, "Successfully paired with client!");
            
            resetServerSocket(false);
            
            success.perform(new BCSocket(connection, serverSocket));
        } catch (Exception e) {
            resetServerSocket(true);
            
            Logger.error(this, "Failed to open server socket, error: " + e);
            
            failure.perform(e);
        }
    }

    private @NotNull StreamConnectionNotifier openServerSocket() throws Exception {
        if (!_adapter.isAvailable()) {
            Errors.throwUnsupportedOperation("Bluetooth adapter is not available");
        }
        
        UUID uuid = getUUID();
        
        String connectionString = "btspp://localhost:" + uuid.toString() + ";name=quickchatter";
        
        Connection streamConnNotifier = Connector.open(connectionString);
        
        if (streamConnNotifier == null) {
            Errors.throwUnknownError("Unknown error");
        }
        
        if (!(streamConnNotifier instanceof StreamConnectionNotifier)) {
            streamConnNotifier.close();
            
            Errors.throwUnknownError("Unknown error");
        }
        
        return (StreamConnectionNotifier)streamConnNotifier;
    }
    
    private void resetServerSocket(boolean close) {
        try {
            if (close) {
                _serverSocket.get().close();
            }
            
            _serverSocket.set(null);
        } catch (Exception e) {
            
        }
    }
}
