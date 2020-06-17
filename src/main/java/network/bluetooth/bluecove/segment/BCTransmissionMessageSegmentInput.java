/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.bluetooth.bluecove.segment;

/// Provides information about input segment data.

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import network.basic.TransmissionMessagePart;
import network.basic.TransmissionType;
import network.bluetooth.basic.BEError;
import network.bluetooth.bluecove.transmission.BCTransmissionMessagePartBuilder;

/// Information passed trough streams is broken into segments, this class helps reading those segments.
public class BCTransmissionMessageSegmentInput {
    public final @NotNull byte[] data;
    public final @NotNull String dataAsString;

    public final @NotNull BCTransmissionMessageSegment segment;

    public static @NotNull BCTransmissionMessageSegmentInput build(@NotNull byte[] data) {
        return new BCTransmissionMessageSegmentInput(data);
    }

    BCTransmissionMessageSegmentInput(@NotNull byte[] data) {
        this.data = data;
        this.dataAsString = BCTransmissionMessageSegment.bytesToString(data);
        this.segment = BCTransmissionMessageSegment.build(data);
    }

    public @NotNull BCTransmissionMessageSegment firstSegment() {
        return segment;
    }

    public @Nullable TransmissionMessagePart buildFirstPart(@NotNull TransmissionType expectedType, int partIndex, int partsCount) throws BEError {
        int startIndex = segment.startIndex();
        int endIndex = segment.endIndex();

        // Check for corruption
        if (startIndex < 0 || endIndex < 0) {
            return null;
        }

        TransmissionType type = segment.getType();

        // Check for corruption
        if (type == null) {
            throw new BEError(BEError.Value.corruptedStreamDataType);
        }

        if (!type.equals(expectedType)) {
            return null;
        }

        BCTransmissionMessagePartBuilder builder = new BCTransmissionMessagePartBuilder(type);

        // > Ping
        if (type.equals(BCTransmissionMessagePartBuilder.PING_TYPE)) {
            return builder.buildPing();
        }

        if (segment.isStartClass()) {
            return builder.buildStart(segment.headerValueAsSizeValue(), partsCount);
        }

        if (segment.isDataClass()) {
            return builder.buildData(segment.value(), partsCount, partIndex);
        }

        if (segment.isEndClass()) {
            return builder.buildEnd(partsCount);
        }

        throw new BEError(BEError.Value.corruptedStreamData);
    }
}
