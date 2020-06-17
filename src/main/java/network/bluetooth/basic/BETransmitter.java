package network.bluetooth.basic;

import org.jetbrains.annotations.NotNull;

import network.basic.Transmitter;

public interface BETransmitter {
    @NotNull BESocket getSocket();

    interface ReaderWriter extends BETransmitter, Transmitter.ReaderWriter {

    }

    interface Service extends BETransmitter, Transmitter.Service, Transmitter.Pinger {

    }
}
