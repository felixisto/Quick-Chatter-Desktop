package quickchatter.network.bluetooth.bluecove.transmission;

import org.jetbrains.annotations.NotNull;

import quickchatter.network.basic.TransmissionReadStream;
import quickchatter.network.basic.TransmissionType;
import quickchatter.network.basic.TransmissionWriteStream;
import quickchatter.network.bluetooth.bluecove.BCConstants;

public class BCTransmissionLineBuilder {
    public final @NotNull
    TransmissionReadStream input;
    public final @NotNull
    TransmissionWriteStream output;

    public BCTransmissionLineBuilder(@NotNull TransmissionReadStream input, @NotNull TransmissionWriteStream output) {
        this.input = input;
        this.output = output;
    }

    public @NotNull BCTransmissionLine build(@NotNull TransmissionType type) {
        return new BCTransmissionLine(input, output, type);
    }

    public @NotNull BCTransmissionLine buildPing() {
        return new BCTransmissionLine(input, output, BCConstants.getShared().TYPE_PING);
    }
}
