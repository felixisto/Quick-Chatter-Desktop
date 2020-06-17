package network.bluetooth.basic;

import org.jetbrains.annotations.NotNull;

import network.basic.Scanner;

public interface BEClientScanner extends Scanner {
    void subscribe(@NotNull BEClientScannerListener listener);
    void unsubscribe(@NotNull BEClientScannerListener listener);
}
