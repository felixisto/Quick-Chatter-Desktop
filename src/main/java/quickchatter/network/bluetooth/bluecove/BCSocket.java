/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove;

import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickchatter.network.bluetooth.basic.BESocket;

public class BCSocket implements BESocket<StreamConnection> {
    private final @NotNull StreamConnection _socket;
    private final @Nullable StreamConnectionNotifier _notifier;
    
    public BCSocket(@NotNull StreamConnection socket) {
        _notifier = null;
        _socket = socket;
    }
    
    public BCSocket(@NotNull StreamConnection socket, @Nullable StreamConnectionNotifier notifier) {
        _socket = socket;
        _notifier = notifier;
    }
    
    @Override
    public void close() throws Exception {
        _socket.close();
        
        if (_notifier != null) {
            _notifier.close();
        }
    }

    @Override
    public StreamConnection getSocket() {
        return _socket;
    }
}
