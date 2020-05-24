/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.parser;

import javax.bluetooth.RemoteDevice;
import org.jetbrains.annotations.NotNull;
import quickchatter.network.bluetooth.basic.BEPairing;
import quickchatter.network.bluetooth.bluecove.model.BCClient;
import quickchatter.network.bluetooth.bluecove.model.BCClientDeviceInfo;
import quickchatter.utilities.Errors;
import quickchatter.utilities.Parser;

public class BluetoothDeviceToPairEntityParser implements Parser<RemoteDevice, BEPairing.Entity> {
    @Override
    public @NotNull BEPairing.Entity parse(@NotNull RemoteDevice data) throws Exception {
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
