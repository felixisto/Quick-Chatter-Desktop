package network.bluetooth.basic;

import org.jetbrains.annotations.NotNull;

import utilities.Callback;

/// Opens server or client sockets when pairing with another bluetooth device.
public interface BEConnector {
    interface Server extends BEConnector {
        void start(@NotNull Callback<BESocket> success, @NotNull Callback<Exception> failure) throws Exception, BEError;
        void stop();
    }

    interface Client extends BEConnector {
        @NotNull BEClient getServer();

        void connect(@NotNull Callback<BESocket> success, @NotNull Callback<Exception> failure) throws Exception, BEError;
        void stop();
    }
}
