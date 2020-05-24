package quickchatter.network.bluetooth.bluecove;

import javax.bluetooth.UUID;
import org.jetbrains.annotations.NotNull;

public class BCUUID {
    public static @NotNull UUID get() {
        // This called base UUID. Its a generic bluetooth uuid.
        return new UUID("0000110100001000800000805F9B34FB", false);
    }
}
