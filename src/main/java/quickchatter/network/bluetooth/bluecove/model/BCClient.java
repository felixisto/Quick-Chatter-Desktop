/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.model;

import org.jetbrains.annotations.NotNull;
import quickchatter.network.bluetooth.basic.BEClient;
import quickchatter.network.bluetooth.basic.BEClientDevice;
import quickchatter.network.bluetooth.basic.BEPairing;

public class BCClient implements BEClient, BEPairing.Entity {
    public final int identifier;
    public final @NotNull BEClientDevice device;
    public final @NotNull String name;

    public BCClient(@NotNull BEClientDevice device) {
        String name = device.getName();

        this.identifier = name.hashCode();
        this.device = device;
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof BCClient) {
            return hashCode() == other.hashCode();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return getIdentifier();
    }

    // # BEClient

    @Override
    public int getIdentifier() {
        return identifier;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull BEClientDevice getDevice() {
        return device;
    }

    // # BEPairing.Entity

    @Override
    public @NotNull BEClient getClient() {
        return this;
    }
}

