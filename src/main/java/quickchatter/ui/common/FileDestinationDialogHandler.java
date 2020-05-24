/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui.common;

import java.awt.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickchatter.presenter.BasePresenter;
import quickchatter.ui.view.BaseView;
import quickchatter.utilities.AlertWindows;
import quickchatter.utilities.Callback;
import quickchatter.utilities.SimpleCallback;

public class FileDestinationDialogHandler implements BasePresenter.FileDestinationHandler {
    private @Nullable Component _view;

    public void setView(@Nullable BaseView view) {
        _view = null;
        
        if (view instanceof BaseView.Whole) {
            _view = ((BaseView.Whole)view).asFrame();
        }
        if (view instanceof BaseView.Part) {
            _view = ((BaseView.Part)view).asPanel();
        }
    }

    // # Presenter.FileDestinationHandler

    @Override
    public void onFileOverwrite(final @NotNull Callback<Boolean> handler) {
        if (_view == null) {
            return;
        }
        
        AlertWindows.showConfirmationMessage(_view,
                "File name",
                "Overwrite file?",
                new SimpleCallback() {
            @Override
            public void perform() {
                handler.perform(true);
            }
        }, new SimpleCallback() {
            @Override
            public void perform() {
                handler.perform(false);
            }
        });
    }

    @Override
    public void onPickFileName(@NotNull final Callback<String> handler, @NotNull String initialName) {
        if (_view == null) {
            return;
        }
        
        AlertWindows.showTextField(_view, "File name", initialName, handler);
    }
}

