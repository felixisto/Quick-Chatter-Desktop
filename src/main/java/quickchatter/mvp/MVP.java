/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.mvp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// This class defines the MVP classes relations and describes the basic usage of them.
//
// View controllers hold the view and the presenter, both are initialized immediately.
// View controller modify the lifecycle of their views.
//
// Presenters holds a view, which is not necessarily set on init.
// Presenters must be started, thats when their view is set, they should be started
// by the view controllers onStart().
//
// Views are started by their view controllers. Presenters should NOT modify the lifecycle
// of their views, view controllers do that.
public interface MVP {
    interface Lifecycle {
        // Call this once, sometime after the view controller is created.
        void onStart();
    
        // Call this when the view controller "loses focus" (usually when either a new
        // view controller shows up ontop of this one, or if the user sends the app
        // to the background).
        void onSuspended();
    
        // Call this to revert suspended state.
        void onResume();
    
        // Optional - call this when view controller should cleanup.
        void onTerminate();
    }
    
    interface View extends Lifecycle {
        // Instructs the view to go back to the previous window/frame, if possible.
        void navigateBack();
    }
    
    interface ViewController extends Lifecycle {
        @NotNull MVP.View getView();
        @NotNull MVP.Presenter getPresenter();
        
        // Instructs the view controller to go back to the previous window/frame, if possible.
        void navigateBack();
    }
    
    interface Presenter {
        @Nullable MVP.View getView();
    }
}
