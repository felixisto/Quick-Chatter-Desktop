package network.bluetooth.bluecove.transmission;

import org.jetbrains.annotations.NotNull;

import network.basic.TransmissionReadStream;
import network.basic.TransmissionType;
import network.basic.TransmissionWriteStream;
import network.bluetooth.bluecove.BCConstants;

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
