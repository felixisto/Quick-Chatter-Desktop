/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui.viewcontroller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickchatter.mvp.MVP;
import quickchatter.navigation.Router;
import network.bluetooth.basic.BEClient;
import quickchatter.presenter.BasePresenter;
import quickchatter.ui.ViewBuilder;
import quickchatter.ui.listdata.BEClientsNamesListData;
import quickchatter.ui.listdata.JListData;
import quickchatter.ui.view.BaseView;
import quickchatter.ui.view.ReconnectFrame;
import utilities.AlertWindows;
import utilities.Callback;
import utilities.Logger;
import utilities.SimpleCallback;

public class ReconnectViewController implements BaseViewController.Reconnect {
    private final @NotNull BasePresenter.Reconnect _presenter;
    private final @NotNull Router.Primary _router;
    private final @NotNull ReconnectFrame _view;
    
    private @Nullable BEClientsNamesListData _namesData;
    
    public ReconnectViewController(@NotNull BasePresenter.Reconnect presenter, @NotNull Router.Primary router) {
        _presenter = presenter;
        _router = router;
        
        _view = new ViewBuilder(_router).buildReconnectScreen();
    }
    
    private void setupView() {
        _view.onNavigateBack = new SimpleCallback() {
            @Override
            public void perform() {
                _router.navigateBack();
            }
        };
        
        _view.onDoubleClickItem = new Callback<Integer>() {
            @Override
            public void perform(Integer argument) {
                _presenter.pickItem(argument);
            }
        };
    }
    
    // # BaseViewController.Reconnect

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
    public @NotNull MVP.View getView() {
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
    public void updateClientsListData(@NotNull JListData<BEClient> data) {
        BEClientsNamesListData namesData = new BEClientsNamesListData(data);
        _namesData = namesData;
        _view.updateClientsListData(namesData);
    }

    @Override
    public void navigateToConnectingScreen(@NotNull BEClient client) {
        Logger.message(this, "Try to connect to client " + client.getName());
        
        displayAskServerOrClient(new Callback<Boolean>() {
            @Override
            public void perform(Boolean asServer) {
                navigateToConnectingScreen(client, asServer);
            }
        });
    }
    
    // # Internal
    
    private void displayAskServerOrClient(@NotNull Callback<Boolean> completion) {
        SimpleCallback aCallback = new SimpleCallback() {
            @Override
            public void perform() {
                completion.perform(false);
            }
        };
        
        SimpleCallback bCallback = new SimpleCallback() {
            @Override
            public void perform() {
                completion.perform(true);
            }
        };
        
        AlertWindows.showChoice(_view, "Connect", "Connect as:", "Client", "Server", aCallback, bCallback);
    }
    
    private void navigateToConnectingScreen(@NotNull BEClient client, boolean asServer) {
        try {
            if (asServer) {
                _router.navigateToConnectingAsServer(client);
            } else {
                _router.navigateToConnectingAsClient(client);
            }
        } catch (Exception e) {
            handleNavigationError(e);
        }
    }
    
    private void handleNavigationError(@NotNull Exception e) {
        AlertWindows.showErrorMessage(_view, "Error", "Internal Error", "Ok");
    }
}

