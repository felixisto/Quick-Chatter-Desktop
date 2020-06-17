package network.basic;

import org.jetbrains.annotations.NotNull;

import utilities.Logger;
import utilities.Timer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.Arrays;

public class TransmissionReadStream implements TransmissionStream.Read {
    private static boolean DEBUG_LOG = true;

    private final @NotNull Object lock = new Object();

    private final @NotNull InputStream _stream;
    private final @NotNull StreamBandwidth.Tracker.Read _bandwidth;

    private final @NotNull ByteArrayOutputStream _bufferStream;
    private final @NotNull DataOutputStream _buffer;

    private long _totalBytesRead = 0;

    private final @NotNull Timer _forceFlushTimer;

    public TransmissionReadStream(@NotNull InputStream stream, @NotNull StreamBandwidth.Tracker.Read bandwidth) {
        _stream = stream;
        _bandwidth = bandwidth;
        _bufferStream = new ByteArrayOutputStream();
        _buffer = new DataOutputStream(_bufferStream);
        _forceFlushTimer = new Timer(bandwidth.getForceFlushTime());
    }

    // # TransmissionStream

    @Override
    public long getTotalBytesRead() {
        synchronized (lock) {
            return _totalBytesRead;
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            try {
                _stream.close();
            } catch (Exception e) {

            }
        }
    }

    @Override
    public @NotNull StreamBandwidth getBandwidth() {
        return _bandwidth;
    }

    @Override
    public void read() {
        synchronized (lock) {
            try {
                readStreamDataIntoBuffer();
            } catch (Exception e) {

            }
        }
    }

    @Override
    public @NotNull byte[] getBuffer() {
        synchronized (lock) {
            byte[] currentBytes = _bufferStream.toByteArray();
            return Arrays.copyOf(currentBytes, currentBytes.length);
        }
    }

    @Override
    public void clearBufferUntilEndIndex(int endIndex) {
        if (endIndex <= 0) {
            return;
        }

        synchronized (lock) {
            byte[] currentBytes = _bufferStream.toByteArray();

            if (endIndex > currentBytes.length) {
                Logger.error(this, "Cannot erase buffer, end index is outside bounds");
                return;
            }

            if (endIndex == currentBytes.length) {
                _bufferStream.reset();
                return;
            }

            byte[] bytes = Arrays.copyOfRange(currentBytes, endIndex, currentBytes.length);
            _bufferStream.reset();

            try {
                _buffer.write(bytes);
            } catch (Exception e) {

            }
        }
    }

    // # Internal

    private void readStreamDataIntoBuffer() throws Exception {
        try {
            while (readNextChunk()) {

            }
        } catch (Exception e) {

        }
    }

    private boolean readNextChunk() throws Exception {
        int flushRate = (int)_bandwidth.getFlushDataRate().inBytes();
        int available = _stream.available();

        if (available == 0) {
            return false;
        }

        if (available < flushRate) {
            // Force flush?
            if (!_forceFlushTimer.update()) {
                return false;
            }
        }

        byte[] bytes = new byte[flushRate];

        int length = _stream.read(bytes);

        if (length <= 0) {
            return false;
        }

        _buffer.write(Arrays.copyOfRange(bytes, 0, length));
        _buffer.flush();

        _bandwidth.read(length);
        _totalBytesRead += length;

        return available - length >= flushRate;
    }
}
