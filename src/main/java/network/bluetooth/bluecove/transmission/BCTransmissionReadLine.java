package network.bluetooth.bluecove.transmission;

import org.jetbrains.annotations.NotNull;

import network.bluetooth.bluecove.segment.BCTransmissionMessageSegment;
import network.bluetooth.bluecove.segment.BCTransmissionMessageSegmentInput;
import network.basic.StreamBandwidth;
import network.basic.TransmissionLine;
import network.basic.TransmissionMessagePart;
import network.basic.TransmissionReadStream;
import network.basic.TransmissionType;
import utilities.Logger;
import utilities.SafeMutableArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/// Reads data given by a TransmissionReadStream.
/// Information is encapsulated by TransmissionMessagePart.
public class BCTransmissionReadLine implements TransmissionLine.Input {
    public static final boolean DEBUG_LOG = true;

    private final @NotNull Object lock = new Object();

    public final @NotNull TransmissionType type;

    private final @NotNull TransmissionReadStream _stream;

    private final @NotNull SafeMutableArray<TransmissionMessagePart> _currentSegmentParts = new SafeMutableArray<>();
    private final @NotNull AtomicReference<TransmissionMessagePart.Start> _currentSegmentStart = new AtomicReference<>();

    public static @NotNull BCTransmissionReadLine build(@NotNull TransmissionReadStream stream, @NotNull TransmissionType type) {
        return new BCTransmissionReadLine(stream, type);
    }

    BCTransmissionReadLine(@NotNull TransmissionReadStream stream, @NotNull TransmissionType type) {
        this.type = type;
        _stream = stream;
    }

    // # TransmissionLine.Input

    @Override
    public void close() {
        _stream.close();
    }

    @Override
    public @NotNull TransmissionType getType() {
        return type;
    }

    @Override
    public long getNumberOfBytesTransmitted() {
        return _stream.getTotalBytesRead();
    }

    @Override
    public @NotNull StreamBandwidth getReadBandwidth() {
        return _stream.getBandwidth();
    }

    @Override
    public int currentReadingSegmentExpectedLength() {
        TransmissionMessagePart.Start start = _currentSegmentStart.get();

        return start != null ? start.expectedLength() : 0;
    }

    @Override
    public void readNewData() throws Exception {
        synchronized (lock) {
            _stream.read();
            read();
        }
    }

    @Override
    public @NotNull List<TransmissionMessagePart> readBuffer() {
        return _currentSegmentParts.copyData();
    }

    @Override
    public @NotNull List<TransmissionMessagePart> clearDataUntilFinalEndPart() {
        List<TransmissionMessagePart> parts;
        ArrayList<TransmissionMessagePart> leftoverParts = new ArrayList<>();

        synchronized (lock) {
            parts = _currentSegmentParts.copyData();

            int indexOfFinalEndPart = -1;

            for (int e = parts.size() - 1; e >= 0; e--) {
                TransmissionMessagePart part = parts.get(e);

                if (part instanceof TransmissionMessagePart.End || part instanceof TransmissionMessagePart.Ping) {
                    indexOfFinalEndPart = e;
                    break;
                }
            }

            if (indexOfFinalEndPart >= 0) {
                for (int e = indexOfFinalEndPart + 1; e < parts.size(); e++) {
                    leftoverParts.add(parts.get(e));
                }

                _currentSegmentParts.removeAll();
                _currentSegmentParts.addAll(leftoverParts);
            }
        }

        return parts;
    }

    // # Internals

    private void read() {
        // Try to create as many messages as possible.
        byte[] bytes = _stream.getBuffer();

        if (DEBUG_LOG) {
            if (bytes.length <= 256) {
                log("Reading: " + BCTransmissionMessageSegment.bytesToString(bytes));
            } else {
                log("Reading more than 64 bytes");
            }
        }

        while (bytes.length > 0) {
            BCTransmissionMessageSegmentInput streamData = BCTransmissionMessageSegmentInput.build(bytes);

            if (!streamData.firstSegment().isValid()) {
                log("Read data does not contain a full data segment, yet. Waiting for more data to arrive.");
                break;
            }

            int segmentLength = streamData.firstSegment().length();
            int partsCount = estimatedCurrentSegmentTotalCount();
            int partIndex = _currentSegmentParts.size();

            log("Reading data segment with length " + segmentLength + " bytes");

            try {
                TransmissionMessagePart part = streamData.buildFirstPart(getType(), partIndex, partsCount);

                // Handle success build - part may be null
                if (part != null) {
                    _stream.clearBufferUntilEndIndex(segmentLength);
                    bytes = _stream.getBuffer();

                    _currentSegmentParts.add(part);

                    if (part instanceof TransmissionMessagePart.Ping) {
                        return;
                    }

                    if (part instanceof TransmissionMessagePart.Start) {
                        _currentSegmentStart.set((TransmissionMessagePart.Start) part);
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                // Handle failure build - corrupted data
                Logger.error(this, "Failed to build stream data, probably corrupted, error: " + e);

                // There is no point of keeping the current buffer, discard it and move to next segment
                _stream.clearBufferUntilEndIndex(segmentLength);

                // We could loop again, but to be safe, interrupt instead
                break;
            }
        }
    }

    private int maxChunkSizeSegment() {
        return BCTransmissionMessageSegment.MESSAGE_CHUNK_MAX_SIZE;
    }

    private int estimatedCurrentSegmentTotalCount() {
        int expectedLength = currentReadingSegmentExpectedLength();
        int chunkSize = maxChunkSizeSegment();

        if (expectedLength <= 0 || chunkSize <= 0) {
            return 0;
        }

        if (expectedLength < chunkSize) {
            return 3;
        }

        return expectedLength / chunkSize;
    }

    void log(@NotNull String message) {
        if (!DEBUG_LOG) {
            return;
        }

        Logger.message(this, message);
    }
}
