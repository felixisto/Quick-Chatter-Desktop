/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.parser;

import javax.bluetooth.RemoteDevice;
import org.jetbrains.annotations.NotNull;
import quickchatter.network.bluetooth.basic.BEClient;
import quickchatter.network.bluetooth.bluecove.model.BDClient;
import quickchatter.network.bluetooth.bluecove.model.BDClientDeviceInfo;
import quickchatter.utilities.Errors;
import quickchatter.utilities.Parser;

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

        return new BDClient(new BDClientDeviceInfo(data));
    }
}
