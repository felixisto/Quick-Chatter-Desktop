/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.discovery;

import java.util.ArrayList;
import java.util.List;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.RemoteDevice;
import org.jetbrains.annotations.NotNull;
import quickchatter.network.bluetooth.basic.BEPairing;
import quickchatter.network.bluetooth.bluecove.BCAdapter;
import quickchatter.network.bluetooth.bluecove.parser.BluetoothDeviceToPairEntityParser;
import quickchatter.utilities.Parser;

public class BCPairing implements BEPairing.Database {
    private final @NotNull BCAdapter _adapter;
    private final @NotNull Parser<RemoteDevice, BEPairing.Entity> _parser;

    public BCPairing(@NotNull BCAdapter adapter, @NotNull Parser<RemoteDevice, BEPairing.Entity> parser) {
        _adapter = adapter;
        _parser = parser;
    }

    public BCPairing(@NotNull BCAdapter adapter) {
        this(adapter, new BluetoothDeviceToPairEntityParser());
    }

    // # BEPairing.Database

    @Override
    public List<BEPairing.Entity> getKnownPairedClients() {
        ArrayList<BEPairing.Entity> clients = new ArrayList<>();
        
        if (!_adapter.isAvailable()) {
            return clients;
        }
        
        RemoteDevice[] remoteDevices = _adapter.getAdapter().getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN);
        
        for (RemoteDevice d : remoteDevices) {
            try {
                clients.add(_parser.parse(d));
            } catch (Exception e) {
                
            }
        }
        
        // Not supported
        return clients;
    }
}
