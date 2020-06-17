package network.bluetooth.bluecove.transmission;

import org.jetbrains.annotations.NotNull;

import network.basic.PingStatusChecker;
import network.basic.TransmissionMessage;
import network.basic.TransmissionMessagePart;
import network.bluetooth.bluecove.BCConstants;
import utilities.Callback;
import utilities.TimeValue;
import utilities.Timer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.Nullable;

/// Transmits ping messages when no data is sent.
public class BCTransmitterPinger extends BCTransmitterPerformer implements PingStatusChecker {
    private final @NotNull TimeValue _delay;

    private final @NotNull BCTransmissionMessagePartBuilder _builder;

    // Used to tell if other side is sending us data.
    private final @NotNull AtomicLong _numberOfBytesRead = new AtomicLong(0);

    // Used to tell if we are sending data. If no data is sent, a ping message will be build and transmitted.
    private final @NotNull AtomicLong _numberOfBytesWritten = new AtomicLong(0);

    private final @NotNull AtomicReference<Timer> _lastReceivedPingTimer = new AtomicReference<>();

    private final @NotNull AtomicReference<Timer> _lastSentPingTimer = new AtomicReference<>();

    public BCTransmitterPinger(@NotNull BCTransmissionLine line, @NotNull TimeValue delay, @Nullable BCTransmitterPerformerDelegate delegate) {
        super(line, delegate);

        _delay = delay;

        _builder = new BCTransmissionMessagePartBuilder(line.getType());

        refreshLastReceivedPing();
        refreshLastSentPing();
    }

    // # BDTransmitterPerformer

    @Override
    public void transmit(@NotNull byte[] bytes) {
        super.transmit(bytes);

        refreshLastSentPing();
    }

    @Override
    public @NotNull List<TransmissionMessagePart> buildMessagePartsFromBuffer(@NotNull byte[] bytes) {
        ArrayList<TransmissionMessagePart> list = new ArrayList<>();

        if (!hasNewDataBeenTransmittedSinceLastUpdate()) {
            list.add(_builder.buildPing());
        }

        return list;
    }

    @Override
    public void readNewMessages(final @NotNull Callback<List<TransmissionMessage>> completion) {
        super.readNewMessages(new Callback<List<TransmissionMessage>>() {
            @Override
            public void perform(List<TransmissionMessage> messages) {
                if (hasNewPingArrivedSinceLastUpdate()) {
                    refreshLastReceivedPing();
                }

                completion.perform(messages);
            }
        });
    }

    public void refreshLastReceivedPing() {
        _lastReceivedPingTimer.set(new Timer(_delay, new Date()));
        _numberOfBytesRead.set(line.getInput().getNumberOfBytesTransmitted());
    }

    public void refreshLastSentPing() {
        _lastSentPingTimer.set(new Timer(_delay, new Date()));
        _numberOfBytesWritten.set(line.getOutput().getNumberOfBytesTransmitted());
    }

    // # PingStatusChecker

    @Override
    public @NotNull TimeValue timeElapsedSinceLastPing() {
        return _lastReceivedPingTimer.get().timeElapsedSinceNow();
    }

    @Override
    public boolean isConnectionTimeout() {
        return timeElapsedSinceLastPing().inMS() >= BCConstants.CONNECTION_TIMEOUT.inMS();
    }

    // # Internals

    private boolean hasNewPingArrivedSinceLastUpdate() {
        long bytesRead = line.getInput().getNumberOfBytesTransmitted();
        return _numberOfBytesRead.get() != bytesRead;
    }

    private boolean hasNewDataBeenTransmittedSinceLastUpdate() {
        long bytesWritten = line.getOutput().getNumberOfBytesTransmitted();
        return _numberOfBytesWritten.get() != bytesWritten;
    }
}
