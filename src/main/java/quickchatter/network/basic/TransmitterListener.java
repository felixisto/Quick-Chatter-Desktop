package quickchatter.network.basic;

import org.jetbrains.annotations.NotNull;

public interface TransmitterListener {
    void onMessageReceived(@NotNull TransmissionType type, @NotNull TransmissionMessage message);
    void onMessageDataChunkReceived(@NotNull TransmissionType type, double progress);
    void onMessageDataChunkSent(@NotNull TransmissionType type, double progress);
    void onMessageFullySent(@NotNull TransmissionType type);
    void onMessageFailedOrCancelled(@NotNull TransmissionType type);
}
