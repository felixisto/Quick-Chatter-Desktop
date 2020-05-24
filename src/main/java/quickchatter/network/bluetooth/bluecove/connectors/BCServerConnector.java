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
        return BCUUID.get();
    }

    @Override
    public boolean isConnecting() {
        return _running.get() && getOpenedServerSocket() == null;
    }

    @Override
    public boolean isConnected() {
        return _running.get() && getOpenedServerSocket() != null;
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
                Logger.message(self, "Opening server socket...");

                StreamConnectionNotifier serverSocket = getOpenedServerSocket();

                try {
                    if (serverSocket == null) {
                        serverSocket = startServerSync();
                        _serverSocket.set(serverSocket);
                    }

                    Logger.message(self, "Server searching for clients...");
                    
                    StreamConnection connection = serverSocket.acceptAndOpen();
                    
                    if (connection == null) {
                        Errors.throwTimeoutError("Timeout");
                    }

                    Logger.message(self, "Successfully paired with client!");

                    success.perform(new BCSocket(connection));
                } catch (Exception e) {
                    cleanup(serverSocket);

                    Logger.error(self, "Failed to open server socket, error: " + e);

                    failure.perform(e);
                }
            }
        };

        LooperService.getShared().asyncInBackground(completion);
    }

    @Override
    public void stop() {
        StreamConnectionNotifier serverSocket = _serverSocket.getAndSet(null);
        
        cleanup(serverSocket);
    }

    // # Internals
    
    private void cleanup(@Nullable StreamConnectionNotifier serverSocket) {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (Exception e) {

            }
        }
    }

    private @NotNull StreamConnectionNotifier startServerSync() throws Exception {
        if (!_adapter.isAvailable()) {
            Errors.throwUnsupportedOperation("Bluetooth adapter is not available");
        }
        
        UUID uuid = getUUID();
        
        String connectionString = "btspp://localhost:" + uuid.toString() + ";name=quickchatter";
        
        Connection streamConnNotifier = Connector.open(connectionString);
        
        if (!(streamConnNotifier instanceof StreamConnectionNotifier)) {
            Errors.throwUnknownError("Unknown error");
        }
        
        return (StreamConnectionNotifier)streamConnNotifier;
    }
}
