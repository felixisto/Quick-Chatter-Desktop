/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.navigation;

import org.jetbrains.annotations.NotNull;
import network.bluetooth.basic.BEClient;
import network.bluetooth.basic.BETransmitter;
import utilities.Callback;
import utilities.Path;
import utilities.SimpleCallback;

public interface Router {
    void navigateBack();

    interface Primary extends Router {
        void navigateToConnectMenuScreen() throws Exception;
        void navigateToConnectScreen() throws Exception;
        void navigateToReconnectScreen() throws Exception;
        void navigateToConnectingAsServer(@NotNull BEClient client) throws Exception;
        void navigateToConnectingAsClient(@NotNull BEClient client) throws Exception;
        void navigateToChatScreen(@NotNull BEClient client,
                                  @NotNull BETransmitter.ReaderWriter transmitter,
                                  @NotNull BETransmitter.Service transmitterService) throws Exception;
    }

    interface FileSystem extends Router {

    }

    interface System extends Router {
        void closeAllPopupWindows();
        
        void pickFile(@NotNull Callback<Path> success, @NotNull SimpleCallback failure, @NotNull String description) throws Exception;
        void pickDirectory(@NotNull Callback<Path> success, @NotNull SimpleCallback failure, @NotNull String description) throws Exception;
        void pickFileDestination(@NotNull Callback<Path> success, @NotNull SimpleCallback failure, @NotNull String name, @NotNull String description) throws Exception;
    }
}
