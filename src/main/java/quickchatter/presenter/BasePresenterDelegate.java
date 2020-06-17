/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.presenter;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import filesystem.fundamentals.DirectoryPath;
import filesystem.fundamentals.FilePath;
import quickchatter.mvp.MVP;
import network.bluetooth.basic.BEClient;
import network.bluetooth.basic.BETransmitter;
import quickchatter.ui.listdata.JListData;
import quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import utilities.Callback;
import utilities.Path;
import utilities.SimpleCallback;

public interface BasePresenterDelegate extends MVP.View {
    interface ConnectMenu extends BasePresenterDelegate {
        
    }
    
    interface GenericConnect extends BasePresenterDelegate {
        void updateClientsListData(@NotNull JListData<BEClient> data);

        void navigateToConnectingScreen(@NotNull BEClient client);
    }
    
    interface Connect extends GenericConnect {
        void onStartScan();
        void onEndScan();
    }
    
    interface Reconnect extends GenericConnect {
        
    }
    
    interface Connecting extends BasePresenterDelegate {
        void updateClientInfo(@NotNull String name);
        
        void navigateToChatScreen(@NotNull BEClient client,
                                  @NotNull BETransmitter.ReaderWriter transmitter,
                                  @NotNull BETransmitter.Service transmitterService);
    }
    
    interface Chat extends BasePresenterDelegate {
        void updateClientInfo(@NotNull String name);
        void updateChat(@NotNull String newLine, @NotNull String fullChat);
        void clearChatTextField();

        void onAskToAcceptTransferFile(@NotNull Callback<Path> accept,
                                       @NotNull SimpleCallback deny,
                                       @NotNull String name,
                                       @NotNull String description);

        void onConnectionRestored();
        void onConnectionTimeout(boolean isWarning);

        void showError(@NotNull String title, @NotNull String message);
    }
    
    interface FileSystem extends BasePresenterDelegate {
        void setCurrentPath(@NotNull DirectoryPath path);
    }

    interface FileSystemNavigation extends FileSystem {
        void setEntityData(@NotNull FileSystemEntityViewModel entity);
    }

    interface FileSystemDirectory extends FileSystem {
        void setEntityData(@NotNull List<FileSystemEntityViewModel> entities);
    }

    interface FilePicker extends FileSystem {
        void onCloseWithoutPicking();

        void onPickFile(@NotNull FilePath path) throws Exception;
        void onPickDirectory(@NotNull DirectoryPath path) throws Exception;
    }
}
