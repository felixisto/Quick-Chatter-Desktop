package quickchatter.network.bluetooth.bluecove.transmission;

import org.jetbrains.annotations.NotNull;

import quickchatter.network.bluetooth.bluecove.segment.BDTransmissionMessageSegment;
import quickchatter.network.bluetooth.bluecove.segment.BDTransmissionMessageSegmentOutput;
import quickchatter.network.basic.StreamBandwidth;
import quickchatter.network.basic.TransmissionLine;
import quickchatter.network.basic.TransmissionMessagePart;
import quickchatter.network.basic.TransmissionType;
import quickchatter.network.basic.TransmissionWriteStream;
import quickchatter.utilities.Logger;

import java.util.List;

/// Writes data to a TransmissionWriteStream.
public class BDTransmissionWriteLine implements TransmissionLine.Output {
    public static final boolean DEBUG_LOG = true;

    public final @NotNull TransmissionType type;

    private final @NotNull TransmissionWriteStream _stream;

    public static @NotNull BDTransmissionWriteLine build(@NotNull TransmissionWriteStream stream, @NotNull TransmissionType type) {
        return new BDTransmissionWriteLine(stream, type);
    }

    BDTransmissionWriteLine(@NotNull TransmissionWriteStream stream, @NotNull TransmissionType type) {
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
            byte[] data = BDTransmissionMessageSegmentOutput.build(message).bytes;

            if (DEBUG_LOG) {
                if (data.length <= 64) {
                    log("Writing: " + BDTransmissionMessageSegment.bytesToString(data));
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

