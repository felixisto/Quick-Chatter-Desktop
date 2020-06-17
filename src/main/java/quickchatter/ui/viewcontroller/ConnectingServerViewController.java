/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui.viewcontroller;

import org.jetbrains.annotations.NotNull;
import quickchatter.mvp.MVP;
import quickchatter.navigation.Router;
import network.bluetooth.basic.BEClient;
import network.bluetooth.basic.BETransmitter;
import quickchatter.presenter.BasePresenter;
import quickchatter.ui.ViewBuilder;
import quickchatter.ui.view.BaseView;
import quickchatter.ui.view.ConnectingFrame;
import utilities.Logger;
import utilities.SimpleCallback;

public class ConnectingServerViewController implements BaseViewController.Connecting {
    private final @NotNull BasePresenter.Connecting _presenter;
    private final @NotNull Router.Primary _router;
    private final @NotNull ConnectingFrame _view;
    
    public ConnectingServerViewController(@NotNull BasePresenter.Connecting presenter, @NotNull Router.Primary router) {
        _presenter = presenter;
        _router = router;
        
        _view = new ViewBuilder(_router).buildConnectingScreen();
    }
    
    private void setupView() {
        _view.onCancelClick = new SimpleCallback() {
            @Override
            public void perform() {
                _router.navigateBack();
            }
        };
    }
    
    // # BaseViewController.Connecting

    @Override
    public MVP.View getView() {
        return _view;
    }

    @Override
    public MVP.Presenter getPresenter() {
        return _presenter;
    }
    
    @Override
    public @NotNull BaseView getViewAsBaseView() {
        return _view;
    }

    @Override
    public void navigateBack() {
        _router.navigateBack();
    }

    @Override
    public void onStart() {
        setupView();
        
        _view.onStart();
        
        try {
            _presenter.start(this);
        } catch (Exception e) {
            Logger.warning(this.getClass().getCanonicalName(), "Failed to start presenter, error: " + e.toString());
        }
    }

    @Override
    public void onSuspended() {
        
    }

    @Override
    public void onResume() {
        
    }

    @Override
    public void onTerminate() {
        _presenter.stop();
        
        _view.onTerminate();
    }
    
    @Override
    public void updateClientInfo(@NotNull String name) {
        _view.setDescription("Connecting to " + name);
    }
    
    @Override
    public void navigateToChatScreen(@NotNull BEClient client,
                                     @NotNull BETransmitter.ReaderWriter transmitter,
                                     @NotNull BETransmitter.Service transmitterService) {
        _router.navigateToChatScreen(client, transmitter, transmitterService);
    }
}

