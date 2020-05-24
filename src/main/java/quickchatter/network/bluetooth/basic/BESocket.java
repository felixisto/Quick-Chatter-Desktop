/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.basic;

public interface BESocket<T> {
    T getSocket();
    
    void close() throws Exception;
}
