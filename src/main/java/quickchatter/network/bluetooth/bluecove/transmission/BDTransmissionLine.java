package quickchatter.network.bluetooth.bluecove.transmission;

import org.jetbrains.annotations.NotNull;
import quickchatter.network.basic.StreamBandwidth;
import quickchatter.network.basic.TransmissionLine;
import quickchatter.network.basic.TransmissionReadStream;
import quickchatter.network.basic.TransmissionType;
import quickchatter.network.basic.TransmissionWriteStream;

/// Combines read and write transmission lines.
public class BDTransmissionLine implements TransmissionLine.InputAndOutput {
    public final @NotNull BDTransmissionReadLine read;
    public final @NotNull BDTransmissionWriteLine write;

    public @NotNull BDTransmissionLine build(@NotNull BDTransmissionReadLine read, @NotNull BDTransmissionWriteLine write) {
        return new BDTransmissionLine(read, write);
    }

    public BDTransmissionLine(@NotNull TransmissionReadStream input, @NotNull TransmissionWriteStream output, @NotNull TransmissionType type) {
        this.read = BDTransmissionReadLine.build(input, type);
        this.write = BDTransmissionWriteLine.build(output, type);
    }

    public BDTransmissionLine(@NotNull BDTransmissionReadLine read, @NotNull BDTransmissionWriteLine write) {
        this.read = read;
        this.write = write;
    }

    // # TransmissionLine.InputAndOutput

    @Override
    public @NotNull TransmissionType getType() {
        return this.read.getType();
    }

    public long getNumberOfBytesTransmitted() {
        return read.getNumberOfBytesTransmitted() + write.getNumberOfBytesTransmitted();
    }

    public @NotNull StreamBandwidth getReadBandwidth() {
        return read.getReadBandwidth();
    }

    public @NotNull StreamBandwidth getWriteBandwidth() {
        return write.getWriteBandwidth();
    }

    @Override
    public void close() {
        read.close();
        write.close();
    }

    @Override
    public @NotNull Input getInput() {
        return this.read;
    }

    @Override
    public @NotNull Output getOutput() {
        return this.write;
    }
}
