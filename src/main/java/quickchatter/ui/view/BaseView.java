/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui.view;

import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import quickchatter.mvp.MVP;

public interface BaseView extends MVP.View {
    // Generic method for force updating the UI.
    public void reloadData();
    
    // A view, that is standalone and independent from other windows/dialogues.
    interface Whole extends BaseView {
        public @NotNull JFrame asFrame();
        
        public void open();
    }
    
    // A view, that is part of another view.
    interface Part extends BaseView {
        public @NotNull JPanel asPanel();
    }
}
