/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.presenter;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import quickchatter.mvp.MVP;
import quickchatter.network.bluetooth.basic.BEClient;
import quickchatter.network.bluetooth.basic.BEPairing;
import quickchatter.network.bluetooth.bluecove.discovery.BDPairing;
import quickchatter.ui.listdata.BEClientsListData;
import quickchatter.utilities.Errors;
import quickchatter.utilities.Logger;
import quickchatter.utilities.LooperService;
import quickchatter.utilities.SimpleCallback;

public class ReconnectPresenter implements BasePresenter.Reconnect {
    private @NotNull BDPairing _pairing;
    private @NotNull BasePresenterDelegate.Reconnect _delegate;
    
    private @NotNull ArrayList<BEClient> _pairedClients = new ArrayList<>();
    
    public ReconnectPresenter(@NotNull BDPairing pairing) {
        _pairing = pairing;
    }

    @Override
    public @NotNull MVP.View getView() {
        return _delegate;
    }
    
    @Override
    public void start(BasePresenterDelegate.Reconnect delegate) throws Exception {
        if (_delegate != null) {
            Errors.throwCannotStartTwice("Presenter already started!");
        }
        
        Logger.message(this, "Start.");
        
        _delegate = delegate;
        
        updatePairedClientsData();
    }
    
    @Override
    public void stop() {
        // Cleanup, make sure bluetooth is properly stopped
    }
    
    @Override
    public void pickItem(int index) {
        if (index < 0 || index >= _pairedClients.size()) {
            return;
        }
        
        if (_delegate == null) {
            return;
        }

        BEClient client = _pairedClients.get(index);

        _delegate.navigateToConnectingScreen(client);
    }
    
    // # Internals
    
    private void updatePairedClientsData() {
        LooperService.getShared().asyncInBackground(new SimpleCallback() {
            @Override
            public void perform() {
                updatePairedClientsDataNow();
            }
        });
    }
    
    private void updatePairedClientsDataNow() {
        List<BEPairing.Entity> entities = _pairing.getKnownPairedClients();
        final ArrayList<BEClient> clients = new ArrayList<>();
        
        for (BEPairing.Entity entity : entities) {
            clients.add(entity.getClient());
        }
        
        LooperService.getShared().performOnAWT(new SimpleCallback() {
            @Override
            public void perform() {
                _pairedClients = clients;
                
                _delegate.updateClientsListData(new BEClientsListData(clients));
            }
        });
    }
}
