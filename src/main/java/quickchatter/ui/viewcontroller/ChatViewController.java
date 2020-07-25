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
import utilities.AlertWindows;
import utilities.Callback;
import utilities.Logger;
import utilities.LooperService;
import utilities.Path;
import utilities.SimpleCallback;
import utilities.TimeValue;

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
                
                pickFileToSend();
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
        final ChatViewController self = this;
        
        SimpleCallback onAccept = new SimpleCallback() {
            @Override
            public void perform() {
                try {
                    _systemRouter.pickFileDestination(accept, deny, name, "Pick destination");
                } catch (Exception e) {
                    Logger.error(self, "Failed to pick file destination, internal error: " + e);
                    
                    deny.perform();
                }
            }
        };
        
        AlertWindows.showChoice(_view, "Transfer", "Other side wants to send file: " + description, "Accept", "Deny", onAccept, deny, deny);
    }

    @Override
    public void onConnectionRestored() {
        
    }

    @Override
    public void onConnectionTimeout(boolean isWarning) {
        final ChatViewController self = this;
        
        if (isWarning) {
            _view.addChatLine("Connection is slow...");
        } else {
            _view.addChatLine("Connection lost!");
            
            LooperService.getShared().asyncOnAWTAfterDelay(new SimpleCallback() {
                @Override
                public void perform() {
                    _presenter.stop();
                    
                    try {
                        _router.navigateToConnectMenuScreen();
                    } catch (Exception e) {
                        Logger.error(self, "Failed to navigate back, internal error: " + e);
                        // Cannot do anything else
                    }
                }
            }, TimeValue.buildSeconds(2));
        }
    }

    @Override
    public void showError(String title, String message) {
        AlertWindows.showErrorMessage(_view, title, message, "Ok");
    }
    
    // # Internals
    
    private void pickFileToSend() {
        try {
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
        } catch (Exception e) {
            handleNavigationError(e);
        }
    }
    
    private void handleNavigationError(@NotNull Exception e) {
        AlertWindows.showErrorMessage(_view, "Error", "Internal Error", "Ok");
    }
}
