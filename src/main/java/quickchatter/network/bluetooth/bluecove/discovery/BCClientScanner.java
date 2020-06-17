/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.discovery;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.bluetooth.RemoteDevice;
import org.jetbrains.annotations.NotNull;
import quickchatter.network.basic.ScannerConfiguration;
import quickchatter.network.bluetooth.basic.BEClient;
import quickchatter.network.bluetooth.basic.BEClientScanner;
import quickchatter.network.bluetooth.basic.BEClientScannerListener;
import quickchatter.network.bluetooth.basic.BEError;
import quickchatter.network.bluetooth.bluecove.model.BCDiscoveredClient;
import quickchatter.network.bluetooth.bluecove.parser.BluetoothDeviceToClientParser;
import quickchatter.utilities.Callback;
import quickchatter.utilities.CollectionUtilities;
import quickchatter.utilities.Logger;
import quickchatter.utilities.LooperClient;
import quickchatter.utilities.LooperService;
import quickchatter.utilities.Parser;
import quickchatter.utilities.SafeMutableArray;
import quickchatter.utilities.TimeValue;

// Wraps a BCDiscovery, and converts found RemoteDevice objects to BEClient objects.
public class BCClientScanner implements BEClientScanner, LooperClient {
    public static final TimeValue CLIENT_DISCOVERY_TIMEOUT = TimeValue.buildSeconds(10);

    private final Object lock = new Object();

    private final AtomicBoolean _running = new AtomicBoolean();

    private final BCDiscovery _discovery;
    private final @NotNull Parser<RemoteDevice, BEClient> _parser;
    private final @NotNull HashSet<BCDiscoveredClient> _clients = new HashSet<>();

    private final AtomicReference<TimeValue> _clientTimeout = new AtomicReference<>(CLIENT_DISCOVERY_TIMEOUT);
    private final AtomicInteger _scanTryCount = new AtomicInteger();

    private @NotNull ScannerConfiguration _configuration = new ScannerConfiguration();

    private final @NotNull SafeMutableArray<BEClientScannerListener> _listeners = new SafeMutableArray<>();

    public BCClientScanner(@NotNull BCDiscovery discovery, @NotNull Parser<RemoteDevice, BEClient> parser) {
        _discovery = discovery;
        _parser = parser;
    }

    public BCClientScanner(@NotNull BCDiscovery discovery) {
        this(discovery, new BluetoothDeviceToClientParser());
    }

    // # BEClientScanner

    @Override
    public void subscribe(@NotNull BEClientScannerListener listener) {
        _listeners.add(listener);
    }

    @Override
    public void unsubscribe(@NotNull BEClientScannerListener listener) {
        _listeners.remove(listener);
    }

    // # Properties

    @Override
    public boolean isRunning() {
        return _running.get();
    }

    public @NotNull TimeValue getClientTimeout() {
        return _clientTimeout.get();
    }

    public void setClientTimeout(@NotNull TimeValue value) {
        _clientTimeout.set(value);
    }

    // # Operations

    @Override
    public void start() throws Exception {
        synchronized (lock) {
            if (isRunning()) {
                throw new BEError(BEError.Value.alreadyRunning);
            }

            _scanTryCount.set(0);

            LooperService.getShared().subscribe(this);

            startScan(false);
        }
    }

    @Override
    public void stop() throws Exception {
        synchronized (lock) {
            if (!isRunning()) {
                throw new BEError(BEError.Value.alreadyStopped);
            }

            stopScan();
        }
    }

    @Override
    public @NotNull ScannerConfiguration getConfiguration() {
        synchronized (lock) {
            return _configuration;
        }
    }

    @Override
    public void setConfiguration(@NotNull ScannerConfiguration configuration) throws Exception {
        synchronized (lock) {
            if (isRunning()) {
                throw new BEError(BEError.Value.cannotConfigurateWhileRunning);
            }

            _configuration = configuration.copy();
        }
    }

    // Updates the scan data based on the currently gathered info.
    void updateScanWithCurrentState(boolean updateTimeOut) {
        ScanUpdateResult result;

        synchronized (lock) {
            result = updateScan(new Date(), updateTimeOut);
        }

        alertListeners(result);
    }

    // # LooperClient

    @Override
    public void loop() {
        if (!isRunning()) {
            return;
        }

        // Update scan data, and the listeners
        updateScanWithCurrentState(false);

        // Timer
        retryScanIfNecessary();
    }

    // # Timeout

    public boolean isClientConsideredTimedOut(@NotNull BCDiscoveredClient client, @NotNull Date now) {
        long timeout = getClientTimeout().inMS();
        long timePassedInMS = now.getTime() - client.getDateFound().getTime();

        return timeout <= timePassedInMS;
    }

    // # Internals

    private void startScan(final boolean isRetry) {
        Logger.message(this, "Running a " + (!isRetry ? "scan" : "rescan"));

        _running.set(true);

        _scanTryCount.incrementAndGet();

        _listeners.perform(new Callback<BEClientScannerListener>() {
            @Override
            public void perform(BEClientScannerListener listener) {
                if (!isRetry) {
                    listener.onScanStart();
                } else {
                    listener.onScanRestart();
                }
            }
        });

        if (!_discovery.isRunning()) {
            try {
                Logger.message(this, "Starting discovery");

                _discovery.runDiscoveryScan(new Callback<Set<RemoteDevice>>() {
                    @Override
                    public void perform(Set<RemoteDevice> argument) {
                        updateScanWithCurrentState(true);
                    }
                });
            } catch (Exception e) {
                Logger.error(this, "Failed to runDiscoveryScan discovery, error: " + e);
            }
        }
    }

    private void retryScanIfNecessary() {
        if (_discovery.isRunning()) {
            return;
        }

        boolean isExhausted = _scanTryCount.get() > getConfiguration().retryCount;

        if (isExhausted) {
            Logger.message(this, "Exhausted retry, stopping scan.");
            stopScan();
            return;
        }

        int retryDelayInMS = getConfiguration().retryDelay.inMS();

        boolean retry = _discovery.getTimePassedSinceLastScan().inMS() > retryDelayInMS;

        if (retry) {
            restartScan();
        }
    }

    private void restartScan() {
        startScan(true);
    }

    private void stopScan() {
        LooperService.getShared().unsubscribe(this);

        _listeners.perform(new Callback<BEClientScannerListener>() {
            @Override
            public void perform(BEClientScannerListener listener) {
                listener.onScanEnd();
            }
        });

        _discovery.stop();

        _running.set(false);
    }

    private @NotNull ScanUpdateResult updateScan(@NotNull Date currentDate, boolean updateTimeOut) {
        ScanUpdateResult result = updateScanData(currentDate, updateTimeOut);

        Logger.message(this, "Scan updated, added " + result.newClients.size() + " new clients and lost " + result.clientsLost.size());

        return result;
    }

    private @NotNull ScanUpdateResult updateScanData(@NotNull Date currentDate, boolean updateTimeOut) {
        Set<BCDiscoveredClient> clientsBeforeUpdate = CollectionUtilities.copy(_clients);

        Set<RemoteDevice> devicesFromScan = _discovery.getFoundDevicesFromLastScan();

        // Make up clients from the found devices
        HashSet<BCDiscoveredClient> scannedClients = new HashSet<>();

        for (RemoteDevice device : devicesFromScan) {
            try {
                BEClient client = _parser.parse(device);

                scannedClients.add(new BCDiscoveredClient(client, currentDate));
            } catch (Exception e) {
                Logger.error(this, "Failed to parse RemoteDevice, error: " + e);
            }
        }

        // Update and record new clients
        Set<BCDiscoveredClient> newClients = addNewClients(scannedClients);

        // Update timeout
        if (updateTimeOut) {
            removedAllTimedOutClients(currentDate);
        }

        // Make a list of all the lost clients
        Set<BCDiscoveredClient> clientsLost = new HashSet<>();

        Set<BCDiscoveredClient> clientsAfterUpdate = CollectionUtilities.copy(_clients);

        for (BCDiscoveredClient client : clientsBeforeUpdate) {
            if (!clientsAfterUpdate.contains(client)) {
                clientsLost.add(client);
            }
        }

        return new ScanUpdateResult(clientsAfterUpdate, newClients, clientsLost);
    }

    // Adds the clients from the given set to the current clients data.
    // Returns a list of the new added clients.
    private @NotNull Set<BCDiscoveredClient> addNewClients(@NotNull Set<BCDiscoveredClient> clients) {
        HashSet<BCDiscoveredClient> addedClients = new HashSet<>();

        synchronized (lock) {
            for (BCDiscoveredClient newClient : clients) {
                int beforeCount = _clients.size();

                _clients.remove(newClient);

                boolean wasAlreadyPresent = beforeCount != _clients.size();

                _clients.add(newClient);

                // If the size changed, then the client was already present
                if (!wasAlreadyPresent) {
                    addedClients.add(newClient);
                }
            }
        }

        return addedClients;
    }

    private void removedAllTimedOutClients(@NotNull Date now) {
        Set<BCDiscoveredClient> currentClients;

        synchronized (lock) {
            currentClients = CollectionUtilities.copy(_clients);
        }

        for (BCDiscoveredClient client: currentClients) {
            if (isClientConsideredTimedOut(client, now)) {
                _clients.remove(client);
            }
        }
    }

    private void alertListeners(@NotNull ScanUpdateResult scanResult) {
        for (final BCDiscoveredClient addedClient : scanResult.newClients) {
            _listeners.perform(new Callback<BEClientScannerListener>() {
                @Override
                public void perform(BEClientScannerListener listener) {
                    listener.onClientFound(addedClient.client);
                }
            });
        }

        for (final BCDiscoveredClient oldClient : scanResult.allClients) {
            _listeners.perform(new Callback<BEClientScannerListener>() {
                @Override
                public void perform(BEClientScannerListener listener) {
                    listener.onClientUpdate(oldClient.client);
                }
            });
        }

        for (final BCDiscoveredClient lostClient : scanResult.clientsLost) {
            _listeners.perform(new Callback<BEClientScannerListener>() {
                @Override
                public void perform(BEClientScannerListener listener) {
                    listener.onClientLost(lostClient.client);
                }
            });
        }
    }
}

class ScanUpdateResult {
    final @NotNull Set<BCDiscoveredClient> allClients;
    final @NotNull Set<BCDiscoveredClient> newClients;
    final @NotNull Set<BCDiscoveredClient> clientsLost;

    ScanUpdateResult(@NotNull Set<BCDiscoveredClient> allClients,
                     @NotNull Set<BCDiscoveredClient> newClients,
                     @NotNull Set<BCDiscoveredClient> clientsLost) {
        this.allClients = allClients;
        this.newClients = newClients;
        this.clientsLost = clientsLost;
    }
}

