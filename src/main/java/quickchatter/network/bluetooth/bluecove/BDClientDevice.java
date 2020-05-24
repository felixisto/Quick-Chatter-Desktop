/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove;

import javax.bluetooth.RemoteDevice;
import org.jetbrains.annotations.NotNull;
import quickchatter.network.bluetooth.basic.BEClientDevice;

public interface BDClientDevice extends BEClientDevice {
    @NotNull RemoteDevice asRemoteDevice();
}
