/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.presenter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickchatter.mvp.MVP;
import quickchatter.network.bluetooth.basic.BEClient;
import quickchatter.network.bluetooth.basic.BEConnector;
import quickchatter.network.bluetooth.basic.BESocket;
import quickchatter.network.bluetooth.bluecove.transmission.BDTransmitter;
import quickchatter.presenter.worker.ConnectingPresenter;
import quickchatter.utilities.Callback;
import quickchatter.utilities.Errors;
import quickchatter.utilities.Logger;
import quickchatter.utilities.LooperService;
import quickchatter.utilities.SimpleCallback;

public class ConnectingServerPresenter implements BasePresenter.Connecting {
    private @NotNull BasePresenterDelegate.Connecting _delegate;
    
    private final @NotNull BEConnector.Server _connector;
    private final @NotNull BEClient _client;
    private @Nullable BDTransmitter _transmitter;
    
    public ConnectingServerPresenter(@NotNull BEClient client, @NotNull BEConnector.Server connector) {
        _connector = connector;
        _client = client;
    }

    @Override
    public MVP.View getView() {
        return _delegate;
    }
    
    @Override
    public void start(BasePresenterDelegate.Connecting delegate) throws Exception {
        if (_delegate != null) {
            Errors.throwCannotStartTwice("Presenter already started!");
        }
        
        Logger.message(this, "Start.");
        
        _delegate = delegate;
        
        final Callback<BESocket> success = new Callback<BESocket>() {
            @Override
            public void perform(BESocket argument) {
                Logger.message(this, "SUCCESS!");

                BDTransmitter transmitter = startServer(argument);

                delegate.updateClientInfo("Paired!");
                delegate.navigateToChatScreen(_client, transmitter, transmitter);
            }
        };

        final Callback<Exception> failure = new Callback<Exception>() {
            @Override
            public void perform(Exception argument) {
                Logger.error(this, "FAILURE!");

                delegate.updateClientInfo("Failed!");
            }
        };
        
        LooperService.getShared().asyncInBackground(new SimpleCallback() {
            @Override
            public void perform() {
                try {
                    _connector.start(success, failure);
                } catch (Exception e) {
                    
                }
            }
        });
        
        _delegate.updateClientInfo(_client.getName());
    }
    
    @Override
    public void stop() {
        LooperService.getShared().asyncInBackground(new SimpleCallback() {
            @Override
            public void perform() {
                stopAndResetTransmitter();
            }
        });
    }
    
    private void stopAndResetTransmitter() {
        if (_transmitter != null) {
            _transmitter.stop();
            _transmitter = null;
        }
    }
    
    private @NotNull BDTransmitter startServer(@NotNull BESocket socket) {
        stopAndResetTransmitter();

        try {
            _transmitter = ConnectingPresenter.startServer(socket);
        } catch (Exception e) {
            Logger.error(this, "Cannot start transmitter, error: " + e);
        }

        return _transmitter;
    }
}
