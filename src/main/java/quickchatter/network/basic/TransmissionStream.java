package quickchatter.network.basic;

import org.jetbrains.annotations.NotNull;

public interface TransmissionStream {
    void close();

    @NotNull StreamBandwidth getBandwidth();

    interface Read extends TransmissionStream {
        long getTotalBytesRead();

        void read();

        @NotNull byte[] getBuffer();
        void clearBufferUntilEndIndex(int endIndex);
    }

    interface Write extends TransmissionStream {
        long getTotalBytesWritten();

        void write(@NotNull byte[] bytes);
        void flush();
    }
}
