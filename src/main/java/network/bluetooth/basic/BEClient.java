package network.bluetooth.basic;

import org.jetbrains.annotations.NotNull;

/// Supports equals() and hashCode().
public interface BEClient {
    int getIdentifier();

    @NotNull String getName();

    @NotNull BEClientDevice getDevice();
}
