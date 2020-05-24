package quickchatter.network.bluetooth.basic;

import org.jetbrains.annotations.NotNull;

import quickchatter.utilities.Callback;

/// Opens server or client sockets when pairing with another bluetooth device.
public interface BEConnector {
    interface Server extends BEConnector {
        boolean isConnecting();
        boolean isConnected();

        void start(@NotNull Callback<BESocket> success, @NotNull Callback<Exception> failure) throws Exception, BEError;
        void stop();
    }

    interface Client extends BEConnector {
        @NotNull BEClient getServer();

        boolean isConnecting();
        boolean isConnected();

        void connect(@NotNull Callback<BESocket> success, @NotNull Callback<Exception> failure) throws Exception, BEError;
        void terminate();
    }
}
