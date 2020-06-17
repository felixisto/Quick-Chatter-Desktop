/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.presenter;

import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import quickchatter.mvp.MVP;
import network.bluetooth.basic.BEClient;
import network.bluetooth.basic.BEClientScanner;
import network.bluetooth.basic.BEClientScannerListener;
import network.bluetooth.basic.BEEmitter;
import quickchatter.ui.listdata.BEClientsListData;
import utilities.Errors;
import utilities.Logger;
import utilities.LooperClient;
import utilities.LooperService;
import utilities.SafeMutableArray;
import utilities.SimpleCallback;

public class ConnectPresenter implements BasePresenter.Connect, LooperClient, BEClientScannerListener {
    private @NotNull BasePresenterDelegate.Connect _delegate;
    
    private @NotNull AtomicBoolean _scanning = new AtomicBoolean();

    private final @NotNull BEClientScanner _scanner;
    private final @NotNull BEEmitter _emitter;

    private final @NotNull SafeMutableArray<BEClient> _clients = new SafeMutableArray<>();
    private @NotNull BEClientsListData _data = new BEClientsListData(_clients.copyData());
    
    public ConnectPresenter(@NotNull BEClientScanner scanner, @NotNull BEEmitter emitter) {
        _scanner = scanner;
        _emitter = emitter;
    }

    @Override
    public @NotNull MVP.View getView() {
        return _delegate;
    }
    
    @Override
    public void start(BasePresenterDelegate.Connect delegate) throws Exception {
        if (_delegate != null) {
            Errors.throwCannotStartTwice("Presenter already started!");
        }
        
        Logger.message(this, "Start.");
        
        _delegate = delegate;
    }
    
    @Override
    public void stop() {
        stopScan();
    }
    
    // # Presenter.Connect

    @Override
    public boolean isScanning() {
        return _scanning.get();
    }
    
    @Override
    public void startScan() {
        if (isScanning()) {
            return;
        }

        Logger.message(this, "Start scanning...");

        startScanningNow();
        startEmittingPresence();
    }
    
    @Override
    public void stopScan() {
        Logger.message(this, "Stop scanning.");

        stopCurrentScan();
        stopEmittingPresence();
    }

    @Override
    public void pickItem(int index) {
        if (index < 0 || index >= _clients.size()) {
            return;
        }

        stopCurrentScan();

        if (_delegate == null) {
            return;
        }

        BEClient client = _clients.get(index);

        _delegate.navigateToConnectingScreen(client);
    }

    // # LooperClient

    @Override
    public void loop() {
        if (!isScanning()) {
            return;
        }

        if (_delegate == null) {
            return;
        }

        try {
            if (!_scanner.isRunning()) {
                _scanner.start();
            }
        } catch (Exception e) {

        }
    }

    // # BEClientScannerListener

    @Override
    public void onScanStart() {
        LooperService.getShared().asyncOnAWT(new SimpleCallback() {
            @Override
            public void perform() {
                if (_delegate != null) {
                    _delegate.onStartScan();
                }
            }
        });
    }

    @Override
    public void onScanRestart() {

    }

    @Override
    public void onScanEnd() {
        LooperService.getShared().asyncOnAWT(new SimpleCallback() {
            @Override
            public void perform() {
                if (_delegate != null) {
                    _delegate.onEndScan();
                }
            }
        });
    }

    @Override
    public void onClientFound(final @NotNull BEClient client) {
        LooperService.getShared().asyncOnAWT(new SimpleCallback() {
            @Override
            public void perform() {
                addClient(client);
                updateClientsData();
            }
        });
    }

    @Override
    public void onClientUpdate(final @NotNull BEClient client) {
        LooperService.getShared().asyncOnAWT(new SimpleCallback() {
            @Override
            public void perform() {
                addClient(client);
                updateClientsData();
            }
        });
    }

    @Override
    public void onClientLost(final @NotNull BEClient client) {
        LooperService.getShared().asyncOnAWT(new SimpleCallback() {
            @Override
            public void perform() {
                removeClient(client);
                updateClientsData();
            }
        });
    }

    // # Internals - Scan
    
    private void startScanningNow() {
        _scanning.set(true);
        _scanner.subscribe(this);
        LooperService.getShared().subscribe(this);
        
        // No need to start scan, will be started by loop()
    }
    
    private void stopCurrentScan() {
        _scanning.set(false);
        _scanner.unsubscribe(this);
        LooperService.getShared().unsubscribe(this);
        
        LooperService.getShared().asyncInBackground(new SimpleCallback() {
            @Override
            public void perform() {
                try {
                    _scanner.stop();
                } catch (Exception e) {
                    
                }
            }
        });
    }
    
    // # Internals - Emit presence
    
    private void startEmittingPresence() {
        LooperService.getShared().asyncInBackground(new SimpleCallback() {
            @Override
            public void perform() {
                startEmittingPresenceNow();
            }
        });
    }

    private void startEmittingPresenceNow() {
        _emitter.addEndCompletion(new SimpleCallback() {
            @Override
            public void perform() {
                restartEmittingPresenceIfNecessary();
            }
        });

        try {
            _emitter.start();
        } catch (Exception e) {

        }
    }
    
    private void stopEmittingPresence() {
        try {
            _emitter.stop();
        } catch (Exception e) {

        }
    }

    private void restartEmittingPresenceIfNecessary() {
        if (!_scanner.isRunning()) {
            return;
        }

        startEmittingPresence();
    }
    
    // # Internals - model

    private void updateClientsData() {
        if (_data.getValues().equals(_clients.copyData())) {
            return;
        }

        _data = new BEClientsListData(_clients.copyData());

        if (_delegate == null) {
            return;
        }

        _delegate.updateClientsListData(_data);
    }

    private void addClient(@NotNull BEClient client) {
        boolean alreadyAdded = false;

        for (BEClient element : _clients.copyData()) {
            if (element.getIdentifier() == client.getIdentifier()) {
                alreadyAdded = true;
                break;
            }
        }

        if (!alreadyAdded) {
            _clients.add(client);
        }
    }

    private void removeClient(@NotNull BEClient client) {
        for (BEClient element: _clients.copyData()) {
            if (element.equals(client)) {
                _clients.remove(client);
            }
        }
    }
}
