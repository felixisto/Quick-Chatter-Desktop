/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.bluetooth.bluecove.other;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import network.basic.StreamBandwidth;
import utilities.DataSize;
import utilities.SafeMutableArray;
import utilities.TimeValue;
import utilities.Timer;

public class BCStandardReadWriteBandwidthMonitor extends BCStandardReadWriteBandwidth implements StreamBandwidth.Tracker.Monitor {
    public final int ESTIMATED_VALUES_CAPACITY = 100;
    public final @NotNull TimeValue DELAY = TimeValue.buildSeconds(30);

    private final @NotNull SafeMutableArray<ProcessEntry> _estimatedRateValues = new SafeMutableArray<>();
    private @NotNull AtomicReference<DataSize> _estimatedCurrentRate = new AtomicReference<>(DataSize.zero());

    public BCStandardReadWriteBandwidthMonitor() {
        this(DEFAULT_FLUSH_RATE, DEFAULT_FORCE_FLUSH_TIME);
    }

    public BCStandardReadWriteBandwidthMonitor(@NotNull DataSize flushDataRate) {
        this(flushDataRate, DEFAULT_FORCE_FLUSH_TIME);
    }

    public BCStandardReadWriteBandwidthMonitor(@NotNull DataSize flushDataRate, @NotNull TimeValue forceFlushTime) {
        super(flushDataRate, forceFlushTime);
    }

    // # BDStandardReadWriteBandwidth

    @Override
    public void read(int length) {
        super.read(length);
        updateEstimatedRate(length, new Date());
    }

    @Override
    public void write(int length) {
        super.write(length);
        updateEstimatedRate(length, new Date());
    }

    // # StreamBandwidth.Tracker.Monitor

    @Override
    public @NotNull DataSize getEstimatedCurrentRate() {
        return _estimatedCurrentRate.get();
    }

    // # Internals

    private synchronized void updateEstimatedRate(int length, @NotNull Date now) {
        nullifyOutdatedRates(now);
        updateNewEstimatedRate(length, now);

        _estimatedCurrentRate.set(estimatedRateFromCurrentValues());
    }

    private void nullifyOutdatedRates(@NotNull Date now) {
        for (ProcessEntry entry : _estimatedRateValues.copyData()) {
            if (new Timer(DELAY).timeElapsedSince(now).inMS() > DELAY.inMS()) {
                entry.value = 0;
            }
        }
    }

    private void updateNewEstimatedRate(int length, @NotNull Date now) {
        if (_estimatedRateValues.size() < ESTIMATED_VALUES_CAPACITY) {
            _estimatedRateValues.add(new ProcessEntry(now, length));
            return;
        }

        ProcessEntry newLastEntry = _estimatedRateValues.get(0);
        _estimatedRateValues.removeAt(0);
        _estimatedRateValues.add(newLastEntry);

        newLastEntry.date = now;
        newLastEntry.value = length;
    }

    private @NotNull DataSize estimatedRateFromCurrentValues() {
        int totalRate = 0;

        for (ProcessEntry entry : _estimatedRateValues.copyData()) {
            totalRate += entry.value;
        }

        return DataSize.buildBytes(totalRate / _estimatedRateValues.size());
    }
}

