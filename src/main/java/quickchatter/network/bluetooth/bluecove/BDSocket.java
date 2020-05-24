/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove;

import javax.microedition.io.StreamConnection;
import org.jetbrains.annotations.NotNull;
import quickchatter.network.bluetooth.basic.BESocket;

public class BDSocket implements BESocket<StreamConnection> {
    private final @NotNull StreamConnection _socket;
    
    public BDSocket(@NotNull StreamConnection socket) {
        _socket = socket;
    }
    
    @Override
    public void close() throws Exception {
        _socket.close();
    }

    @Override
    public StreamConnection getSocket() {
        return _socket;
    }
}
