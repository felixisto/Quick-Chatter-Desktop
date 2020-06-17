/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.bluetooth.bluecove.model;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import network.bluetooth.basic.BEClient;

public class BCDiscoveredClient {
    public final @NotNull BEClient client;
    private final @NotNull AtomicReference<Date> _dateFound = new AtomicReference<>();

    public BCDiscoveredClient(@NotNull BEClient client, @NotNull Date dateFound) {
        this.client = client;
        this._dateFound.set(dateFound);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof BCDiscoveredClient) {
            return hashCode() == other.hashCode();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return client.getIdentifier();
    }

    public @NotNull Date getDateFound() {
        return _dateFound.get();
    }

    public void updateDateFound(@NotNull Date dateFound) {
        _dateFound.set(dateFound);
    }
}
