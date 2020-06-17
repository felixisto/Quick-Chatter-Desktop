package network.bluetooth.bluecove.transmission;

import org.jetbrains.annotations.NotNull;

import network.basic.TransmissionMessage;
import network.basic.TransmissionMessagePart;
import network.basic.TransmissionType;

/// Full transmission message.
public class BCTransmissionMessage implements TransmissionMessage {
    public static final @NotNull byte[] EMPTY_BYTES = new byte[0];

    public final @NotNull
    TransmissionType type;
    public final @NotNull byte[] bytes;

    public BCTransmissionMessage(@NotNull TransmissionType type) {
        this(type, EMPTY_BYTES);
    }

    public BCTransmissionMessage(@NotNull TransmissionType type, @NotNull byte[] bytes) {
        this.type = type;
        this.bytes = bytes;
    }

    public BCTransmissionMessage(@NotNull TransmissionMessagePart.Data message) {
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
