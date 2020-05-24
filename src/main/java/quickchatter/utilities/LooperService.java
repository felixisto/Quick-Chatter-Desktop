/*
 * Created by Kristiyan Butev.
 * Copyright © 2019 Kristiyan Butev. All rights reserved.
 */
package quickchatter.utilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Contains utilities for performing callbacks, and updating objects every interval.
 * 
 * LooperClients are updated by one single specific background thread. Do not block it!
 * 
 * The performSync blocks the caller thread until the given callback is performed on
 * a background thread (same background thread as the LooperClients).
 *
 * @author Bytevi
 */
public class LooperService {
    public static int LOOPER_INTERVAL_MSEC = 50;

    private static LooperService singleton;
    
    @NotNull private final ClientsManager _clientsManager = new ClientsManager();
    @NotNull private final CallbacksManager _callbacksManager = new CallbacksManager();
    
    @NotNull private final Runnable _looperRunnable = new Runnable() {
        public void run()
        {
            loop();
            loopAgain();
        }
    };
    
    @NotNull private final ScheduledThreadPoolExecutor _looperExecutor = new ScheduledThreadPoolExecutor(1);
    
    @NotNull private final Object _syncWaitLock = new Object();
    @NotNull private final ArrayList<Thread> _syncWaitingThreads = new ArrayList<>();
    
    private LooperService()
    {
        loopAgain();
    }
    
    // # Public

    synchronized public static @NotNull LooperService getShared()
    {
        if (singleton == null)
        {
            singleton = new LooperService();
        }

        return singleton;
    }
    
    // # Subscription
    
    public void subscribe(@NotNull final LooperClient client, int delay)
    {
        _clientsManager.subscribe(client, delay);
    }

    public void subscribe(@NotNull final LooperClient client)
    {
        subscribe(client, 0);
    }

    public void unsubscribe(@NotNull final LooperClient client)
    {
        _clientsManager.unsubscribe(client);
    }
    
    // # AWT
    
    // Perform callback immediately if caller is on main thread.
    // Otherwise, the callback is performed on main asynchronously.
    public void performOnAWT(final SimpleCallback callback)
    {
        if (java.awt.EventQueue.isDispatchThread())
        {
            callback.perform();
        } else {
            asyncOnAWT(callback);
        }
    }
    
    public void syncOnAWT(@NotNull final SimpleCallback callback)
    {
        if (java.awt.EventQueue.isDispatchThread())
        {
            callback.perform();
            return;
        }
        
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    callback.perform();
                }
            });
        } catch (Exception e) {
            
        }
    }
    
    public <T> void syncOnAWT(@NotNull final Callback<T> callback, @Nullable final T parameter)
    {
        if (java.awt.EventQueue.isDispatchThread())
        {
            callback.perform(parameter);
            return;
        }
        
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    callback.perform(parameter);
                }
            });
        } catch (Exception e) {
            
        }
    }
    
    public void asyncOnAWT(@NotNull final SimpleCallback callback)
    {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                callback.perform();
            }
        });
    }
    
    public <T> void asyncOnAWT(@NotNull final Callback<T> callback, @Nullable final T parameter)
    {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                callback.perform(parameter);
            }
        });
    }
    
    public void asyncOnAWTAfterDelay(@NotNull final SimpleCallback callback, @NotNull TimeValue delay)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay.inMS());
                } catch (Exception e) {
                    
                }
                
                syncOnAWT(callback);
            }
        };
        
        new Thread(runnable).start();
    }
    
    public <T> void asyncOnAWTAfterDelay(@NotNull final Callback<T> callback, @Nullable final T parameter, @NotNull TimeValue delay)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay.inMS());
                } catch (Exception e) {
                    
                }
                
                syncOnAWT(callback, parameter);
            }
        };
        
        new Thread(runnable).start();
    }
    
    // # Background
    
    public void syncInBackground(@NotNull final SimpleCallback callback)
    {
        _callbacksManager.queueCallback(callback);
        enterSyncWaitLock();
    }
    
    public <T> void syncInBackground(@NotNull final Callback<T> callback, @Nullable final T parameter)
    {
        _callbacksManager.queueCallback(callback, parameter);
        enterSyncWaitLock();
    }
    
    public void asyncInBackground(@NotNull final SimpleCallback callback)
    {
        _callbacksManager.queueCallback(callback);
    }
    
    public <T> void asyncInBackground(@NotNull final Callback<T> callback, @Nullable final T parameter)
    {
        _callbacksManager.queueCallback(callback, parameter);
    }
    
    public void asyncInBackgroundAfterDelay(@NotNull final SimpleCallback callback, @NotNull TimeValue delay)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay.inMS());
                    _callbacksManager.queueCallback(callback);
                } catch (Exception e) {
                    
                }
                
            }
        };
        
        new Thread(runnable).start();
    }
    
    public <T> void asyncInBackgroundAfterDelay(@NotNull final Callback<T> callback, @Nullable final T parameter, @NotNull TimeValue delay)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay.inMS());
                    _callbacksManager.queueCallback(callback, parameter);
                } catch (Exception e) {
                    
                }
                
            }
        };
        
        new Thread(runnable).start();
    }
    
    // # Private
    
    private void loop()
    {
        _clientsManager.loop();
        _callbacksManager.loop();
    }
    
    private void loopAgain()
    {
        releaseAllSyncWaitLocks();
        
        _looperExecutor.schedule(_looperRunnable, LOOPER_INTERVAL_MSEC, TimeUnit.MILLISECONDS);
    }
    
    private void enterSyncWaitLock()
    {
        synchronized(_syncWaitLock)
        {
            _syncWaitingThreads.add(Thread.currentThread());
        }
        
        try {
            Thread.currentThread().wait();
        } catch (Exception e) {
            
        }
        
        // No need to remove itself from the array, its removed by the releaser
    }
    
    private void releaseAllSyncWaitLocks()
    {
        synchronized(_syncWaitLock)
        {
            for (Thread t : _syncWaitingThreads)
            {
                t.interrupt();
            }
            
            _syncWaitingThreads.clear();
        }
    }
    
    class ClientsManager 
    {
        @NotNull private HashSet<LooperClientSubscription> _subscriptions = new HashSet<>();
        
        @NotNull private final Object _mainLock = new Object();
        
        private void subscribe(@NotNull final LooperClient client, int delay)
        {
            synchronized (_mainLock)
            {
                _subscriptions.add(new LooperClientSubscription(client, delay));
            }
        }

        private void unsubscribe(@NotNull final LooperClient client)
        {
            synchronized (_mainLock)
            {
                LooperClientSubscription subToRemove = null;
                
                for (LooperClientSubscription sub : _subscriptions)
                {
                    if (sub.client == client)
                    {
                        subToRemove = sub;
                        break;
                    }
                }
                
                _subscriptions.remove(subToRemove);
            }
        }
        
       private void loop()
       {
            Collection<LooperClientSubscription> subscriptions;

            synchronized (_mainLock)
            {
                subscriptions = CollectionUtilities.copyAsImmutable(_subscriptions);
            }
            
            for (LooperClientSubscription sub : subscriptions)
            {
                try {
                    sub.loop(LOOPER_INTERVAL_MSEC);
                } catch (Exception e) {
                    Logger.error(this, "Uncaught exception in client loop: " + e.toString());
                    e.printStackTrace(System.out);
                }
            }
        }
    }
    
    class CallbacksManager {
        @NotNull private final Object _mainLock = new Object();
        
        @NotNull private ArrayList<LooperCallback> _callbacks = new ArrayList();
        
        private void queueCallback(@NotNull SimpleCallback callback)
        {
            synchronized (_mainLock)
            {
                _callbacks.add(new LooperCallback(callback));
            }
        }
        
        private <T> void queueCallback(@NotNull Callback<T> callback, @Nullable T parameter)
        {
            synchronized (_mainLock)
            {
                _callbacks.add(new LooperCallback(callback, parameter));
            }
        }
        
        private void clearCallbacks()
        {
            synchronized (_mainLock)
            {
                _callbacks.clear();
            }
        }
        
        private void loop()
        {
            Collection<LooperCallback> callbacks;

            synchronized (_mainLock)
            {
                callbacks = CollectionUtilities.copyAsImmutable(_callbacks);
            }
            
            for (LooperCallback callback : callbacks)
            {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            callback.perform();
                        } catch (Exception e) {
                            Logger.error(this, "Uncaught exception in callback: " + e.toString());
                            e.printStackTrace(System.out);
                        }
                    }
                };
                
                new Thread(runnable).start();
            }
            
            clearCallbacks();
        }
    }
}

class LooperClientSubscription {
    @NotNull public final LooperClient client;
    
    @NotNull private final int _timer;
    private int _currentTimer;
    
    LooperClientSubscription(@NotNull LooperClient client, int timer)
    {
        this.client = client;
        this._timer = timer;
        this._currentTimer = timer;
    }
    
    public void loop(int dt)
    {
        // Loop only every specific interval
        if (_timer != 0)
        {
            _currentTimer -= dt;
            
            if (_currentTimer > 0)
            {
                return;
            }
            
            // Reset timer to original value
            // Overkill time value is also added to the result
            _currentTimer += _timer;
        }
        
        // Loop
        this.client.loop();
    }
}

class LooperCallback <T> {
    @NotNull public final SimpleCallback callback;
    @NotNull public final Callback<T> callbackWithParameter;
    @Nullable public final T parameter;
        
    LooperCallback(@NotNull SimpleCallback callback)
    {
        this.callback = callback;
        this.callbackWithParameter = null;
        this.parameter = null;
    }
    
    LooperCallback(@NotNull Callback<T> callback, @Nullable T parameter)
    {
        this.callback = null;
        this.callbackWithParameter = callback;
        this.parameter = parameter;
    }
    
    public void perform()
    {
        if (callback != null)
        {
            this.callback.perform();
        }
        else
        {
            this.callbackWithParameter.perform(parameter);
        }
    }
}
