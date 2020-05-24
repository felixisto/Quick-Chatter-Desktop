package quickchatter.network.bluetooth.bluecove.transmission;

import org.jetbrains.annotations.NotNull;

import quickchatter.utilities.Copyable;
import quickchatter.utilities.TimeValue;

import java.util.concurrent.atomic.AtomicBoolean;

public class BDTransmitterPing implements Copyable<BDTransmitterPing> {
    public final @NotNull TimeValue delay;

    private final @NotNull AtomicBoolean _active = new AtomicBoolean(true);

    public BDTransmitterPing(@NotNull TimeValue delay) {
        this.delay = delay;
    }

    public boolean isActive() {
        return _active.get();
    }

    public void setActive(boolean active) {
        _active.set(active);
    }

    // # Copyable

    @Override
    public BDTransmitterPing copy() {
        BDTransmitterPing ping = new BDTransmitterPing(delay);
        ping._active.set(_active.get());
        return ping;
    }
}
