/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.navigation;

import org.jetbrains.annotations.NotNull;
import quickchatter.network.bluetooth.basic.BEClient;
import quickchatter.network.bluetooth.basic.BETransmitter;
import quickchatter.utilities.Callback;
import quickchatter.utilities.Path;
import quickchatter.utilities.SimpleCallback;

public interface Router {
    void navigateBack();

    interface Primary extends Router {
        void navigateToConnectMenuScreen();
        void navigateToConnectScreen();
        void navigateToReconnectScreen();
        void navigateToConnectingAsServer(@NotNull BEClient client);
        void navigateToConnectingAsClient(@NotNull BEClient client);
        void navigateToChatScreen(@NotNull BEClient client,
                                  @NotNull BETransmitter.ReaderWriter transmitter,
                                  @NotNull BETransmitter.Service transmitterService);
    }

    interface FileSystem extends Router {

    }

    interface System extends Router {
        void closeAllPopupWindows();
        
        void pickFile(@NotNull Callback<Path> success, @NotNull SimpleCallback failure, @NotNull String description);
        void pickDirectory(@NotNull Callback<Path> success, @NotNull SimpleCallback failure, @NotNull String description);
        void pickFileDestination(@NotNull Callback<Path> success, @NotNull SimpleCallback failure, @NotNull String name, @NotNull String description);
    }
}
