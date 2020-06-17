package network.bluetooth.bluecove.transmission;

import org.jetbrains.annotations.NotNull;

import utilities.Copyable;
import utilities.TimeValue;

import java.util.concurrent.atomic.AtomicBoolean;

public class BCTransmitterPing implements Copyable<BCTransmitterPing> {
    public final @NotNull TimeValue delay;

    private final @NotNull AtomicBoolean _active = new AtomicBoolean(true);

    public BCTransmitterPing(@NotNull TimeValue delay) {
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
    public BCTransmitterPing copy() {
        BCTransmitterPing ping = new BCTransmitterPing(delay);
        ping._active.set(_active.get());
        return ping;
    }
}
