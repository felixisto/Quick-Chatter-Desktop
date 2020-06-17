/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.bluetooth.obex;

import java.io.InputStream;
import org.jetbrains.annotations.NotNull;

public interface BEObexReceiverOperation {
    boolean isActive();
    @NotNull String getHeaderName();
    @NotNull InputStream getInputStream();
}
