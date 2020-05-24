/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.model;

import javax.bluetooth.RemoteDevice;
import org.jetbrains.annotations.NotNull;
import quickchatter.network.bluetooth.bluecove.BDClientDevice;

public class BDClientDeviceInfo implements BDClientDevice {
    private final @NotNull RemoteDevice device;
    private final @NotNull String name;

    public BDClientDeviceInfo(@NotNull RemoteDevice device) {
        this.device = device;
        
        String deviceName;
        
        try {
            deviceName = device.getFriendlyName(false);
        } catch (Exception e) {
            deviceName = "Unknown";
        }
        
        this.name = deviceName;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull RemoteDevice asRemoteDevice() {
        return device;
    }
}
