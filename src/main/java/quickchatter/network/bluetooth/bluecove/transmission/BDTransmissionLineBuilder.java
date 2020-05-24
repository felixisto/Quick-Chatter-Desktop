package quickchatter.network.bluetooth.bluecove.transmission;

import org.jetbrains.annotations.NotNull;

import quickchatter.network.basic.TransmissionReadStream;
import quickchatter.network.basic.TransmissionType;
import quickchatter.network.basic.TransmissionWriteStream;
import quickchatter.network.bluetooth.bluecove.BDConstants;

public class BDTransmissionLineBuilder {
    public final @NotNull
    TransmissionReadStream input;
    public final @NotNull
    TransmissionWriteStream output;

    public BDTransmissionLineBuilder(@NotNull TransmissionReadStream input, @NotNull TransmissionWriteStream output) {
        this.input = input;
        this.output = output;
    }

    public @NotNull BDTransmissionLine build(@NotNull TransmissionType type) {
        return new BDTransmissionLine(input, output, type);
    }

    public @NotNull BDTransmissionLine buildPing() {
        return new BDTransmissionLine(input, output, BDConstants.getShared().TYPE_PING);
    }
}
