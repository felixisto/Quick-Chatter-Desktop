/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.navigation;

import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import filesystem.model.EntityInfo;
import filesystem.simple.SimpleFileSystem;
import filesystem.worker.loader.FileSystemLoaderLocal;
import network.bluetooth.basic.BEClient;
import network.bluetooth.basic.BEConnector;
import network.bluetooth.basic.BETransmitter;
import network.bluetooth.bluecove.BCAdapter;
import network.bluetooth.bluecove.connectors.BCClientConnector;
import network.bluetooth.bluecove.connectors.BCServerConnector;
import network.bluetooth.bluecove.discovery.BCClientScanner;
import network.bluetooth.bluecove.discovery.BCDiscovery;
import network.bluetooth.bluecove.discovery.BCEmitter;
import network.bluetooth.bluecove.discovery.BCPairing;
import quickchatter.presenter.ChatPresenter;
import quickchatter.presenter.ConnectMenuPresenter;
import quickchatter.presenter.ConnectPresenter;
import quickchatter.presenter.ConnectingClientPresenter;
import quickchatter.presenter.ConnectingServerPresenter;
import quickchatter.presenter.FileDestinationPickerPresenter;
import quickchatter.presenter.FilePickerPresenter;
import quickchatter.presenter.ReconnectPresenter;
import quickchatter.ui.ViewBuilder;
import quickchatter.ui.common.FileDestinationDialogHandler;
import quickchatter.ui.parser.EntityInfoToVMParser;
import quickchatter.ui.viewcontroller.BaseViewController;
import quickchatter.ui.viewcontroller.ChatViewController;
import quickchatter.ui.viewcontroller.ConnectMenuViewController;
import quickchatter.ui.viewcontroller.ConnectViewController;
import quickchatter.ui.viewcontroller.ConnectingClientViewController;
import quickchatter.ui.viewcontroller.ConnectingServerViewController;
import quickchatter.ui.viewcontroller.DirectoryPickerViewController;
import quickchatter.ui.viewcontroller.FileDestinationPickerViewController;
import quickchatter.ui.viewcontroller.FilePickerViewController;
import quickchatter.ui.viewcontroller.ReconnectViewController;
import quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import utilities.Callback;
import utilities.Errors;
import utilities.Logger;
import utilities.LooperSwing;
import utilities.Parser;
import utilities.Path;
import utilities.SimpleCallback;
import utilities.TimeValue;

public class PrimaryRouter implements Router.Primary, Router.System {
    private final @NotNull AtomicReference<State> _state = new AtomicReference<>();

    private final @NotNull BCAdapter _adapter;

    private final @NotNull BCClientScanner _scanner;
    private final @NotNull BCEmitter _emitter;

    private final @NotNull SimpleFileSystem _fileSystem = new SimpleFileSystem();
    private final @NotNull FileSystemLoaderLocal _fileSystemLoader;
    private @Nullable EntityInfo.Directory _rootDirectoryRepo;
    
    private @NotNull BaseViewController _currentViewController;
    private @NotNull BaseViewController _popoverViewController;
    private final @NotNull ViewBuilder _viewBuilder;

    public PrimaryRouter() {
        _state.set(State.connectMenu);
        _adapter = BCAdapter.getShared();
        _scanner = new BCClientScanner(new BCDiscovery(_adapter));
        _emitter = new BCEmitter(_adapter, BCEmitter.DEFAULT_EMIT_TIME);
        _fileSystemLoader = new FileSystemLoaderLocal(_fileSystem);
        _viewBuilder = new ViewBuilder(this);

        Logger.message(this.getClass().getCanonicalName(), "Initialized");

        goToConnectMenuScreen();
    }

    // # Properties

    @NotNull State getState() {
        return _state.get();
    }

    // # Router.Primary

    @Override
    public void navigateBack() {
        Logger.message(this, "Navigate back...");

        // System screen navigation
        if (isFilePickerScreenVisible()) {
            exitFilePickerScreen();
            return;
        }

        // Cannot navigate away from root
        if (getState() == State.connectMenu) {
            Logger.message(this, "Cannot navigate back, already at root screen");
            return;
        }
        
        // Go back to root screen
        if (getState() == State.connect) {
            goToConnectMenuScreen();
            return;
        }

        // Go back to root screen
        if (getState() == State.reconnect) {
            goToConnectMenuScreen();
            return;
        }

        if (getState() == State.connecting) {
            goToConnectScreen();
            return;
        }

        if (getState() == State.chat) {
            goToConnectMenuScreen();
            return;
        }

        Logger.error(this, "Unknown screen, cannot navigate back, internal error.");
    }

    @Override
    public void navigateToConnectMenuScreen() throws Exception {
        if (getState() == State.connectMenu) {
            String message = "Already at connect menu screen, no need for navigation.";
            Logger.warning(this, message);
            Errors.throwIllegalStateError(message);
        }
        
        closeAllPopupWindows();

        goToConnectMenuScreen();
    }

    @Override
    public void navigateToConnectScreen() throws Exception {
        if (getState() == State.connect) {
            String message = "Already at connect screen, no need for navigation.";
            Logger.warning(this, message);
            Errors.throwIllegalStateError(message);
        }

        if (getState() != State.connectMenu && getState() != State.chat) {
            String message = "Can navigate to connect screen only from either chat OR connect menu screens!";
            Logger.error(this, message);
            Errors.throwIllegalStateError(message);
        }
        
        closeAllPopupWindows();

        goToConnectScreen();
    }

    @Override
    public void navigateToReconnectScreen() throws Exception {
        if (getState() == State.reconnect) {
            String message = "Already at connect screen, no need for navigation.";
            Logger.warning(this, message);
            Errors.throwIllegalStateError(message);
        }

        if (getState() != State.connectMenu) {
            String message = "Can navigate to reconnect screen only from connect menu screen!";
            Logger.error(this, message);
            Errors.throwIllegalStateError(message);
        }
        
        closeAllPopupWindows();

        goToReconnectScreen();
    }

    @Override
    public void navigateToConnectingAsServer(@NotNull BEClient client) throws Exception {
        closeAllPopupWindows();
        
        navigateToConnecting(client, true);
    }

    @Override
    public void navigateToConnectingAsClient(@NotNull BEClient client) throws Exception {
        closeAllPopupWindows();
        
        navigateToConnecting(client, false);
    }

    private void navigateToConnecting(@NotNull BEClient client, boolean isServer) {
        if (getState() == State.connecting) {
            String message = "Already at connecting screen, no need for navigation.";
            Logger.warning(this, message);
            Errors.throwIllegalStateError(message);
        }

        if (getState() != State.connect && getState() != State.reconnect) {
            String message = "Can navigate to connecting screen from either reconnect OR connect screen!";
            Logger.error(this, message);
            Errors.throwIllegalStateError(message);
        }
        
        closeAllPopupWindows();

        if (isServer) {
            goToConnectingServerScreen(client);
        } else {
            goToConnectingClientScreen(client);
        }
    }

    @Override
    public void navigateToChatScreen(@NotNull BEClient client,
                                     @NotNull BETransmitter.ReaderWriter transmitter,
                                     @NotNull BETransmitter.Service transmitterService) throws Exception {
        if (getState() == State.chat) {
            String message = "Already at chat screen, no need for navigation.";
            Logger.warning(this, message);
            Errors.throwIllegalStateError(message);
        }

        if (getState() != State.connecting) {
            String message = "Can navigate to chat screen only from connecting screen!";
            Logger.error(this, message);
            Errors.throwIllegalStateError(message);
        }
        
        closeAllPopupWindows();

        goToChatScreen(client, transmitter, transmitterService);
    }

    // # Router.System
    
    @Override
    public void closeAllPopupWindows() {
        if (isFilePickerScreenVisible()) {
            exitFilePickerScreen();
        }
    }

    @Override
    public void pickFile(@NotNull Callback<Path> success, @NotNull SimpleCallback failure, @NotNull String description) throws Exception {
        if (isFilePickerScreenVisible()) {
            String message = "Cannot open pick file screen again, its already open.";
            Logger.error(this, message);
            Errors.throwIllegalStateError(message);
        }
        
        openPickFileScreen(success, failure, description);
    }

    @Override
    public void pickDirectory(@NotNull Callback<Path> success, @NotNull SimpleCallback failure, @NotNull String description) throws Exception {
        if (isFilePickerScreenVisible()) {
            String message = "Cannot open pick directory screen again, its already open.";
            Logger.error(this, message);
            Errors.throwIllegalStateError(message);
        }
        
        openPickDirectoryScreen(success, failure, description);
    }

    @Override
    public void pickFileDestination(@NotNull Callback<Path> success, @NotNull SimpleCallback failure, @NotNull String name, @NotNull String description) throws Exception {
        if (isFilePickerScreenVisible()) {
            String message = "Cannot open pick file screen again, its already open";
            Logger.error(this, message);
            Errors.throwIllegalStateError(message);
        }
        
        openPickFileDestinationScreen(success, failure, name, description);
    }

    // # Go to

    private void goToConnectMenuScreen() {
        Logger.message(this, "Go to connect menu screen");

        _state.set(State.connectMenu);
        
        final PrimaryRouter self = this;
        
        LooperSwing.getShared().performCallback(new SimpleCallback() {
            @Override
            public void perform() {
                destroyCurrentContentViewController();
                
                ConnectMenuPresenter presenter = new ConnectMenuPresenter();
                ConnectMenuViewController vc = new ConnectMenuViewController(presenter, self);
                
                _currentViewController = vc;
                
                vc.onStart();
            }
        });
    }

    private void goToConnectScreen() {
        Logger.message(this, "Go to connect screen");

        _state.set(State.connect);
        
        final PrimaryRouter self = this;
        
        LooperSwing.getShared().performCallback(new SimpleCallback() {
            @Override
            public void perform() {
                destroyCurrentContentViewController();
                
                ConnectPresenter presenter = new ConnectPresenter(_scanner, _emitter);
                ConnectViewController vc = new ConnectViewController(presenter, self);
                
                _currentViewController = vc;
                
                vc.onStart();
            }
        });
    }

    private void goToReconnectScreen() {
        Logger.message(this, "Go to reconnect screen");

        _state.set(State.reconnect);
        
        final PrimaryRouter self = this;
        
        LooperSwing.getShared().performCallback(new SimpleCallback() {
            @Override
            public void perform() {
                destroyCurrentContentViewController();
                
                BCPairing pairing = new BCPairing(_adapter);
                ReconnectPresenter presenter = new ReconnectPresenter(pairing);
                ReconnectViewController vc = new ReconnectViewController(presenter, self);
                
                _currentViewController = vc;
                
                vc.onStart();
            }
        });
    }

    private void goToConnectingServerScreen(@NotNull BEClient client) {
        Logger.message(this, "Go to connecting screen as server, for client " + client.getName());
        
        _state.set(State.connecting);
        
        final PrimaryRouter self = this;
        
        LooperSwing.getShared().performCallback(new SimpleCallback() {
            @Override
            public void perform() {
                destroyCurrentContentViewController();
                
                BEConnector.Server connector = new BCServerConnector("QuickChat", _adapter);
                ConnectingServerPresenter presenter = new ConnectingServerPresenter(client, connector);
                ConnectingServerViewController vc = new ConnectingServerViewController(presenter, self);
                
                _currentViewController = vc;
                
                vc.onStart();
            }
        });
    }

    private void goToConnectingClientScreen(@NotNull BEClient client) {
        Logger.message(this, "Go to connecting screen as client, for server " + client.getName());
        
        _state.set(State.connecting);
        
        final PrimaryRouter self = this;
        
        LooperSwing.getShared().performCallback(new SimpleCallback() {
            @Override
            public void perform() {
                destroyCurrentContentViewController();
                
                BEConnector.Client connector = new BCClientConnector(client, _adapter, 10, TimeValue.buildSeconds(1));
                ConnectingClientPresenter presenter = new ConnectingClientPresenter(client, connector);
                ConnectingClientViewController vc = new ConnectingClientViewController(presenter, self);
                
                _currentViewController = vc;
                
                vc.onStart();
            }
        });
    }

    private void goToChatScreen(@NotNull BEClient client,
                                @NotNull BETransmitter.ReaderWriter transmitter,
                                @NotNull BETransmitter.Service transmitterService) {
        Logger.message(this, "Go to chat screen");
        
        _state.set(State.chat);
        
        final PrimaryRouter self = this;
        
        LooperSwing.getShared().performCallback(new SimpleCallback() {
            @Override
            public void perform() {
                destroyCurrentContentViewController();
                
                ChatPresenter presenter = new ChatPresenter(client, transmitter, transmitterService);
                ChatViewController vc = new ChatViewController(presenter, self, self);
                
                _currentViewController = vc;
                
                vc.onStart();
            }
        });
    }

    private void openPickFileScreen(@NotNull Callback<Path> success,
                                    @NotNull SimpleCallback failure,
                                    @NotNull String description) {
        Logger.message(this, "Open pick file screen");
        
        final PrimaryRouter self = this;
        
        LooperSwing.getShared().performCallback(new SimpleCallback() {
            @Override
            public void perform() {
                try {
                    _rootDirectoryRepo = _fileSystemLoader.readRootDirectoryInfo();
                } catch (Exception e) {
                    Logger.error(self, "Failed to open pick file screen, could not load root info, error:" + e);
                    return;
                }
                
                Parser<EntityInfo, FileSystemEntityViewModel> parser = new EntityInfoToVMParser();
                FilePickerPresenter presenter = new FilePickerPresenter(_fileSystemLoader, _rootDirectoryRepo, parser);
                FilePickerViewController vc = new FilePickerViewController(presenter, presenter, presenter, self, success, failure, description);
                
                _popoverViewController = vc;
                
                vc.onStart();
            }
        });
    }
    
    private void openPickDirectoryScreen(@NotNull Callback<Path> success,
                                         @NotNull SimpleCallback failure,
                                         @NotNull String description) {
        Logger.error(this, "Open pick directory screen");
        
        final PrimaryRouter self = this;
        
        LooperSwing.getShared().performCallback(new SimpleCallback() {
            @Override
            public void perform() {
                try {
                    _rootDirectoryRepo = _fileSystemLoader.readRootDirectoryInfo();
                } catch (Exception e) {
                    Logger.error(self, "Failed to open pick directory screen, could not load root info, error:" + e);
                    failure.perform();
                    return;
                }
                
                Parser<EntityInfo, FileSystemEntityViewModel> parser = new EntityInfoToVMParser();
                FilePickerPresenter presenter = new FilePickerPresenter(_fileSystemLoader, _rootDirectoryRepo, parser);
                DirectoryPickerViewController vc = new DirectoryPickerViewController(presenter, presenter, presenter, self, success, failure, description);
                
                _popoverViewController = vc;
                
                vc.onStart();
            }
        });
    }

    private void openPickFileDestinationScreen(@NotNull Callback<Path> success,
                                               @NotNull SimpleCallback failure,
                                               @NotNull String name,
                                               @NotNull String description) {
        Logger.message(this, "Open pick file destination screen");
        
        final PrimaryRouter self = this;
        
        LooperSwing.getShared().performCallback(new SimpleCallback() {
            @Override
            public void perform() {
                try {
                    _rootDirectoryRepo = _fileSystemLoader.readRootDirectoryInfo();
                } catch (Exception e) {
                    Logger.error(self, "Failed to open pick file screen, could not load root info, error:" + e);
                    return;
                }
                
                Parser<EntityInfo, FileSystemEntityViewModel> parser = new EntityInfoToVMParser();
                FileDestinationDialogHandler fileDestDialogHandler = new FileDestinationDialogHandler();
                
                FileDestinationPickerPresenter presenter = new FileDestinationPickerPresenter(_fileSystemLoader, _rootDirectoryRepo, parser, fileDestDialogHandler);
                FileDestinationPickerViewController vc = new FileDestinationPickerViewController(presenter, presenter, presenter, self, fileDestDialogHandler, success, failure, description);
                
                try {
                    presenter.setName(name);
                } catch (Exception e) {
                    
                }
                
                fileDestDialogHandler.setView(vc.getViewAsBaseView());
                
                _popoverViewController = vc;
                
                vc.onStart();
            }
        });
    }
    
    // # Helpers
    
    private void destroyCurrentContentViewController() {
        if (_currentViewController == null) {
            return;
        }
        
        _currentViewController.onTerminate();
        
        _currentViewController = null;
    }
    
    private void hideCurrentContentViewController() {
        if (_currentViewController == null) {
            return;
        }
        
        _currentViewController.onSuspended();
    }
    
    private void unhideCurrentContentViewController() {
        if (_currentViewController == null) {
            return;
        }
        
        _currentViewController.onResume();
    }
    
    // # File picker screen

    private boolean isFilePickerScreenVisible() {
        return _popoverViewController instanceof FilePickerViewController;
    }

    private void exitFilePickerScreen() {
        if (_popoverViewController == null) {
            return;
        }
        
        Logger.message(this, "Exit pick file screen");
        
        _popoverViewController.onTerminate();
        
        _popoverViewController = null;
    }

    // # State

    enum State {
        connectMenu, connect, reconnect, connecting, chat;
    }
}

