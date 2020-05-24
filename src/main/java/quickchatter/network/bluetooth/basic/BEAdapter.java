/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.basic;

/// Generic component used to see if bluetooth is available and provides fundamental bluetooth
/// functionality such as getting bluetooth scanners.
public interface BEAdapter <T> {
    boolean isAvailable();

    T getAdapter();
}
