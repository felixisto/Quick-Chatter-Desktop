package network.bluetooth.basic;

import org.jetbrains.annotations.NotNull;

import utilities.SimpleCallback;
import utilities.TimeValue;

/// Begins emitting the presence of the user's device.
public interface BEEmitter {
    boolean isBluetoothOn();

    boolean isEmitting();

    @NotNull TimeValue getTime();

    void start() throws Exception;

    // Note: not all emitters support this.
    void stop() throws Exception;

    void addEndCompletion(@NotNull SimpleCallback completion);
}
