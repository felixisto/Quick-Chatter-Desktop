package network.bluetooth.obex;

import org.jetbrains.annotations.NotNull;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public interface BEObexReceiverListener {
    void onPutBegin(@NotNull String name);
    void onPutUpdate(@NotNull String name, int receivedBytes);
    void onPutEnd(@NotNull String name, @NotNull byte [] data);
}
