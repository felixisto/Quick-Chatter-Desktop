package quickchatter.network.bluetooth.bluecove.transmission;

import org.jetbrains.annotations.NotNull;

import quickchatter.network.basic.StreamBandwidth;
import quickchatter.network.basic.TransmissionMessage;
import quickchatter.network.basic.TransmissionMessagePart;
import quickchatter.network.basic.TransmissionType;
import quickchatter.utilities.Callback;
import quickchatter.utilities.CollectionUtilities;
import quickchatter.utilities.Errors;
import quickchatter.utilities.Logger;
import quickchatter.utilities.LooperClient;
import quickchatter.utilities.LooperService;
import quickchatter.utilities.SimpleCallback;
import quickchatter.utilities.TimeValue;
import quickchatter.utilities.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.Nullable;

/// Wraps the functionality of BDTransmissionLine, and additionally provides a queue system for sending messages.
/// Multiple messages can be send, which are transmitted one by one in the same order they were added.
/// The read functionality is extended, by providing construction process of the transmission messages.
public class BDTransmitterPerformer implements LooperClient {
    private final @NotNull Object lock = new Object();

    public final @NotNull BDTransmissionLine line;
    private final @Nullable BDTransmitterPerformerDelegate _delegate;

    private final @NotNull AtomicBoolean _readingActive = new AtomicBoolean(true);
    private final @NotNull AtomicBoolean _writingActive = new AtomicBoolean(true);

    private final @NotNull ArrayList<QueuedMessage> _queuedMessages = new ArrayList<>();

    private final @NotNull BDTransmissionMessagePartBuilder _partsBuilder;

    private final @NotNull Timer _resetTimer = new Timer(TimeValue.buildSeconds(1));

    public static @NotNull BDTransmitterPerformer build(@NotNull BDTransmissionLine line, BDTransmitterPerformerDelegate delegate) {
        return new BDTransmitterPerformer(line, delegate);
    }

    BDTransmitterPerformer(@NotNull BDTransmissionLine line, @Nullable BDTransmitterPerformerDelegate delegate) {
        this.line = line;
        this._partsBuilder = new BDTransmissionMessagePartBuilder(line.getType());
        this._delegate = delegate;

        subscribeToLooperService();
    }

    // # Properties

    public @NotNull TransmissionType getType() {
        return line.getType();
    }

    public @NotNull StreamBandwidth getReadBandwidth() {
        return line.getReadBandwidth();
    }

    public @NotNull StreamBandwidth getWriteBandwidth() {
        return line.getWriteBandwidth();
    }

    // # Operations

    public void stop() {
        synchronized (lock) {
            cancelAllMessages();
            stopReading();
            stopWriting();
            line.close();
            unsubscribeToLooperService();
        }
    }

    public boolean isWritingActive() {
        return _writingActive.get();
    }

    public void startWriting() {
        if (!_writingActive.getAndSet(true)) {
            if (!isReadingActive()) {
                subscribeToLooperService();
            }
        }
    }

    public void stopWriting() {
        if (_writingActive.getAndSet(true)) {
            if (!isReadingActive()) {
                unsubscribeToLooperService();
            }
        }
    }

    public boolean isReadingActive() {
        return _readingActive.get();
    }

    public void startReading() {
        if (!_readingActive.getAndSet(true)) {
            if (!isWritingActive()) {
                subscribeToLooperService();
            }
        }
    }

    public void stopReading() {
        if (_readingActive.getAndSet(true)) {
            if (!isWritingActive()) {
                unsubscribeToLooperService();
            }
        }
    }

    public @NotNull List<TransmissionMessagePart> buildMessagePartsFromBuffer(@NotNull byte[] bytes) {
        return _partsBuilder.buildAllMessagePartsFromBuffer(bytes);
    }

    public void transmit(@NotNull byte[] bytes) {
        synchronized (lock) {
            try {
                QueuedMessage message = new QueuedMessage(buildMessagePartsFromBuffer(bytes), bytes.length);
                _queuedMessages.add(message);
            } catch (Exception e) {

            }
        }
    }

    public void readNewMessages(final @NotNull Callback<List<TransmissionMessage>> completion) {
        LooperService.getShared().asyncInBackground(new SimpleCallback() {
            @Override
            public void perform() {
                completion.perform(readNewDataAndAlertDelegate());
            }
        });
    }

    // # LooperClient

    @Override
    public void loop() {
        LooperService.getShared().asyncInBackground(new SimpleCallback() {
            @Override
            public void perform() {
                synchronized (lock) {
                    updateReading();
                    updateWriting();
                }
            }
        });
    }

    // # Internals

    private void subscribeToLooperService() {
        LooperService.getShared().subscribe(this);
    }

    private void unsubscribeToLooperService() {
        LooperService.getShared().unsubscribe(this);
    }

    private void updateReading() {
        if (!isReadingActive()) {
            return;
        }

        if (_resetTimer.update()) {
            readNewMessages(new Callback<List<TransmissionMessage>>() {
                @Override
                public void perform(List<TransmissionMessage> argument) {

                }
            });
        }
    }

    private void updateWriting() {
        if (!isWritingActive()) {
            return;
        }

        transmitNextParts(_queuedMessages);

        List<QueuedMessage> queuedMessages = CollectionUtilities.copy(_queuedMessages);
        _queuedMessages.clear();
        _queuedMessages.addAll(clearFullySentMessages(queuedMessages));
    }

    private void transmitNextParts(@NotNull List<QueuedMessage> messages) {
        if (messages.isEmpty()) {
            return;
        }

        try {
            NextParts nextParts = pickNextParts(messages);

            line.write.writeMessages(nextParts.messageParts);

            // Alert delegate
            if (_delegate != null) {
                for (QueuedMessagePart part : nextParts.parts) {
                    if (part.isEnd()) {
                        _delegate.onMessageFullySent(part.getType());
                    } else {
                        _delegate.onMessageDataChunkSent(part.getType(), part.getProgress());
                    }
                }
            }
        } catch (Exception e) {
            Logger.error(this, "Failed to write message to line, error: " + e);
        }
    }

    private @NotNull NextParts pickNextParts(@NotNull List<QueuedMessage> messages) {
        ArrayList<QueuedMessagePart> parts = new ArrayList<>();
        ArrayList<TransmissionMessagePart> messageParts = new ArrayList<>();

        for (QueuedMessage message : messages) {
            int index = message.currentPartIndex;
            TransmissionMessagePart next = message.next();

            if (next != null) {
                parts.add(new QueuedMessagePart(next, index, message.getPartCount()));
                messageParts.add(next);
            }
        }

        return new NextParts(parts, messageParts);
    }

    private @NotNull List<QueuedMessage> clearFullySentMessages(@NotNull List<QueuedMessage> messages) {
        ArrayList<QueuedMessage> result = new ArrayList<>();

        for (QueuedMessage message : messages) {
            if (!message.isFullySent()) {
                result.add(message);
            }
        }

        return result;
    }

    private @NotNull List<TransmissionMessage> readNewDataAndAlertDelegate() {
        TransmissionType type = getType();

        try {
            // Update
            line.read.readNewData();

            List<TransmissionMessagePart> parts = line.read.clearDataUntilFinalEndPart();
            BuiltMessagesResult builtMessages = buildFullySentMessagesFromParts(parts);
            List<TransmissionMessage> messages = builtMessages.messages;

            if (_delegate != null) {
                // Data update
                if (parts.size() > 0) {
                    // Update the last part only
                    TransmissionMessagePart part = parts.get(parts.size()-1);

                    if (!(part instanceof TransmissionMessagePart.Ping)) {
                        double progress = 0;

                        if (part.partsCount() > 0) {
                            double index = part.partIndex();
                            double count = part.partsCount();
                            progress = index / count;
                        }

                        _delegate.onMessageDataChunkReceived(type, progress);
                    }
                }

                // Message failed or cancelled
                for (int e = 0; e < builtMessages.failedMessageCount; e++) {
                    _delegate.onMessageFailedOrCancelled(type);
                }

                // Message received
                for (TransmissionMessage message : messages) {
                    _delegate.onMessageReceived(type, message);
                }
            }

            return messages;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private @NotNull BuiltMessagesResult buildFullySentMessagesFromParts(@NotNull List<TransmissionMessagePart> parts) {
        ArrayList<TransmissionMessagePart> currentMessageParts = new ArrayList<>();
        ArrayList<TransmissionMessage> messages = new ArrayList<>();
        int failedMessageCount = 0;

        for (TransmissionMessagePart part : parts) {
            if (part instanceof TransmissionMessagePart.Ping) {
                messages.add(new BDTransmissionMessage(part.getType()));
            } else {
                currentMessageParts.add(part);

                if (part instanceof TransmissionMessagePart.End) {
                    if (verifyPartsDataIntegrity(currentMessageParts)) {
                        BDTransmissionMessageBuilder builder = new BDTransmissionMessageBuilder(part.getType());
                        TransmissionMessage message = builder.buildFromMessageParts(currentMessageParts);

                        if (message != null) {
                            messages.add(message);
                        }
                    } else {
                        failedMessageCount += 1;
                    }

                    currentMessageParts.clear();
                }
            }
        }

        return new BuiltMessagesResult(messages, failedMessageCount);
    }

    private boolean verifyPartsDataIntegrity(@NotNull List<TransmissionMessagePart> currentMessageParts) {
        if (currentMessageParts.size() < 3) {
            return false;
        }

        if (!(currentMessageParts.get(0) instanceof TransmissionMessagePart.Start)) {
            return false;
        }

        if (!(currentMessageParts.get(currentMessageParts.size()-1) instanceof TransmissionMessagePart.End)) {
            return false;
        }

        TransmissionMessagePart.Start start = (TransmissionMessagePart.Start) currentMessageParts.get(0);

        int expectedLength = start.expectedLength();
        int totalDataLength = 0;

        for (TransmissionMessagePart part : currentMessageParts) {
            if (part instanceof TransmissionMessagePart.Data) {
                totalDataLength += ((TransmissionMessagePart.Data) part).getData().length;
            }
        }

        return expectedLength == totalDataLength;
    }

    private void cancelAllMessages() {
        ArrayList<TransmissionMessagePart> partsToWrite = new ArrayList<>();

        List<QueuedMessage> messages = new ArrayList<>(_queuedMessages);

        for (QueuedMessage message : messages) {
            if (message.parts.size() == 0) {
                continue;
            }

            TransmissionMessagePart last = message.parts.get(message.parts.size()-1);

            if (last instanceof TransmissionMessagePart.End) {
                partsToWrite.add(last);
            }
        }

        try {
            this.line.write.writeMessages(partsToWrite);
        } catch (Exception e) {

        }

        _queuedMessages.clear();
    }
}

class BuiltMessagesResult {
    final @NotNull List<TransmissionMessage> messages;
    final int failedMessageCount;

    BuiltMessagesResult(@NotNull List<TransmissionMessage> messages, int failedMessageCount) {
        this.messages = messages;
        this.failedMessageCount = failedMessageCount;
    }
}

class QueuedMessage {
    int currentPartIndex = 0;

    final @NotNull List<TransmissionMessagePart> parts;
    final int dataSize;

    QueuedMessage(@NotNull List<TransmissionMessagePart> parts, int dataSize) throws Exception {
        if (parts.isEmpty()) {
            Errors.throwInvalidArgument("Message must contain at least one part");
        }

        this.parts = parts;
        this.dataSize = dataSize;
    }

    @Nullable
    TransmissionMessagePart next() {
        if (isFullySent()) {
            return null;
        }

        int index = currentPartIndex;

        currentPartIndex += 1;

        return parts.get(index);
    }

    int getPartCount() {
        return parts.size();
    }

    boolean isFullySent() {
        return currentPartIndex >= parts.size();
    }
}

class QueuedMessagePart {
    final @NotNull
    TransmissionMessagePart part;
    final int index;
    final int messagePartsCount;

    QueuedMessagePart(@NotNull TransmissionMessagePart part, int index, int messagePartsCount) {
        this.part = part;
        this.index = index;
        this.messagePartsCount = messagePartsCount;
    }

    @NotNull
    TransmissionType getType() {
        return part.getType();
    }

    double getProgress() {
        if (messagePartsCount == 0) {
            return 0;
        }

        double dIndex = index;
        double dCount = messagePartsCount;

        return dIndex / dCount;
    }

    boolean isData() {
        return part instanceof TransmissionMessagePart.Data;
    }

    boolean isEnd() {
        return part instanceof TransmissionMessagePart.End;
    }
}

class NextParts {
    final @NotNull List<QueuedMessagePart> parts;
    final @NotNull List<TransmissionMessagePart> messageParts;

    NextParts(@NotNull List<QueuedMessagePart> parts, @NotNull List<TransmissionMessagePart> messageParts) {
        this.parts = parts;
        this.messageParts = messageParts;
    }
}
