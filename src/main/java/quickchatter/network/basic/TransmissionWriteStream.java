package quickchatter.network.basic;

import org.jetbrains.annotations.NotNull;

import quickchatter.utilities.Logger;
import quickchatter.utilities.Timer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class TransmissionWriteStream implements TransmissionStream.Write {
    private static boolean DEBUG_LOG = true;

    private final @NotNull Object lock = new Object();

    private final @NotNull OutputStream _stream;
    private final @NotNull StreamBandwidth.Tracker.Write _bandwidth;

    private final @NotNull ByteArrayOutputStream _bufferStream;
    private final @NotNull DataOutputStream _buffer;

    private long _totalBytesWritten = 0;

    private final @NotNull Timer _forceFlushTimer;

    public TransmissionWriteStream(@NotNull OutputStream stream, @NotNull StreamBandwidth.Tracker.Write bandwidth) {
        _stream = stream;
        _bandwidth = bandwidth;
        _bufferStream = new ByteArrayOutputStream();
        _buffer = new DataOutputStream(_bufferStream);
        _forceFlushTimer = new Timer(bandwidth.getForceFlushTime());
    }

    // # TransmissionStream.Write

    @Override
    public long getTotalBytesWritten() {
        synchronized (lock) {
            return _totalBytesWritten;
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
    public void write(@NotNull byte[] bytes) {
        synchronized (lock) {
            try {
                writeToBufferCache(bytes);
            } catch (Exception e) {

            }
        }

        flush();
    }

    @Override
    public void flush() {
        synchronized (lock) {
            try {
                writeBufferToStream();
            } catch (Exception e) {

            }
        }
    }

    // # Internal

    private void writeToBufferCache(@NotNull byte[] bytes) throws Exception {
        _bufferStream.write(bytes);
    }

    private void writeBufferToStream() {
        try {
            while (writeNextChunkToStream()) {

            }
        } catch (Exception e) {

        }
    }

    // Returns true if another chunk can be written again, returns false otherwise.
    private boolean writeNextChunkToStream() throws Exception {
        _bufferStream.flush();

        int maxBytes = (int)_bandwidth.getFlushDataRate().inBytes();

        byte[] currentBytes = _bufferStream.toByteArray();

        if (currentBytes.length > maxBytes) {
            // Write next chunk
            _forceFlushTimer.reset();

            writeDataAndFlush(currentBytes, maxBytes, false);
        } else {
            // Force flush scenario
            if (!_forceFlushTimer.update()) {
                return false;
            }

            maxBytes = currentBytes.length;

            writeDataAndFlush(currentBytes, maxBytes, true);
        }

        return _bufferStream.toByteArray().length >= maxBytes;
    }

    private void writeDataAndFlush(@NotNull byte[] currentBytes, int maxBytes, boolean forceFlush) throws Exception {
        byte[] leftoverBytes = Arrays.copyOfRange(currentBytes, maxBytes, currentBytes.length);

        // Rewrite the buffer
        _bufferStream.reset();
        _buffer.write(leftoverBytes);

        // Write max number of bytes, starting from zero index
        currentBytes = Arrays.copyOfRange(currentBytes, 0, maxBytes);

        _stream.write(currentBytes, 0, currentBytes.length);
        _stream.flush();

        // Update bandwidth and total bytes
        _bandwidth.write(currentBytes.length);
        _totalBytesWritten += currentBytes.length;

        if (DEBUG_LOG) {
            if (!forceFlush) {
                Logger.message(this, "Writing " + currentBytes.length + " bytes and flushing");
            } else {
                Logger.message(this, "Writing " + currentBytes.length + " bytes and force flushing");
            }
        }
    }
}
