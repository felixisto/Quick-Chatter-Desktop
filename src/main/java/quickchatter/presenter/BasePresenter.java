/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.presenter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickchatter.filesystem.fundamentals.DirectoryPath;
import quickchatter.filesystem.fundamentals.FilePath;
import quickchatter.mvp.MVP;
import quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import quickchatter.utilities.Callback;
import quickchatter.utilities.Path;

public interface BasePresenter extends MVP.Presenter {
    interface ConnectMenu extends BasePresenter {
        void start(@NotNull BasePresenterDelegate.ConnectMenu delegate) throws Exception;
    }
    
    interface Connect extends BasePresenter {
        void start(@NotNull BasePresenterDelegate.Connect delegate) throws Exception;
        
        void stop();
        
        boolean isScanning();

        void startScan();
        void stopScan();

        void pickItem(int index);
    }
    
    interface Reconnect extends BasePresenter {
        void start(@NotNull BasePresenterDelegate.Reconnect delegate) throws Exception;
        
        void stop();
        
        void pickItem(int index);
    }
    
    interface Connecting extends BasePresenter {
        void start(@NotNull BasePresenterDelegate.Connecting delegate) throws Exception;
        
        void stop();
    }
    
    interface Chat extends BasePresenter {
        void start(@NotNull BasePresenterDelegate.Chat delegate) throws Exception;
        void stop();
        
        void sendMessage(@NotNull String message);

        boolean canSendFile();
        void sendFile(@NotNull Path path);
    }
    
    interface FileSystem extends BasePresenter {
        @NotNull DirectoryPath getDirectoryPath();

        void onNavigateBack();
    }

    interface FileSystemNavigation extends FileSystem {
        void start(@NotNull BasePresenterDelegate.FileSystemNavigation delegate) throws Exception;
        
        @NotNull DirectoryPath getRootDirectoryPath();
    }

    interface FileSystemDirectory extends FileSystem {
        void start(@NotNull BasePresenterDelegate.FileSystemDirectory delegate) throws Exception;

        @NotNull FileSystemEntityViewModel getCurrentDirectory();
        @Nullable FileSystemEntityViewModel getEntityInfoAt(int index);
        
        void navigateToEntityAt(int index);

        void scrollBy(double scrollValue);
    }

    // Pick file.
    interface FilePicker extends FileSystem {
        void start(@NotNull BasePresenterDelegate.FilePicker filePickerDelegate,
                   @NotNull BasePresenterDelegate.FileSystemNavigation navigationDelegate,
                   @NotNull BasePresenterDelegate.FileSystemDirectory directoryDelegate) throws Exception;

        void closeWithoutPick();

        void pickFile(@NotNull FileSystemEntityViewModel model);
        void pickDirectory(@NotNull FileSystemEntityViewModel model);

        @NotNull FileSystemNavigation getNavigationSubpresenter();
        @NotNull FileSystemDirectory getSystemDirectorySubpresenter();
    }

    // Pick directory.
    interface DirectoryPicker extends FilePicker {
        @NotNull FilePath buildPath(@NotNull Path base);

        boolean isPathAvailable(@NotNull FilePath path);

        @NotNull String getName();
        void setName(@NotNull String name) throws Exception;
    }
    
    // Pick file or directory as a destination path.
    interface FileDestinationPicker extends FilePicker {
        @NotNull FileDestinationHandler getDestinationHandler();
        
        @NotNull FilePath buildPath(@NotNull Path base);

        boolean isPathAvailable(@NotNull FilePath path);

        @NotNull String getName();
        void setName(@NotNull String name) throws Exception;
    }
    
    // Handles file destination specific cases.
    interface FileDestinationHandler {
        void onFileOverwrite(@NotNull Callback<Boolean> handler);
        void onPickFileName(@NotNull Callback<String> handler, @NotNull String initialName);
    }
}
