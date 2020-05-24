package quickchatter.network.bluetooth.bluecove.transmission;

import org.jetbrains.annotations.NotNull;

import quickchatter.network.basic.TransmissionMessagePart;
import quickchatter.network.basic.TransmissionType;
import quickchatter.network.bluetooth.bluecove.BCConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import quickchatter.network.bluetooth.bluecove.segment.BCTransmissionMessageSegment;

/// Builds message parts from bytes.
public class BCTransmissionMessagePartBuilder {
    public static final @NotNull TransmissionType PING_TYPE = BCConstants.getShared().TYPE_PING;

    public final @NotNull TransmissionType type;

    public BCTransmissionMessagePartBuilder(@NotNull TransmissionType type) {
        this.type = type;
    }

    public @NotNull TransmissionMessagePart.Ping buildPing() {
        return new BDTransmissionMessagePartStandardPing(PING_TYPE);
    }

    public @NotNull TransmissionMessagePart.Start buildStart(int expectedLength, int partsCount) {
        return new BDTransmissionMessagePartStandardStart(type, expectedLength, partsCount);
    }

    public @NotNull TransmissionMessagePart.Data buildData(@NotNull byte[] data, int partIndex, int partsCount) {
        return new BDTransmissionMessagePartStandardData(type, data, partsCount, partIndex);
    }

    public @NotNull TransmissionMessagePart.Data buildDataFromList(@NotNull List<TransmissionMessagePart.Data> data) {
        ArrayList<byte[]> items = new ArrayList<>();

        for (TransmissionMessagePart.Data item : data) {
            items.add(item.getData());
        }

        return buildDataFromBytesList(items);
    }

    public @NotNull TransmissionMessagePart.Data buildDataFromBytesList(@NotNull List<byte[]> data) {
        int length = 0;

        for (byte[] element : data) {
            length += element.length;
        }

        if (length == 0) {
            return buildData(new byte[0], 0, 0);
        }

        byte[] allBytes = new byte[0];

        for (byte[] element : data) {
            allBytes = addAllBytes(allBytes, element);
        }

        return new BDTransmissionMessagePartStandardData(type, allBytes, 1, 0);
    }

    public @NotNull TransmissionMessagePart.End buildEnd(int partsCount) {
        return new BDTransmissionMessagePartStandardEnd(type, partsCount);
    }

    public @NotNull List<TransmissionMessagePart> buildAllMessagePartsFromBuffer(@NotNull byte[] bytes) {
        int chunkSize = BCTransmissionMessageSegment.MESSAGE_CHUNK_MAX_SIZE;

        int length = bytes.length;

        int partsCount = length / chunkSize;

        ArrayList<TransmissionMessagePart> messages = new ArrayList<>();

        int position = 0;

        // First and middle chunks
        while (position + chunkSize < length) {
            int next = position + chunkSize;

            TransmissionMessagePart data = buildData(Arrays.copyOfRange(bytes, position, next), messages.size(), partsCount);
            messages.add(data);

            position = next;
        }

        // Last data chunk
        TransmissionMessagePart data = buildData(Arrays.copyOfRange(bytes, position, length), messages.size(), partsCount);
        messages.add(data);

        // Append end
        TransmissionMessagePart start = buildStart(length, partsCount);
        TransmissionMessagePart end = buildEnd(partsCount);
        messages.add(0, start);
        messages.add(end);

        return messages;
    }

    private static byte[] addAllBytes(final byte[] a, byte[] b) {
        byte[] joinedArray = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, joinedArray, a.length, b.length);
        return joinedArray;
    }
}

class BDTransmissionMessagePartStandardPing implements TransmissionMessagePart.Ping {
    final @NotNull
    TransmissionType type;

    public BDTransmissionMessagePartStandardPing(@NotNull TransmissionType type) {
        this.type = type;
    }

    // # TransmissionMessagePart.Ping

    @Override
    public @NotNull
    TransmissionType getType() {
        return type;
    }

    @Override
    public int partIndex() {
        return 0;
    }

    @Override
    public int partsCount() {
        return 0;
    }
}

class BDTransmissionMessagePartStandardStart implements TransmissionMessagePart.Start {
    final @NotNull
    TransmissionType type;
    final int expectedLength;
    final int partsCount;

    public BDTransmissionMessagePartStandardStart(@NotNull TransmissionType type, int expectedLength, int partsCount) {
        this.type = type;
        this.expectedLength = expectedLength;
        this.partsCount = partsCount;
    }

    // # TransmissionMessagePart.Start

    @Override
    public @NotNull
    TransmissionType getType() {
        return type;
    }

    @Override
    public int expectedLength() {
        return expectedLength;
    }

    @Override
    public int partIndex() {
        return 0;
    }

    @Override
    public int partsCount() {
        return partsCount;
    }
}

class BDTransmissionMessagePartStandardData implements TransmissionMessagePart.Data {
    final @NotNull
    TransmissionType type;
    final @NotNull byte[] data;
    final int partsCount;
    final int partIndex;

    public BDTransmissionMessagePartStandardData(@NotNull TransmissionType type, @NotNull byte[] data, int partIndex, int partsCount) {
        this.type = type;
        this.data = data;
        this.partsCount = partsCount;
        this.partIndex = partIndex;
    }

    // # TransmissionMessagePart.Data

    @Override
    public @NotNull
    TransmissionType getType() {
        return type;
    }

    @Override
    public @NotNull byte[] getData() {
        return data;
    }

    @Override
    public int partIndex() {
        return partIndex;
    }

    @Override
    public int partsCount() {
        return partsCount;
    }
}

class BDTransmissionMessagePartStandardEnd implements TransmissionMessagePart.End {
    final @NotNull
    TransmissionType type;
    final int partsCount;

    public BDTransmissionMessagePartStandardEnd(@NotNull TransmissionType type, int partsCount) {
        this.type = type;
        this.partsCount = partsCount;
    }

    // # TransmissionMessagePart.End

    @Override
    public @NotNull
    TransmissionType getType() {
        return type;
    }

    @Override
    public int partIndex() {
        return partsCount;
    }

    @Override
    public int partsCount() {
        return partsCount;
    }
}
