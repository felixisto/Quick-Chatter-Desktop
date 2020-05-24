/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.other;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import quickchatter.network.basic.StreamBandwidth;
import quickchatter.utilities.DataSize;
import quickchatter.utilities.TimeValue;

public class BCStandardReadWriteBandwidth implements StreamBandwidth.Tracker.Read, StreamBandwidth.Tracker.Write, StreamBandwidth.Boostable {
    public static final @NotNull DataSize DEFAULT_FLUSH_RATE = DataSize.buildBytes(128);
    public static final @NotNull TimeValue DEFAULT_FORCE_FLUSH_TIME = TimeValue.buildSeconds(1);

    private final @NotNull Object lock = new Object();

    public final @NotNull DataSize _flushDataRate;
    public final @NotNull TimeValue _forceFlushTime;
    private final @NotNull AtomicReference<DataSize> _currentFlushDataRate = new AtomicReference<DataSize>();
    private final @NotNull AtomicReference<Double> _maxRateBoost = new AtomicReference<>(1.0);

    public BCStandardReadWriteBandwidth() {
        this(DEFAULT_FLUSH_RATE, DEFAULT_FORCE_FLUSH_TIME);
    }

    public BCStandardReadWriteBandwidth(@NotNull DataSize flushDataRate) {
        this(flushDataRate, DEFAULT_FORCE_FLUSH_TIME);
    }

    public BCStandardReadWriteBandwidth(@NotNull DataSize flushDataRate, @NotNull TimeValue forceFlushTime) {
        _flushDataRate = flushDataRate;
        _forceFlushTime = forceFlushTime;
        _currentFlushDataRate.set(_flushDataRate);
    }

    // # StreamBandwidth.Tracker.Read

    @Override
    public @NotNull DataSize getFlushDataRate() {
        return _currentFlushDataRate.get();
    }

    @Override
    public @NotNull TimeValue getForceFlushTime() {
        return _forceFlushTime;
    }

    @Override
    public void read(int length) {
        process(length);
    }

    // # StreamBandwidth.Tracker.Write

    @Override
    public void write(int length) {
        process(length);
    }

    // # StreamBandwidth.Boostable

    @Override
    public void boostFlushRate(double multiplier) {
        synchronized (lock) {
            if (multiplier >= 0) {
                _maxRateBoost.set(multiplier);
                _currentFlushDataRate.set(DataSize.buildBytes((int)(_flushDataRate.inBytes() * _maxRateBoost.get())));
            }
        }
    }

    @Override
    public void revertBoost() {
        _maxRateBoost.set(1.0);
    }

    // # Internals

    private void updateCurrentMaxRate() {
        process(0);
    }

    private synchronized void process(int length) {

    }
}

class ProcessEntry {
    @NotNull Date date;
    int value;

    ProcessEntry(@NotNull Date date, int value) {
        this.date = date;
        this.value = value;
    }
}

