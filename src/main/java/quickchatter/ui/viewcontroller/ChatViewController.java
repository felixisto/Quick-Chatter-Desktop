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
import quickchatter.ui.view.ChatFrame;
import quickchatter.utilities.AlertWindows;
import quickchatter.utilities.Callback;
import quickchatter.utilities.Logger;
import quickchatter.utilities.Path;
import quickchatter.utilities.SimpleCallback;

public class ChatViewController implements BaseViewController.Chat {
    private final @NotNull BasePresenter.Chat _presenter;
    private final @NotNull Router.Primary _router;
    private final @NotNull Router.System _systemRouter;
    private final @NotNull ChatFrame _view;
    
    public ChatViewController(@NotNull BasePresenter.Chat presenter, @NotNull Router.Primary router, @NotNull Router.System systemRouter) {
        _presenter = presenter;
        _router = router;
        _systemRouter = systemRouter;
        
        _view = new ViewBuilder(_router).buildChatScreen();
    }
    
    private void setupView() {
        _view.onNavigateBack = new SimpleCallback() {
            @Override
            public void perform() {
                _router.navigateBack();
            }
        };
        
        _view.onSendChat = new Callback<String>() {
            @Override
            public void perform(String argument) {
                _view.clearChatField();
                _presenter.sendMessage(argument);
            }
        };
        
        _view.onSendFile = new SimpleCallback() {
            @Override
            public void perform() {
                if (!_presenter.canSendFile()) {
                    showError("Error", "Already sending file!");
                    return;
                }
                
                _systemRouter.pickFile(new Callback<Path>() {
                    @Override
                    public void perform(Path path) {
                        _presenter.sendFile(path);
                    }
                }, new SimpleCallback() {
                    @Override
                    public void perform() {
                        
                    }
                }, "Pick file to send");
            }
        };
    }
    
    // # BaseViewController.Chat
    
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
    public void updateClientInfo(String name) {
        _view.setDescription("Chatting with " + name);
    }

    @Override
    public void updateChat(String newLine, String fullChat) {
        _view.setChat(fullChat);
    }

    @Override
    public void clearChatTextField() {
        _view.clearChatField();
    }

    @Override
    public void onAskToAcceptTransferFile(@NotNull Callback<Path> accept, @NotNull SimpleCallback deny, @NotNull String name, @NotNull String description) {
        SimpleCallback onAccept = new SimpleCallback() {
            @Override
            public void perform() {
                _systemRouter.pickFileDestination(accept, deny, name, "Pick destination");
            }
        };
        
        AlertWindows.showChoice(_view, "Transfer", "Other side wants to send file: " + description, "Accept", "Deny", onAccept, deny, deny);
    }

    @Override
    public void onConnectionRestored() {
        
    }

    @Override
    public void onConnectionTimeout(boolean isWarning) {
        
    }

    @Override
    public void showError(String title, String message) {
        AlertWindows.showErrorMessage(_view, title, message, "Ok");
    }
}
