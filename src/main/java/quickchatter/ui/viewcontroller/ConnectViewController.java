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
import quickchatter.ui.view.ConnectFrame;
import utilities.AlertWindows;
import utilities.Callback;
import utilities.Logger;
import utilities.SimpleCallback;

public class ConnectViewController implements BaseViewController.Connect {
    private final @NotNull BasePresenter.Connect _presenter;
    private final @NotNull Router.Primary _router;
    private final @NotNull ConnectFrame _view;
    
    private @Nullable BEClientsNamesListData _namesData;
    
    public ConnectViewController(@NotNull BasePresenter.Connect presenter, @NotNull Router.Primary router) {
        _presenter = presenter;
        _router = router;
        
        _view = new ViewBuilder(_router).buildConnectScreen();
    }
    
    private void setupView() {
        _view.onNavigateBack = new SimpleCallback() {
            @Override
            public void perform() {
                _router.navigateBack();
            }
        };
        
        _view.onScanStart = new SimpleCallback() {
            @Override
            public void perform() {
                _presenter.startScan();
            }
        };
        
        _view.onScanEnd = new SimpleCallback() {
            @Override
            public void perform() {
                _presenter.stopScan();
            }
        };
        
        _view.onDoubleClickItem = new Callback<Integer>() {
            @Override
            public void perform(Integer argument) {
                _presenter.pickItem(argument);
            }
        };
    }
    
    // # BaseViewController.Connect

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
            public void perform(Boolean argument) {
                if (argument) {
                    _router.navigateToConnectingAsServer(client);
                } else {
                    _router.navigateToConnectingAsClient(client);
                }
            }
        });
    }
    
    @Override
    public void onStartScan() {
        Logger.message(this, "Scan started.");
        
        _view.startScan();
    }

    @Override
    public void onEndScan() {
        Logger.message(this, "Scan ended.");
        
        _view.stopScan();
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
}
