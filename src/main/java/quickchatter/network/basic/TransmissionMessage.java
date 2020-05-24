package quickchatter.network.basic;

import org.jetbrains.annotations.NotNull;

public interface TransmissionMessage {
    @NotNull TransmissionType getType();

    @NotNull byte[] getBytes();

    int length();
}
