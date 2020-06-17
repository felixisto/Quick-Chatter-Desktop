package quickchatter.network.bluetooth.bluecove;

import javax.bluetooth.UUID;
import org.jetbrains.annotations.NotNull;

public class BCUUID {
    public static @NotNull UUID getGeneric() {
        return new UUID("0000110100001000800000805F9B34FB", false);
    }
    
    public static @NotNull UUID getFileTransfer() {
        return new UUID("0000110600001000800000805F9B34FB", false);
    }
    
    public static @NotNull UUID getFileTransferPush() {
        return new UUID("0000110500001000800000805F9B34FB", false);
    }
}
