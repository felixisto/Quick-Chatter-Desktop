package quickchatter.network.bluetooth.basic;

import org.jetbrains.annotations.NotNull;

/// Describes the device of a client.
/// Contains information about the capabilities of the device.
public interface BEClientDevice {
    @NotNull String getName();

    interface AudioGateway extends BEClientDevice {

    }

    interface AudioIn extends AudioGateway {

    }

    interface AudioOut extends AudioGateway {

    }
}
