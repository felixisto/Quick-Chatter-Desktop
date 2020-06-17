package network.bluetooth.basic;

import org.jetbrains.annotations.NotNull;

import utilities.Copyable;

public class BEClientInfo implements Copyable<BEClientInfo> {
    public @NotNull String name = "";

    @Override
    public BEClientInfo copy() {
        BEClientInfo info = new BEClientInfo();
        info.name = name;
        return info;
    }
}
