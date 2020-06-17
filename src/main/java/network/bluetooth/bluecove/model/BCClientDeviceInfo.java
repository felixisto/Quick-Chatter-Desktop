/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.bluetooth.bluecove.model;

import java.util.ArrayList;
import java.util.List;
import javax.bluetooth.RemoteDevice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import network.bluetooth.bluecove.BCClientDevice;
import network.bluetooth.service.BEService;
import utilities.CollectionUtilities;

public class BCClientDeviceInfo implements BCClientDevice {
    private final @NotNull RemoteDevice device;
    private final @NotNull String name;
    private final @NotNull List<BEService> services;

    public BCClientDeviceInfo(@NotNull RemoteDevice device) {
        this(device, new ArrayList<>());
    }
    
    public BCClientDeviceInfo(@NotNull RemoteDevice device, @NotNull List<BEService> services) {
        this.device = device;
        
        String deviceName;
        
        try {
            deviceName = device.getFriendlyName(false);
        } catch (Exception e) {
            deviceName = "Unknown";
        }
        
        this.name = deviceName;
        this.services = CollectionUtilities.copy(services);
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull RemoteDevice asRemoteDevice() {
        return device;
    }
    
    @Override
    public @NotNull List<BEService> getServices() {
        return services;
    }
    
    @Override
    public @Nullable boolean suportsService(@NotNull BEService service) {
        return false;
    }
}

