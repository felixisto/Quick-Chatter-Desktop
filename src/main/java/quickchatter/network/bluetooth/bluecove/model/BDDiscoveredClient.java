/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.model;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import quickchatter.network.bluetooth.basic.BEClient;

public class BDDiscoveredClient {
    public final @NotNull BEClient client;
    private final @NotNull AtomicReference<Date> _dateFound = new AtomicReference<>();

    public BDDiscoveredClient(@NotNull BEClient client, @NotNull Date dateFound) {
        this.client = client;
        this._dateFound.set(dateFound);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof BDDiscoveredClient) {
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
