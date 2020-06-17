/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui.viewcontroller;

import org.jetbrains.annotations.NotNull;
import quickchatter.mvp.MVP;
import quickchatter.navigation.Router;
import quickchatter.presenter.BasePresenter;
import quickchatter.ui.ViewBuilder;
import quickchatter.ui.view.BaseView;
import quickchatter.ui.view.ConnectMenuFrame;
import utilities.Logger;
import utilities.SimpleCallback;

public class ConnectMenuViewController implements BaseViewController.ConnectMenu {
    private final @NotNull BasePresenter.ConnectMenu _presenter;
    private final @NotNull Router.Primary _router;
    private final @NotNull ConnectMenuFrame _view;
    
    public ConnectMenuViewController(@NotNull BasePresenter.ConnectMenu presenter, @NotNull Router.Primary router) {
        _presenter = presenter;
        _router = router;
        
        _view = new ViewBuilder(_router).buildConnectMenuScreen();
    }
    
    private void setupView() {
        _view.onConnectClick = new SimpleCallback() {
            @Override
            public void perform() {
                _router.navigateToConnectScreen();
            }
        };
        
        _view.onReconnectClick = new SimpleCallback() {
            @Override
            public void perform() {
                _router.navigateToReconnectScreen();
            }
        };
    }
    
    // # BaseViewController.ConnectMenu

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
}
