package quickchatter.network.bluetooth.bluecove;

import javax.bluetooth.UUID;
import org.jetbrains.annotations.NotNull;

public class BDUUID {
    public static @NotNull UUID get() {
        // This is the base UUID. No need for anything more than that.
        return new UUID("0000110100001000800000805F9B34FB", false);
    }
}
