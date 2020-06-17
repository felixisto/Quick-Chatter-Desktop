package network.bluetooth.basic;

import org.jetbrains.annotations.NotNull;

public interface BEClientScannerListener {
    void onScanStart();
    void onScanRestart();
    void onScanEnd();

    void onClientFound(@NotNull BEClient client);
    void onClientUpdate(@NotNull BEClient client);
    void onClientLost(@NotNull BEClient client);
}
