package quickchatter.network.bluetooth.bluecove.transmission;

import org.jetbrains.annotations.NotNull;

import quickchatter.network.bluetooth.bluecove.segment.BCTransmissionMessageSegment;
import quickchatter.network.bluetooth.bluecove.segment.BCTransmissionMessageSegmentOutput;
import quickchatter.network.basic.StreamBandwidth;
import quickchatter.network.basic.TransmissionLine;
import quickchatter.network.basic.TransmissionMessagePart;
import quickchatter.network.basic.TransmissionType;
import quickchatter.network.basic.TransmissionWriteStream;
import quickchatter.utilities.Logger;

import java.util.List;

/// Writes data to a TransmissionWriteStream.
public class BCTransmissionWriteLine implements TransmissionLine.Output {
    public static final boolean DEBUG_LOG = true;

    public final @NotNull TransmissionType type;

    private final @NotNull TransmissionWriteStream _stream;

    public static @NotNull BCTransmissionWriteLine build(@NotNull TransmissionWriteStream stream, @NotNull TransmissionType type) {
        return new BCTransmissionWriteLine(stream, type);
    }

    BCTransmissionWriteLine(@NotNull TransmissionWriteStream stream, @NotNull TransmissionType type) {
        this.type = type;
        this._stream = stream;
    }

    // # TransmissionLine.Output

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
        return _stream.getTotalBytesWritten();
    }

    @Override
    public @NotNull StreamBandwidth getWriteBandwidth() {
        return _stream.getBandwidth();
    }

    @Override
    public void writeMessages(@NotNull List<TransmissionMessagePart> messages) throws Exception {
        for (int e = 0; e < messages.size(); e++) {
            TransmissionMessagePart message = messages.get(e);
            byte[] data = BCTransmissionMessageSegmentOutput.build(message).bytes;

            if (DEBUG_LOG) {
                if (data.length <= 64) {
                    log("Writing: " + BCTransmissionMessageSegment.bytesToString(data));
                } else {
                    log("Writing more than 64 bytes");
                }
            }

            _stream.write(data);
        }
    }

    // # Internals

    void log(@NotNull String message) {
        if (!DEBUG_LOG) {
            return;
        }

        Logger.message(this, message);
    }
}

