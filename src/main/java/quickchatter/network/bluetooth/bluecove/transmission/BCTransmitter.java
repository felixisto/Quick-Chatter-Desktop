package quickchatter.network.bluetooth.bluecove.transmission;

import org.jetbrains.annotations.NotNull;

import quickchatter.network.bluetooth.basic.BETransmitter;
import quickchatter.network.basic.PingStatusChecker;
import quickchatter.network.basic.TransmissionLine;
import quickchatter.network.basic.TransmissionMessage;
import quickchatter.network.basic.TransmissionReadStream;
import quickchatter.network.basic.TransmissionType;
import quickchatter.network.basic.TransmissionWriteStream;
import quickchatter.network.basic.TransmitterListener;
import quickchatter.network.bluetooth.bluecove.BCConstants;
import quickchatter.utilities.Callback;
import quickchatter.utilities.CollectionUtilities;
import quickchatter.utilities.Errors;
import quickchatter.utilities.Logger;
import quickchatter.utilities.LooperClient;
import quickchatter.utilities.LooperService;
import quickchatter.utilities.SafeMutableArray;
import quickchatter.utilities.TimeValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.Nullable;
import quickchatter.network.bluetooth.basic.BESocket;
import quickchatter.network.bluetooth.bluecove.BCSocket;

/// A component responsible for transmitting and receiving information over one or multiple transmission lines.
public class BCTransmitter implements BETransmitter.ReaderWriter, BETransmitter.Service, LooperClient, BCTransmitterPerformerDelegate {
    private final @NotNull Object lock = new Object();

    public final @NotNull BCSocket socket;

    private final @NotNull List<BCTransmitterPerformer> _performers;
    private BCTransmitterPinger _pinger;

    private final @NotNull AtomicReference<BCTransmitterPing> _ping = new AtomicReference<>();
    private @NotNull Date _lastPingDate = new Date();

    private final @NotNull SafeMutableArray<TransmitterListener> _listeners = new SafeMutableArray<>();
    
    private final @NotNull AtomicBoolean _isActive = new AtomicBoolean(false);

    public BCTransmitter(@NotNull BCSocket socket, @NotNull TransmissionReadStream input, @NotNull TransmissionWriteStream output, @NotNull List<BCTransmissionLine> lines, @NotNull BCTransmitterPing ping) {
        this.socket = socket;

        _performers = new ArrayList<>();
        _ping.set(ping.copy());
        
        // Build and add performers
        lines = CollectionUtilities.copy(lines);

        ArrayList<BCTransmitterPerformer> performers = new ArrayList<>();

        for (BCTransmissionLine line : lines) {
            performers.add(new BCTransmitterPerformer(line, this));
        }

        performers.add(new BCTransmitterPinger(new BCTransmissionLineBuilder(input, output).buildPing(), BCConstants.DEFAULT_PING_DELAY, this));

        stripPerformersOfDuplicates(performers);

        _performers.addAll(performers);

        for (BCTransmitterPerformer performer : _performers) {
            if (performer instanceof BCTransmitterPinger) {
                _pinger = (BCTransmitterPinger) performer;
            }
        }
    }

    public BCTransmitter(@NotNull BCSocket socket, @NotNull TransmissionReadStream input, @NotNull TransmissionWriteStream output, @NotNull List<BCTransmissionLine> lines) {
        this(socket, input, output, lines, new BCTransmitterPing(BCConstants.DEFAULT_PING_DELAY));
    }

    // # Transmitter.ReaderWriter, Transmitter.Service

    @Override
    public @NotNull BESocket getSocket() {
        return this.socket;
    }
    
    @Override
    public void start() throws Exception {
        if (_isActive.getAndSet(true)) {
            Errors.throwCannotStartTwice("Already started");
        }
        
        synchronized (lock) {
            LooperService.getShared().subscribe(this);
        }
    }

    @Override
    public void stop() {
        synchronized (lock) {
            LooperService.getShared().unsubscribe(this);

            for (BCTransmitterPerformer performer : _performers) {
                performer.stop();
            }

            _performers.clear();
            
            _isActive.set(false);
        }
    }

    @Override
    public @NotNull List<TransmissionLine.Input> getInputLines() {
        ArrayList<TransmissionLine.Input> lines = new ArrayList<>();

        for (BCTransmitterPerformer l : _performers) {
            lines.add(l.line.getInput());
        }

        return lines;
    }

    @Override
    public @NotNull List<TransmissionLine.Output> getOutputLines() {
        ArrayList<TransmissionLine.Output> lines = new ArrayList<>();

        for (BCTransmitterPerformer l : _performers) {
            lines.add(l.line.getOutput());
        }

        return lines;
    }

    @Override
    public boolean isPingActive() {
        return _ping.get().isActive();
    }

    @Override
    public @NotNull TimeValue getPingDelay() {
        return _ping.get().delay;
    }

    @Override
    public void setPingDelay(@NotNull TimeValue delay) {
        synchronized (lock) {
            BCTransmitterPing ping = new BCTransmitterPing(delay);
            ping.setActive(_ping.get().isActive());
            _ping.set(ping);
        }
    }

    @Override
    public void activatePing() {
        synchronized (lock) {
            _ping.get().setActive(true);
        }
    }

    @Override
    public void deactivatePing() {
        synchronized (lock) {
            _ping.get().setActive(false);
        }
    }

    @Override
    public void sendMessage(@NotNull TransmissionMessage message) throws Exception {
        TransmissionType type = message.getType();
        BCTransmitterPerformer stream = getStream(type);

        if (stream == null) {
            Errors.throwUnsupportedOperation("Transmission line '" + type.value + "' does not exist");
        }

        Logger.message(this, "Transmit message of type '" + type.value + "' length = " + message.length());

        stream.transmit(message.getBytes());
    }

    @Override
    public void readNewMessages(@NotNull TransmissionType type, final @NotNull Callback<List<TransmissionMessage>> completion) throws Exception {
        BCTransmitterPerformer stream = getStream(type);

        if (stream == null) {
            Errors.throwUnsupportedOperation("Transmission line '" + type.value + "' does not exist");
        }

        stream.readNewMessages(completion);
    }

    @Override
    public void readAllNewMessages() {
        for (BCTransmitterPerformer stream : _performers) {
            stream.readNewMessages(new Callback<List<TransmissionMessage>>() {
                @Override
                public void perform(List<TransmissionMessage> argument) {

                }
            });
        }
    }

    @Override
    public @NotNull PingStatusChecker getPingStatusChecker() {
        return _pinger;
    }

    @Override
    public void subscribe(@NotNull TransmitterListener listener) {
        _listeners.add(listener);
    }

    @Override
    public void unsubscribe(@NotNull TransmitterListener listener) {
        _listeners.remove(listener);
    }

    // # LooperClient

    @Override
    public void loop() {
        pingUpdateIfNecessary();
    }

    // # BDTransmitterPerformerDelegate

    @Override
    public void onMessageReceived(final @NotNull TransmissionType type, @NotNull final TransmissionMessage message) {
        Logger.message(this, "Received message of type '" + type.value + "'");

        _listeners.perform(new Callback<TransmitterListener>() {
            @Override
            public void perform(TransmitterListener element) {
                element.onMessageReceived(type, message);
            }
        });
    }

    @Override
    public void onMessageDataChunkReceived(final @NotNull TransmissionType type, final double progress) {
        _listeners.perform(new Callback<TransmitterListener>() {
            @Override
            public void perform(TransmitterListener listener) {
                listener.onMessageDataChunkReceived(type, progress);
            }
        });
    }

    @Override
    public void onMessageDataChunkSent(final @NotNull TransmissionType type, final double progress) {
        _listeners.perform(new Callback<TransmitterListener>() {
            @Override
            public void perform(TransmitterListener listener) {
                listener.onMessageDataChunkSent(type, progress);
            }
        });
    }

    @Override
    public void onMessageFullySent(final @NotNull TransmissionType type) {
        Logger.message(this, "Sent message of type '" + type.value + "'");

        _listeners.perform(new Callback<TransmitterListener>() {
            @Override
            public void perform(TransmitterListener element) {
                element.onMessageFullySent(type);
            }
        });
    }

    @Override
    public void onMessageFailedOrCancelled(final @NotNull TransmissionType type) {
        Logger.message(this, "Message failed or cancelled of type '" + type.value + "'");

        _listeners.perform(new Callback<TransmitterListener>() {
            @Override
            public void perform(TransmitterListener element) {
                element.onMessageFailedOrCancelled(type);
            }
        });
    }

    // # Internals

    private @Nullable BCTransmitterPerformer getStream(@NotNull TransmissionType type) {
        BCTransmitterPerformer line = null;

        for (BCTransmitterPerformer l : _performers) {
            if (l.getType().equals(type)) {
                line = l;
                break;
            }
        }

        return line;
    }

    private void pingUpdateIfNecessary() {
        if (!_ping.get().isActive()) {
            return;
        }

        if (timeSinceLastPing().inMS() > _ping.get().delay.inMS()) {
            updatePingDate();
            pingUpdate();
        }
    }

    private void pingUpdate() {
        byte[] noBytes = new byte[0];

        try {
            BCTransmitterPerformer stream = getStream(BCConstants.getShared().TYPE_PING);

            if (stream != null) {
                stream.transmit(noBytes);
            }
        } catch (Exception e) {

        }
    }

    private void updatePingDate() {
        _lastPingDate = new Date();
    }

    private @NotNull TimeValue timeSinceLastPing() {
        Date now = new Date();
        long time = now.getTime() - _lastPingDate.getTime();
        return TimeValue.buildMS((int) time);
    }

    // # Utilities

    public static @NotNull List<BCTransmitterPerformer> stripPerformersOfDuplicates(@NotNull List<BCTransmitterPerformer> performers) {
        ArrayList<BCTransmitterPerformer> result = new ArrayList<>();

        for (BCTransmitterPerformer line : CollectionUtilities.copy(performers)) {
            boolean alreadyPresent = false;

            for (BCTransmitterPerformer resultLine : result) {
                if (line.getType().equals(resultLine.getType())) {
                    alreadyPresent = true;
                    break;
                }
            }

            if (!alreadyPresent) {
                result.add(line);
            }
        }

        return result;
    }
}
