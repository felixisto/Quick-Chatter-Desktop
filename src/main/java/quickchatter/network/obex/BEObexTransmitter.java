/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.obex;

import org.jetbrains.annotations.NotNull;
import quickchatter.network.basic.Transmitter;
import quickchatter.network.bluetooth.basic.BESocket;

public interface BEObexTransmitter {
    interface Receiver extends Transmitter.Reader {
        void start(@NotNull BEObexReceiverListener listener, @NotNull BESocket socket) throws Exception;
    }
    
    interface Sender extends Transmitter.Writer {
        @NotNull BEObexSenderOperator start(@NotNull BESocket socket) throws Exception;
    }
}
