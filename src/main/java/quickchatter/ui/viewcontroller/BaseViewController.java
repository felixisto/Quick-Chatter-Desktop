/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui.viewcontroller;

import org.jetbrains.annotations.NotNull;
import quickchatter.mvp.MVP;
import quickchatter.presenter.BasePresenterDelegate;
import quickchatter.ui.view.BaseView;

public interface BaseViewController extends MVP.ViewController {
    @NotNull BaseView getViewAsBaseView();
    
    interface ConnectMenu extends BaseViewController, BasePresenterDelegate.ConnectMenu {
        
    }
    
    interface Connect extends BaseViewController, BasePresenterDelegate.Connect {
        
    }
    
    interface Reconnect extends BaseViewController, BasePresenterDelegate.Reconnect {
        
    }
    
    interface Connecting extends BaseViewController, BasePresenterDelegate.Connecting {
        
    }
    
    interface Chat extends BaseViewController, BasePresenterDelegate.Chat {
        
    }
    
    interface FilePicker extends BaseViewController, BasePresenterDelegate.FilePicker, BasePresenterDelegate.FileSystemNavigation, BasePresenterDelegate.FileSystemDirectory {
        
    }
}
