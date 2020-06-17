package quickchatter.network.bluetooth.basic;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickchatter.network.bluetooth.service.BEService;

/// Describes the device of a client.
/// Contains information about the capabilities of the device.
public interface BEClientDevice {
    @NotNull String getName();
    
    @NotNull List<BEService> getServices();
    @Nullable boolean suportsService(@NotNull BEService service);
}
