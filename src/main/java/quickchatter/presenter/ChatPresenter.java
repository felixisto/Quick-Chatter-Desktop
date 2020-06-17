/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.presenter;

import org.jetbrains.annotations.NotNull;
import filesystem.fundamentals.FilePath;
import quickchatter.mvp.MVP;
import network.basic.StreamBandwidth;
import network.basic.TransmissionMessage;
import network.basic.TransmissionType;
import network.basic.TransmitterListener;
import network.bluetooth.basic.BEClient;
import network.bluetooth.basic.BETransmitter;
import network.bluetooth.bluecove.BCConstants;
import network.bluetooth.bluecove.transmission.BCTransmissionMessage;
import quickchatter.presenter.worker.SendFilePerformer;
import quickchatter.presenter.worker.SendFilePerformerDelegate;
import quickchatter.ui.chat.UIChat;
import utilities.Callback;
import utilities.Errors;
import utilities.Logger;
import utilities.LooperClient;
import utilities.LooperService;
import utilities.Path;
import utilities.SimpleCallback;
import utilities.TimeValue;
import utilities.Timer;

public class ChatPresenter implements BasePresenter.Chat, LooperClient, TransmitterListener, SendFilePerformerDelegate {
    private @NotNull BasePresenterDelegate.Chat _delegate;
    
    private @NotNull final BEClient _client;
    private @NotNull final BETransmitter.ReaderWriter _transmitter;
    private @NotNull final BETransmitter.Service _transmitterService;
    
    private @NotNull final UIChat _chat;
    
    private @NotNull final Timer _updateTimer = new Timer(TimeValue.buildSeconds(0.5));
    
    private @NotNull final SendFilePerformer _sendFilePerformer;
    
    private boolean _timeoutWarningSent = false;
    private boolean _timeoutSent = false;
    
    public ChatPresenter(@NotNull BEClient client,
                         @NotNull BETransmitter.ReaderWriter transmitter,
                         @NotNull BETransmitter.Service transmitterService) {
        _client = client;
        _transmitter = transmitter;
        _transmitterService = transmitterService;
        
        _chat = new UIChat("Me", client.getName(), true, 100);
        
        _sendFilePerformer = new SendFilePerformer(_transmitter, _transmitterService);
    }
    
    // # BasePresenter.Chat

    @Override
    public void start(BasePresenterDelegate.Chat delegate) throws Exception {
        if (_delegate != null) {
            Errors.throwCannotStartTwice("Already started");
        }

        _delegate = delegate;
        
        try {
            _transmitter.start();
        } catch (Exception e) {
            _delegate = null;
            
            throw e;
        }
        
        LooperService.getShared().subscribe(this);
        _transmitterService.subscribe(this);

        _delegate.updateClientInfo(_client.getName());

        _chat.addTextLine("Connected on " + _chat.getCurrentTimestamp() + "!");

        _delegate.updateChat("", _chat.getLog());
        
        _sendFilePerformer.start(this);
    }
    
    @Override
    public void stop() {
        final ChatPresenter self = this;
        
        LooperService.getShared().performOnAWT(new SimpleCallback() {
            @Override
            public void perform() {
                _delegate = null;

                LooperService.getShared().unsubscribe(self);

                _transmitterService.unsubscribe(self);

                _sendFilePerformer.stop();

                _transmitter.stop();
                _transmitterService.stop();
            }
        });
    }
    
    @Override
    public void sendMessage(@NotNull String message) {
        if (message.isEmpty()) {
            return;
        }

        Logger.message(this, "Send message '" + message + "'");

        try {
            TransmissionType type = BCConstants.getShared().TYPE_CHAT;
            byte[] bytes = message.getBytes();

            _transmitter.sendMessage(new BCTransmissionMessage(type, bytes));

            message = _chat.parseStringForSendMessage(bytes);

            _chat.onMessageSent(message);

            if (_delegate != null) {
                _delegate.updateChat(message, _chat.getLog());
                _delegate.clearChatTextField();
            }
        } catch (Exception e) {

        }
    }

    @Override
    public boolean canSendFile() {
        return _sendFilePerformer.isIdle();
    }
    
    @Override
    public void sendFile(@NotNull Path path) {
        if (!canSendFile()) {
            return;
        }

        Logger.message(this, "Attempting to send file at " + path.toString());

        try {
            _sendFilePerformer.sendFile(new FilePath(path));

            final String message = "Asking other side for transfer file...";

            _chat.addTextLine(message);

            LooperService.getShared().performOnAWT(new SimpleCallback() {
                @Override
                public void perform() {
                    if (_delegate != null) {
                        _delegate.updateChat(message, _chat.getLog());
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    @Override
    public MVP.View getView() {
        return _delegate;
    }

    // # LooperClient
    
    @Override
    public void loop() {
        if (_updateTimer.update()) {
            readAllNewMessages();
            updatePingState();
        }
    }
    
    // # SendFilePerformerDelegate

    @Override
    public void onAskedToReceiveFile(@NotNull final Callback<Path> accept,
                                     @NotNull final SimpleCallback deny,
                                     @NotNull final String name,
                                     @NotNull final String description) {
        final String message = "Other side wants to transfer file...";
        Logger.message(this, message);

        final ChatPresenter self = this;

        LooperService.getShared().performOnAWT(new SimpleCallback() {
            @Override
            public void perform() {
                if (_delegate == null) {
                    return;
                }

                _delegate.onAskToAcceptTransferFile(new Callback<Path>() {
                    @Override
                    public void perform(Path path) {
                        Logger.message(self, "Attempting to save receiving file to " + path.toString());

                        final String message = "Accepted file transfer! Saving to " + path.toString();
                        _chat.addTextLine(message);
                        updateDelegateChat(message);

                        accept.perform(path);
                    }
                }, new SimpleCallback() {
                    @Override
                    public void perform() {
                        Logger.message(self, "Denied file transfer");

                        final String message = "Denied file transfer!";
                        _chat.addTextLine(message);
                        updateDelegateChat(message);

                        deny.perform();
                    }
                }, name, description);

                _chat.addTextLine(message);
                updateDelegateChat(message);
            }
        });
    }

    @Override
    public void onOtherSideAcceptedTransferAsk() {
        if (_delegate == null) {
            return;
        }

        final String message = "Other side agreed to transfer file to them! Begin transfer...";
        _chat.addTextLine(message);
        updateDelegateChat(message);
    }

    @Override
    public void onOtherSideDeniedTransferAsk() {
        if (_delegate == null) {
            return;
        }

        final String message = "Other side denied file transfer.";
        _chat.addTextLine(message);
        updateDelegateChat(message);
    }

    @Override
    public void onFileTransferComplete(@NotNull FilePath path) {
        _chat.removeBottomLineText();

        final String message = "File transferred successfully!";
        _chat.addTextLine(message);
        updateDelegateChat(message);
    }

    @Override
    public void onFileTransferCancelled() {
        _chat.removeBottomLineText();

        final String message = "File transfer cancelled!";
        _chat.addTextLine(message);
        updateDelegateChat(message);
    }

    @Override
    public void fileTransferProgressUpdate(double progress) {
        Logger.message(this, "File transfer update: " + progress);

        String currentTransferRate = "";

        if (_sendFilePerformer.getState() == SendFilePerformer.State.receivingData) {
            StreamBandwidth bandwidth = _transmitter.getInputLines().get(0).getReadBandwidth();

            if (bandwidth instanceof StreamBandwidth.Tracker.Monitor) {
                currentTransferRate = " (" + ((StreamBandwidth.Tracker.Monitor)bandwidth).getEstimatedCurrentRate().toString() + "/sec)";
            }
        }

        _chat.setBottomLineText("> Transfer progress " + (int)(progress * 100.0) + "%" + currentTransferRate);
        updateDelegateChat("");
    }

    @Override
    public void fileSaveFailed(final @NotNull Exception error) {
        _chat.removeBottomLineText();

        final String message = "Failed to save file, error: " + error.toString();
        _chat.addTextLine(message);
        updateDelegateChat(message);

        LooperService.getShared().performOnAWT(new SimpleCallback() {
            @Override
            public void perform() {
                if (_delegate != null) {
                    _delegate.showError("Failed to save file", error.toString());
                }
            }
        });
    }
    
    // # TransmitterListener

    @Override
    public void onMessageReceived(TransmissionType type, TransmissionMessage message) {
        if (!type.equals(BCConstants.getShared().TYPE_CHAT)) {
            return;
        }

        LooperService.getShared().performOnAWT(new SimpleCallback() {
            @Override
            public void perform() {
                String text = _chat.parseStringForReceiveMessage(message.getBytes());

                _chat.onMessageReceived(text);

                if (_delegate != null) {
                    _delegate.updateChat(text, _chat.getLog());
                }
            }
        });
    }

    @Override
    public void onMessageDataChunkReceived(TransmissionType type, double progress) {
        
    }

    @Override
    public void onMessageDataChunkSent(TransmissionType type, double progress) {
        
    }

    @Override
    public void onMessageFullySent(TransmissionType type) {
        
    }

    @Override
    public void onMessageFailedOrCancelled(TransmissionType type) {
        
    }
    
    // # Internals
    
    private void readAllNewMessages() {
        _transmitterService.readAllNewMessages();
    }

    private void updatePingState() {
        TimeValue pingDelay = _transmitterService.getPingStatusChecker().timeElapsedSinceLastPing();

        if (pingDelay.inMS() > BCConstants.CONNECTION_TIMEOUT_WARNING.inMS()) {
            if (pingDelay.inMS() > BCConstants.CONNECTION_TIMEOUT.inMS()) {
                handleConnectionTimeout();
            } else {
                handleConnectionTimeoutWarning();
            }
        } else {
            if (_timeoutWarningSent || _timeoutSent) {
                performOnDelegate(new Callback<BasePresenterDelegate.Chat>() {
                    @Override
                    public void perform(BasePresenterDelegate.Chat delegate) {
                        delegate.onConnectionRestored();
                    }
                });
            }
            
            _timeoutWarningSent = false;
            _timeoutSent = false;
        }
    }
    
    private void handleConnectionTimeout() {
        if (!_timeoutSent) {
            Logger.warning(this, "Connection timeout detected!");
            
            _timeoutSent = true;
                    
            performOnDelegate(new Callback<BasePresenterDelegate.Chat>() {
                @Override
                public void perform(BasePresenterDelegate.Chat delegate) {
                    delegate.onConnectionTimeout(false);
                }
            });
        }
    }
    
    private void handleConnectionTimeoutWarning() {
        if (!_timeoutWarningSent) {
            Logger.warning(this, "Connection timeout - warning.");
            
            _timeoutWarningSent = true;
                    
            performOnDelegate(new Callback<BasePresenterDelegate.Chat>() {
                @Override
                public void perform(BasePresenterDelegate.Chat delegate) {
                    delegate.onConnectionTimeout(true);
                }
            });
        }
    }
    
    private void performOnDelegate(@NotNull Callback<BasePresenterDelegate.Chat> callback) {
        LooperService.getShared().performOnAWT(new SimpleCallback() {
            @Override
            public void perform() {
                if (_delegate == null) {
                    return;
                }
                
                callback.perform(_delegate);
            }
        });
    }
    
    private void updateDelegateChat(@NotNull final String message) {
        performOnDelegate(new Callback<BasePresenterDelegate.Chat>() {
            @Override
            public void perform(BasePresenterDelegate.Chat delegate) {
                delegate.updateChat(message, _chat.getLog());
            }
        });
    }
}
