package quickchatter.network.bluetooth.bluecove.transmission;

import org.jetbrains.annotations.NotNull;

import quickchatter.network.basic.TransmissionMessage;
import quickchatter.network.basic.TransmissionMessagePart;
import quickchatter.network.basic.TransmissionType;

/// Full transmission message.
public class BDTransmissionMessage implements TransmissionMessage {
    public static final @NotNull byte[] EMPTY_BYTES = new byte[0];

    public final @NotNull
    TransmissionType type;
    public final @NotNull byte[] bytes;

    public BDTransmissionMessage(@NotNull TransmissionType type) {
        this(type, EMPTY_BYTES);
    }

    public BDTransmissionMessage(@NotNull TransmissionType type, @NotNull byte[] bytes) {
        this.type = type;
        this.bytes = bytes;
    }

    public BDTransmissionMessage(@NotNull TransmissionMessagePart.Data message) {
        this(message.getType(), message.getData());
    }

    @Override
    public @NotNull
    TransmissionType getType() {
        return type;
    }

    @Override
    public @NotNull byte[] getBytes() {
        return bytes;
    }

    @Override
    public int length() {
        return bytes.length;
    }
}
