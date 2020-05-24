package quickchatter.network.bluetooth.basic;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface BEPairing {
    interface Entity {
        @NotNull BEClient getClient();
    }

    interface Database {
        @NotNull List<BEPairing.Entity> getKnownPairedClients();
    }
}
