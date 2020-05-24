package quickchatter.network.basic;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TransmissionLine {
    void close();

    @NotNull TransmissionType getType();

    // Numbers of bytes read or written from/to stream.
    long getNumberOfBytesTransmitted();

    interface Input extends TransmissionLine {
        @NotNull StreamBandwidth getReadBandwidth();

        int currentReadingSegmentExpectedLength();

        // Read new data, if available.
        void readNewData() throws Exception;

        @NotNull List<TransmissionMessagePart> readBuffer();
        @NotNull List<TransmissionMessagePart> clearDataUntilFinalEndPart();
    }

    interface Output extends TransmissionLine {
        @NotNull StreamBandwidth getWriteBandwidth();

        void writeMessages(@NotNull List<TransmissionMessagePart> messages) throws Exception;
    }

    interface InputAndOutput extends TransmissionLine {
        @NotNull TransmissionLine.Input getInput();
        @NotNull TransmissionLine.Output getOutput();
    }
}
