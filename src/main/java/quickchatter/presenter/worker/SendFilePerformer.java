/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.presenter.worker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import filesystem.fundamentals.FilePath;
import filesystem.simple.SimpleFileSystem;
import java.util.regex.Pattern;
import network.basic.TransmissionMessage;
import network.basic.TransmissionType;
import network.basic.Transmitter;
import network.basic.TransmitterListener;
import network.bluetooth.bluecove.BCConstants;
import network.bluetooth.bluecove.segment.BCTransmissionMessageSegment;
import network.bluetooth.bluecove.transmission.BCTransmissionMessage;
import utilities.Callback;
import utilities.DataSize;
import utilities.Errors;
import utilities.Logger;
import utilities.Path;
import utilities.SimpleCallback;

public class SendFilePerformer implements TransmitterListener {
    public enum State {
        idle, sendingAsk, sendingData, receivingAsk, receivingData, finalConfirmation
    }

    public static final char STATUS_CANCEL = '0';
    public static final char STATUS_ACCEPT = '1';
    public static final char STATUS_DENY = '2';
    
    public static final @NotNull String ASK_DESCRIPTION_NAME_SEPARATOR = "'";

    private @NotNull final Object lock = new Object();

    private @NotNull BCConstants constants = BCConstants.getShared();

    private @NotNull AtomicReference<State> _state = new AtomicReference<>(State.idle);
    private @NotNull AtomicReference<FilePath> _path = new AtomicReference<>();
    private @NotNull AtomicReference<FilePath> _savePath = new AtomicReference<>();

    private final @NotNull Transmitter.ReaderWriter _readerWriter;
    private final @NotNull Transmitter.Service _service;

    private @Nullable SendFilePerformerDelegate _delegate;

    private SendFilePerformer self = this;

    private final @NotNull Callback<Path> askReceiveFileAcceptCallback = new Callback<Path>() {
        @Override
        public void perform(Path path) {
            synchronized (lock) {
                Logger.message(self, "Accepted to transfer, beginning to receive....");

                _state.set(State.receivingData);

                _savePath.set(new FilePath(path));

                sendAcceptStatusToOtherSide();
            }
        }
    };

    private final @NotNull SimpleCallback askReceiveFileDenyCallback = new SimpleCallback() {
        @Override
        public void perform() {
            synchronized (lock) {
                Logger.message(self, "Denied to transfer.");

                _state.set(State.idle);

                sendDenyStatusToOtherSide();
            }
        }
    };

    public SendFilePerformer(@NotNull Transmitter.ReaderWriter readerWriter, @NotNull Transmitter.Service service) {
        _readerWriter = readerWriter;
        _service = service;

        SimpleFileSystem system = new SimpleFileSystem();
        _savePath.set(new FilePath(system.getDataDirectory(), "file"));
    }

    // # Properties

    public boolean isIdle() {
        return getState() == State.idle;
    }

    public @NotNull State getState() {
        return _state.get();
    }

    // # Operations

    public void start(@NotNull SendFilePerformerDelegate delegate) {
        synchronized (lock) {
            _delegate = delegate;
            _service.subscribe(this);
        }
    }

    public void stop() {
        synchronized (lock) {
            _delegate = null;

            _state.set(State.idle);

            _service.unsubscribe(this);

            sendCancelStatusToOtherSide();
        }
    }

    public void sendFile(@NotNull FilePath path) throws Exception {
        synchronized (lock) {
            if (getState() != State.idle) {
                Errors.throwIllegalStateError("Cannot send file, already doing something else");
            }

            Logger.message(this, "Send file commencing, first ask other side about permission...");

            _path.set(path);

            _state.set(State.sendingAsk);

            sendAskStatusToOtherSide(path);
        }
    }

    // # TransmitterListener

    @Override
    public void onMessageReceived(@NotNull TransmissionType type, @NotNull TransmissionMessage message) {
        if (message.getType().equals(constants.TYPE_PING)) {
            return;
        }

        synchronized (lock) {
            if (_delegate == null) {
                return;
            }

            if (isMessageAsk(message)) {
                if (getState() == State.idle) {
                    Logger.message(this, "Other side is asking to transfer file to us! Accept or deny?");

                    _state.set(State.receivingAsk);

                    onOtherSideAsk(bytesToString(message.getBytes()));
                } else {
                    Logger.message(this, "Other side is asking to transfer file to us but we are currently busy! Sending cancel...");

                    sendCancelStatusToOtherSide();
                }

                return;
            }

            if (isMessageStatusCancel(message)) {
                Logger.message(this, "Other side is cancelling all operations!");

                _state.set(State.idle);

                onTransferCancel();

                return;
            }

            if (isMessageReceiveData(message) && getState() == State.receivingData) {
                Logger.message(this, "File successfully received! Sending final confirmation to other side and switching to idle mode.");

                _state.set(State.idle);

                onTransferCompleted();
                saveFileData(message.getBytes());

                sendFinalConfirmation();

                return;
            }

            if (getState() == State.sendingAsk) {
                if (isMessageStatusAccept(message)) {
                    Logger.message(this, "Other side agreed to receive the file! Begin transfer...");

                    _state.set(State.sendingData);

                    onOtherSideAcceptedAsk();
                }

                if (isMessageStatusDeny(message)) {
                    Logger.message(this, "Other side denied to receive the file!");

                    _state.set(State.idle);

                    onOtherSideDeniedAsk();
                }

                return;
            }

            if (getState() == State.finalConfirmation) {
                if (isMessageFinalConfirm(message)) {
                    Logger.message(this, "Other side has received the upload! Switching back to idle mode");

                    _state.set(State.idle);

                    onTransferCompletedAndConfirmed();
                }
            }
        }
    }

    @Override
    public void onMessageDataChunkReceived(@NotNull TransmissionType type, double progress) {
        if (!type.equals(constants.TYPE_SEND_FILE_DATA)) {
            return;
        }

        if (_delegate != null) {
            _delegate.fileTransferProgressUpdate(progress);
        }
    }

    @Override
    public void onMessageDataChunkSent(@NotNull TransmissionType type, double progress) {
        if (!type.equals(constants.TYPE_SEND_FILE_DATA)) {
            return;
        }

        if (_delegate != null) {
            _delegate.fileTransferProgressUpdate(progress);
        }
    }

    @Override
    public void onMessageFullySent(@NotNull TransmissionType type) {
        if (type.equals(constants.TYPE_PING)) {
            return;
        }

        synchronized (lock) {
            if (getState() == State.idle) {
                return;
            }

            if (type.equals(constants.TYPE_SEND_FILE_DATA) && getState() == State.sendingData) {
                Logger.message(this, "File successfully sent! Waiting for final confirmation...");

                _state.set(State.finalConfirmation);

                onTransferCompleted();

                return;
            }
        }
    }

    @Override
    public void onMessageFailedOrCancelled(final @NotNull TransmissionType type) {
        if (!type.equals(constants.TYPE_SEND_FILE_DATA)) {
            return;
        }

        synchronized (lock) {
            if (getState() != State.receivingData) {
                return;
            }

            Logger.message(this, "File transfer was cancelled or failed!");

            _state.set(State.idle);

            onTransferCancel();
        }
    }

    // # Build message

    private @NotNull BCTransmissionMessage buildCancelStatusMessage() {
        return new BCTransmissionMessage(constants.TYPE_SEND_FILE_STATUS, charToByte(STATUS_CANCEL));
    }

    private @NotNull BCTransmissionMessage buildAcceptStatusMessage() {
        return new BCTransmissionMessage(constants.TYPE_SEND_FILE_STATUS, charToByte(STATUS_ACCEPT));
    }

    private @NotNull BCTransmissionMessage buildDenyStatusMessage() {
        return new BCTransmissionMessage(constants.TYPE_SEND_FILE_STATUS, charToByte(STATUS_DENY));
    }

    private @NotNull BCTransmissionMessage buildSendAskMessage(@NotNull FilePath path) {
        byte[] value = stringToBytes(buildSendAskDescription(path));

        return new BCTransmissionMessage(constants.TYPE_SEND_FILE_ASK, value);
    }

    private @NotNull BCTransmissionMessage buildDataMessage(@NotNull FilePath path) {
        return new BCTransmissionMessage(constants.TYPE_SEND_FILE_DATA, getData(path));
    }

    private @NotNull BCTransmissionMessage buildFinalConfirmMessage() {
        return new BCTransmissionMessage(constants.TYPE_SEND_FILE_FINAL_CONFIRM);
    }

    // # Message validators

    private boolean isMessageAsk(@NotNull TransmissionMessage message) {
        return message.getType().equals(constants.TYPE_SEND_FILE_ASK);
    }

    private boolean isMessageStatusAccept(@NotNull TransmissionMessage message) {
        String data = bytesToString(message.getBytes());

        if (data.isEmpty()) {
            return false;
        }

        return message.getType().equals(constants.TYPE_SEND_FILE_STATUS) && data.charAt(0) == STATUS_ACCEPT;
    }

    private boolean isMessageStatusDeny(@NotNull TransmissionMessage message) {
        String data = bytesToString(message.getBytes());

        if (data.isEmpty()) {
            return false;
        }

        return message.getType().equals(constants.TYPE_SEND_FILE_STATUS) && data.charAt(0) == STATUS_DENY;
    }

    private boolean isMessageStatusCancel(@NotNull TransmissionMessage message) {
        String data = bytesToString(message.getBytes());

        if (data.isEmpty()) {
            return false;
        }

        return message.getType().equals(constants.TYPE_SEND_FILE_STATUS) && data.charAt(0) == STATUS_CANCEL;
    }

    private boolean isMessageReceiveData(@NotNull TransmissionMessage message) {
        return message.getType().equals(constants.TYPE_SEND_FILE_DATA);
    }

    private boolean isMessageFinalConfirm(@NotNull TransmissionMessage message) {
        return message.getType().equals(constants.TYPE_SEND_FILE_FINAL_CONFIRM);
    }

    // # Transfer steps

    private void sendAskStatusToOtherSide(@NotNull FilePath path) {
        try {
            _readerWriter.sendMessage(buildSendAskMessage(path));
        } catch (Exception e) {
            Logger.error(this, "Something went wrong, failed to ask other side, error: " + e);
            _state.set(State.idle);
        }
    }

    private void onOtherSideAsk(@NotNull String description) {
        if (_delegate == null) {
            return;
        }

        String name = getFileNameFromSentDescription(description);

        _delegate.onAskedToReceiveFile(askReceiveFileAcceptCallback, askReceiveFileDenyCallback, name, description);
    }

    private void onOtherSideAcceptedAsk() {
        beginTransferData(_path.get());

        if (_delegate == null) {
            return;
        }

        _delegate.onOtherSideAcceptedTransferAsk();
    }

    private void onOtherSideDeniedAsk() {
        if (_delegate == null) {
            return;
        }

        _delegate.onOtherSideDeniedTransferAsk();
    }

    private void beginTransferData(@NotNull FilePath path) {
        try {
            _readerWriter.sendMessage(buildDataMessage(path));
        } catch (Exception e) {
            Logger.error(this, "Something went wrong, failed to transfer file, error: " + e);
            _state.set(State.idle);
            sendCancelStatusToOtherSide();
        }
    }

    private void onTransferCompleted() {

    }

    private void onTransferCompletedAndConfirmed() {
        if (_delegate == null) {
            return;
        }

        FilePath path = _savePath.get();

        _delegate.onFileTransferComplete(path);
    }

    private void onTransferCancel() {
        if (_delegate == null) {
            return;
        }

        _delegate.onFileTransferCancelled();
    }

    private void sendAcceptStatusToOtherSide() {
        try {
            _readerWriter.sendMessage(buildAcceptStatusMessage());
        } catch (Exception e) {
        }
    }

    private void sendDenyStatusToOtherSide() {
        try {
            _readerWriter.sendMessage(buildDenyStatusMessage());
        } catch (Exception e) {
        }
    }

    private void sendCancelStatusToOtherSide() {
        try {
            _readerWriter.sendMessage(buildCancelStatusMessage());
        } catch (Exception e) {
        }
    }

    private void saveFileData(@NotNull byte[] bytes) {
        FilePath filePath = _savePath.get();

        Logger.message(this, "Saving transferred data to new file, to directory " + filePath.getPath() + ". Transfer data length is " + bytes.length);

        try {
            String path = filePath.getPath();

            File newFile = new File(path);

            if (newFile.exists()) {
                if (!newFile.delete()) {
                    Errors.throwUnknownError("Failed to replace file at " + path);
                }
            }

            if (!newFile.createNewFile()) {
                Errors.throwUnknownError("Failed to create file at " + path);
            }

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile));
            bos.write(bytes);
            bos.flush();
            bos.close();

            if (_delegate != null) {
                _delegate.onFileTransferComplete(filePath);
            }
        } catch (Exception e) {
            Logger.error(this, "Failed to save transferred file, error: " + e);

            if (_delegate != null) {
                _delegate.fileSaveFailed(e);
            }
        }
    }

    private void sendFinalConfirmation() {
        try {
            _readerWriter.sendMessage(buildFinalConfirmMessage());
        } catch (Exception e) {
            Logger.error(this, "Something went wrong, failed to send final confirmation, error: " + e);
            _state.set(State.idle);
            sendCancelStatusToOtherSide();
        }
    }

    // # Other

    private @NotNull byte[] charToByte(char value) {
        return BCTransmissionMessageSegment.stringToBytes(String.valueOf(value));
    }

    private @NotNull byte[] stringToBytes(@NotNull String value) {
        return BCTransmissionMessageSegment.stringToBytes(value);
    }

    private @NotNull String bytesToString(@NotNull byte[] data) {
        return BCTransmissionMessageSegment.bytesToString(data);
    }

    private @NotNull DataSize getSizeFromPath(@NotNull FilePath path) {
        String filePath = path.getPath();

        File file = new File(filePath);
        return DataSize.buildBytes(file.length());
    }

    private @NotNull byte[] getData(@NotNull FilePath path) {
        try {
            RandomAccessFile file = new RandomAccessFile(path.getPath(), "r");
            byte[] data = new byte[(int)file.length()];
            file.readFully(data);
            return data;
        } catch (Exception e) {

        }

        return new byte[0];
    }

    private @NotNull String buildSendAskDescription(@NotNull FilePath path) {
        String name = path.getLastComponent();
        DataSize size = getSizeFromPath(path);
        return ASK_DESCRIPTION_NAME_SEPARATOR + name + ASK_DESCRIPTION_NAME_SEPARATOR + " (" + size.toString() + ")";
    }
    
    private @NotNull String getFileNameFromSentDescription(@NotNull String description) {
        String[] components = description.split(Pattern.quote(ASK_DESCRIPTION_NAME_SEPARATOR));

        if (components.length <= 2) {
            return "transferredFile";
        }

        return components[1];
    }
}

