/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.presenter;

import org.jetbrains.annotations.NotNull;
import quickchatter.mvp.MVP;
import utilities.Errors;
import utilities.Logger;

public class ConnectMenuPresenter implements BasePresenter.ConnectMenu {
    private @NotNull BasePresenterDelegate.ConnectMenu _delegate;
    
    public ConnectMenuPresenter() {
        
    }

    @Override
    public @NotNull MVP.View getView() {
        return _delegate;
    }
    
    @Override
    public void start(BasePresenterDelegate.ConnectMenu delegate) throws Exception {
        if (_delegate != null) {
            Errors.throwCannotStartTwice("Presenter already started!");
        }
        
        Logger.message(this, "Start.");
        
        _delegate = delegate;
    }
}
