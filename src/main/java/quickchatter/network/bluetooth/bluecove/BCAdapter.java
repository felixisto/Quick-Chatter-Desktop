/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove;

import javax.bluetooth.LocalDevice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickchatter.network.bluetooth.basic.BEAdapter;

public class BCAdapter implements BEAdapter<LocalDevice> {
    private final @Nullable LocalDevice _device;
    
    public BCAdapter(@Nullable LocalDevice device) {
        _device = device;
    }
    
    public static @NotNull BCAdapter getShared() {
        try {
            return new BCAdapter(LocalDevice.getLocalDevice());
        } catch (Exception e) {
            return new BCAdapter(null);
        }
    }
    
    // # BDAdapter

    @Override
    public boolean isAvailable() {
        return _device != null;
    }

    @Override
    public LocalDevice getAdapter() {
        return _device;
    }
    
}
