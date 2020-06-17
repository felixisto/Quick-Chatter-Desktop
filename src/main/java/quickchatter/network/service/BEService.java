/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.service;

import org.jetbrains.annotations.NotNull;

/**
 * Functionality supported by a bluetooth device.
 */
public interface BEService {
    interface Named extends BEService {
        @NotNull String getName();
    }
    
    interface Connectable extends BEService {
        @NotNull String getConnectURL();
    }
    
    interface Obex extends Connectable {
        
    }
}
