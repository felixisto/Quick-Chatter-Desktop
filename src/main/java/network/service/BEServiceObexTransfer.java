/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.bluetooth.service;

import org.jetbrains.annotations.NotNull;

public class BEServiceObexTransfer implements BEService.Obex {
    private final @NotNull String _connectURL;
    
    public BEServiceObexTransfer(@NotNull String connectURL) {
        _connectURL = connectURL;
    }
    
    // # BEService
    
    @Override
    public @NotNull String getConnectURL() {
        return _connectURL;
    }
}
