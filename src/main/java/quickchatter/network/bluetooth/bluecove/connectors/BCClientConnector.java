/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.connectors;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.bluetooth.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickchatter.network.bluetooth.basic.BEClient;
import quickchatter.network.bluetooth.basic.BEConnector;
import quickchatter.network.bluetooth.basic.BEError;
import quickchatter.network.bluetooth.basic.BESocket;
import quickchatter.network.bluetooth.bluecove.BCAdapter;
import quickchatter.network.bluetooth.bluecove.BCUUID;
import quickchatter.utilities.Callback;
import quickchatter.utilities.Errors;
import quickchatter.utilities.Logger;
import quickchatter.utilities.LooperService;
import quickchatter.utilities.SimpleCallback;
import quickchatter.utilities.TimeValue;

public class BCClientConnector implements BEConnector.Client {
    private final @NotNull BEClient _server;
    private final @NotNull BCAdapter _adapter;

    private final @NotNull AtomicBoolean _running = new AtomicBoolean(false);

    private final @NotNull AtomicReference<BESocket> _clientSocket = new AtomicReference<>();
    private final int _tryCountOriginal;
    private final @NotNull AtomicInteger _tryCount = new AtomicInteger();
    private final @NotNull TimeValue _retryDelay;

    public BCClientConnector(@NotNull BEClient server, @NotNull BCAdapter adapter, int tryCount, @NotNull TimeValue retryDelay) {
        _server = server;
        _adapter = adapter;
        _tryCountOriginal = tryCount;
        _tryCount.set(_tryCountOriginal);
        _retryDelay = retryDelay;
    }

    // # Properties

    private @Nullable BESocket getOpenedSocket() {
        return _clientSocket.get();
    }

    public boolean isTryExhausted() {
        return _tryCount.get() <= 0;
    }

    // # BEConnector.Client

    public @NotNull UUID getUUID() {
        return BCUUID.get();
    }

    @Override
    public @NotNull BEClient getServer() {
        return _server;
    }

    @Override
    public boolean isConnecting() {
        return _running.get() && getOpenedSocket() == null;
    }

    @Override
    public boolean isConnected() {
        return _running.get() && getOpenedSocket() != null;
    }

    @Override
    public void connect(@NotNull Callback<BESocket> success, @NotNull Callback<Exception> failure) throws Exception, BEError {
        if (_running.getAndSet(true)) {
            Errors.throwCannotStartTwice("Already running");
        }

        _tryCount.set(_tryCountOriginal);

        start(success, failure);
    }

    @Override
    public void terminate() {

    }

    // # Internals

    private void start(final @NotNull Callback<BESocket> success, final @NotNull Callback<Exception> failure) throws Exception, BEError {
        final BCClientConnector self = this;

        Logger.message(self, "Connecting to server...");

        SimpleCallback completion = new SimpleCallback() {
            @Override
            public void perform() {
                Logger.message(self, "Opening client socket...");
                
                BCConnection connection = new BCConnection(_adapter, _server, getUUID());
                
                connection.start(new Callback<BESocket>() {
                    @Override
                    public void perform(BESocket socket) {
                        Logger.message(self, "Successfully paired with server '" + _server.getName() + "'!");
                        
                        _clientSocket.set(socket);
                        
                        success.perform(socket);
                    }
                }, new Callback<Exception>() {
                    @Override
                    public void perform(Exception error) {
                        Logger.warning(self, "Failed to open client socket, error: " + error);
                        
                        retry(success, failure, error);
                    }
                });
            }
        };

        LooperService.getShared().asyncInBackground(completion);
    }

    private void retry(final @NotNull Callback<BESocket> success, final @NotNull Callback<Exception> failure, @NotNull Exception originalError) {
        if (isTryExhausted()) {
            Logger.error(this, "Timer is exhausted, ending connection.");
            completeFailure(failure, originalError);
            return;
        }

        _tryCount.decrementAndGet();
        
        LooperService.getShared().asyncInBackgroundAfterDelay(new SimpleCallback() {
            @Override
            public void perform() {
                try {
                    start(success, failure);
                } catch (Exception e) {
                    completeFailure(failure, e);
                }
            }
        }, _retryDelay);
    }

    private void completeFailure(final @NotNull Callback<Exception> failure, @NotNull Exception error) {
        Logger.error(this, "Failed to connect, error: " + error);

        _running.set(false);

        failure.perform(error);
    }
}

