/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.discovery;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.bluetooth.DiscoveryAgent;
import org.jetbrains.annotations.NotNull;
import quickchatter.network.bluetooth.basic.BEEmitter;
import quickchatter.network.bluetooth.bluecove.BCAdapter;
import quickchatter.utilities.Errors;
import quickchatter.utilities.Logger;
import quickchatter.utilities.SafeMutableArray;
import quickchatter.utilities.SimpleCallback;
import quickchatter.utilities.TimeValue;

/// This emitter does nothing for now, usually the bluetooth device needs to emit its
/// presence to others, but in this case java does it when inquiry scanning.
public class BCEmitter implements BEEmitter {
    public static final @NotNull TimeValue DEFAULT_EMIT_TIME = TimeValue.buildSeconds(30);

    private final @NotNull BCAdapter _adapter;
    private final @NotNull TimeValue _time;
    private final @NotNull AtomicBoolean _active = new AtomicBoolean();

    private final @NotNull SafeMutableArray<SimpleCallback> _endCompletions = new SafeMutableArray<>();

    public BCEmitter(@NotNull BCAdapter adapter, @NotNull TimeValue time) {
        _adapter = adapter;
        _time = time;
    }

    // # BEEmitter

    @Override
    public boolean isBluetoothOn() {
        return _adapter.isAvailable();
    }

    @Override
    public boolean isEmitting() {
        return _active.get();
    }

    @Override
    public @NotNull TimeValue getTime() {
        return _time;
    }

    @Override
    public void start() throws Exception {
        if (!_adapter.isAvailable()) {
            Errors.throwUnsupportedOperation("Bluetooth unavailable");
        }
        
        if (_active.getAndSet(true)) {
            return;
        }
        
        _adapter.getAdapter().setDiscoverable(DiscoveryAgent.GIAC);

        Logger.message(this, "Device is now discoverable!");
    }

    @Override
    public void stop() throws Exception {
        if (!_adapter.isAvailable()) {
            Errors.throwUnsupportedOperation("Bluetooth unavailable");
        }
        
        if (!_active.getAndSet(false)) {
            return;
        }
        
        Logger.message(this, "Device is no longer discoverable!");
        
        _adapter.getAdapter().setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);
        
        performAndClearEndCompletions();
    }

    @Override
    public void addEndCompletion(@NotNull SimpleCallback completion) {
        _endCompletions.add(completion);
    }

    // # Internals

    private void performAndClearEndCompletions() {
        List<SimpleCallback> completions = _endCompletions.copyData();

        _endCompletions.removeAll();

        for (SimpleCallback c : completions) {
            c.perform();
        }
    }
}

