package quickchatter.network.bluetooth.basic;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

public interface BEBinding {
    @NotNull Date getDateBinded();

    boolean isClientMaster();
    @NotNull BEClient getClient();
}
