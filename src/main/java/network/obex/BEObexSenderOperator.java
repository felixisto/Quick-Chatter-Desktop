/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.bluetooth.obex;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * @author Byti
 */
public interface BEObexSenderOperator {
    void put(@NotNull byte[] data, @Nullable String name, @Nullable String type) throws Exception;
    
    // Note: may block the caller until all data is transmitted.
    void disconnect() throws Exception;
}
