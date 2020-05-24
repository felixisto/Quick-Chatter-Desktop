package quickchatter.network.bluetooth.basic;

import org.jetbrains.annotations.NotNull;

import quickchatter.utilities.SimpleCallback;
import quickchatter.utilities.TimeValue;

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
