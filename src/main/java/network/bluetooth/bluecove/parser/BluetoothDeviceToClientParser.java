/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.bluetooth.bluecove.parser;

import javax.bluetooth.RemoteDevice;
import org.jetbrains.annotations.NotNull;
import network.bluetooth.basic.BEClient;
import network.bluetooth.bluecove.model.BCClient;
import network.bluetooth.bluecove.model.BCClientDeviceInfo;
import utilities.Errors;
import utilities.Parser;

public class BluetoothDeviceToClientParser implements Parser<RemoteDevice, BEClient> {
    @Override
    public @NotNull BEClient parse(@NotNull RemoteDevice data) throws Exception {
        String name = "Unknown";
        
        try {
            name = data.getFriendlyName(false);
        } catch (Exception e) {
            
        }
        
        if (name == null) {
            Errors.throwInvalidArgument("Bluetooth device needs to have a non null name");
        }

        return new BCClient(new BCClientDeviceInfo(data));
    }
}
