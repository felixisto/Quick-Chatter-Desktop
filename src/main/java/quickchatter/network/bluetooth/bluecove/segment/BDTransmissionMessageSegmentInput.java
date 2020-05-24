/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.segment;

/// Provides information about input segment data.

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickchatter.network.basic.TransmissionMessagePart;
import quickchatter.network.basic.TransmissionType;
import quickchatter.network.bluetooth.basic.BEError;
import quickchatter.network.bluetooth.bluecove.transmission.BDTransmissionMessagePartBuilder;

/// Information passed trough streams is broken into segments, this class helps reading those segments.
public class BDTransmissionMessageSegmentInput {
    public final @NotNull byte[] data;
    public final @NotNull String dataAsString;

    public final @NotNull BDTransmissionMessageSegment segment;

    public static @NotNull BDTransmissionMessageSegmentInput build(@NotNull byte[] data) {
        return new BDTransmissionMessageSegmentInput(data);
    }

    BDTransmissionMessageSegmentInput(@NotNull byte[] data) {
        this.data = data;
        this.dataAsString = BDTransmissionMessageSegment.bytesToString(data);
        this.segment = BDTransmissionMessageSegment.build(data);
    }

    public @NotNull BDTransmissionMessageSegment firstSegment() {
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

        BDTransmissionMessagePartBuilder builder = new BDTransmissionMessagePartBuilder(type);

        // > Ping
        if (type.equals(BDTransmissionMessagePartBuilder.PING_TYPE)) {
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
