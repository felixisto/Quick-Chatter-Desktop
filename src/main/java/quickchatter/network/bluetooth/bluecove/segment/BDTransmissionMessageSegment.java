/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.bluecove.segment;

/// Wraps a buffer of bytes and provides read functionality transmission messages that are stored as segments.

import java.nio.charset.Charset;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickchatter.network.basic.TransmissionType;
import quickchatter.network.bluetooth.basic.BEError;
import quickchatter.utilities.DataSize;
import quickchatter.utilities.StringFormatting;

/// When transmitting a message, the message is converted to bytes, and then back. This class is used
/// to read the bytes segment.
/// If the given buffer contains multiple segments, only the first segment is read while the others are
/// ignored.
///
/// The structure of a message segment:
// ![TYPE<!class!><!header!>value]!
/// The length of the type is fixed.
/// The length of the segment is fixed.
/// The length of the value is determined by the header value (contains bytes value).
/// The length of the header is either 0 or fixed.
/// Start messages are defined class value 0 and have no value.
/// Data messages are defined class value 1.
/// End messages are defined class value 2 and have no value and no header.
/// Ping messages are defined class value 2 and have no value and no header.
public class BDTransmissionMessageSegment {
    public static final int SEPARATOR_LENGTH = 2;
    public static final int TOTAL_SEPARATOR_LENGTH = (SEPARATOR_LENGTH * 6);
    public static final @NotNull String MESSAGE_START_SEPARATOR = "![";
    public static final int MESSAGE_TYPE_LENGTH = 4;
    public static final @NotNull String MESSAGE_CLASS_START_SEPARATOR = "<!";
    public static final @NotNull String MESSAGE_CLASS_END_SEPARATOR = "!>";
    public static final int MESSAGE_CLASS_LENGTH = 1;
    public static final @NotNull String MESSAGE_HEADER_START_SEPARATOR = "<!";
    public static final @NotNull String MESSAGE_HEADER_END_SEPARATOR = "!>";
    public static final int MESSAGE_HEADER_LENGTH = 16;
    public static final int MESSAGE_CHUNK_MAX_SIZE = (int) DataSize.buildKB(1).inBytes();
    public static final @NotNull String MESSAGE_END_SEPARATOR = "]!";
    public static final int MIN_LENGTH = MESSAGE_TYPE_LENGTH + MESSAGE_CLASS_LENGTH + TOTAL_SEPARATOR_LENGTH;

    public static final @NotNull String CLASS_VALUE_START = "0";
    public static final @NotNull String CLASS_VALUE_DATA = "1";
    public static final @NotNull String CLASS_VALUE_END = "2";
    public static final @NotNull String CLASS_VALUE_PING = "3";

    private static final @NotNull byte[] MESSAGE_START_SEPARATOR_BYTES = stringToBytes(MESSAGE_START_SEPARATOR);
    private static final @NotNull byte[] MESSAGE_END_SEPARATOR_BYTES = stringToBytes(MESSAGE_END_SEPARATOR);

    public final @NotNull byte[] data;
    public final @NotNull String dataAsString;

    public static @NotNull BDTransmissionMessageSegment build(@NotNull byte[] bytes) {
        return new BDTransmissionMessageSegment(bytes);
    }

    public static @NotNull String bytesToString(@NotNull byte[] bytes) {
        String result = new String(bytes, Charset.defaultCharset());
        return result;
    }

    public static @NotNull byte[] stringToBytes(@NotNull String string) {
        byte[] result = string.getBytes(Charset.defaultCharset());
        return result;
    }

    BDTransmissionMessageSegment(@NotNull byte[] data) {
        this.data = data;
        this.dataAsString = bytesToString(data);
    }

    // Returns true if the data contains a valid segment.
    public boolean isValid() {
        return length() > 0;
    }

    public boolean isStartClass() {
        byte[] value = classValue();

        if (value.length != 1) {
            return false;
        }

        return bytesToString(value).equals(CLASS_VALUE_START);
    }

    public boolean isDataClass() {
        byte[] value = classValue();

        if (value.length != 1) {
            return false;
        }

        return bytesToString(value).equals(CLASS_VALUE_DATA);
    }

    public boolean isEndClass() {
        byte[] value = classValue();

        if (value.length != 1) {
            return false;
        }

        return bytesToString(value).equals(CLASS_VALUE_END);
    }

    public boolean isPingClass() {
        byte[] value = classValue();

        if (value.length != 1) {
            return false;
        }

        return bytesToString(value).equals(CLASS_VALUE_PING);
    }

    // The entire segment length, including the start and end separators.
    // Note: this is NOT endIndex() - startIndex(), those calculations do not account for the separators length.
    public int length() {
        int start = startIndex();
        int end = endIndex();

        if (start < 0 || end < 0) {
            return 0;
        }

        if (indexOfBytes(end, MESSAGE_END_SEPARATOR_BYTES) == -1) {
            return 0;
        }

        return end - start + SEPARATOR_LENGTH;
    }

    public int valueLength() {
        try {
            return headerValueAsSizeValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public @Nullable TransmissionType getType() {
        int start = startIndex();

        if (start < 0) {
            return null;
        }

        start += SEPARATOR_LENGTH;

        int end = start + MESSAGE_TYPE_LENGTH;

        if (end >= data.length) {
            return null;
        }

        try {
            return new TransmissionType(bytesToString(Arrays.copyOfRange(data, start, end)));
        } catch (Exception e) {

        }

        return null;
    }

    public int startIndex() {
        return indexOfBytes(MESSAGE_START_SEPARATOR_BYTES);
    }

    public int endIndex() {
        return valueEndIndex();
    }

    public int classLength() {
        int start = classStartIndex();
        int end = classEndIndex();

        if (start < 0 || end < 0) {
            return 0;
        }

        return end - start;
    }

    public int classStartIndex() {
        int start = startIndex();

        if (start < 0) {
            return start;
        }

        return start + SEPARATOR_LENGTH + MESSAGE_TYPE_LENGTH + SEPARATOR_LENGTH;
    }

    public int classEndIndex() {
        int start = classStartIndex();

        if (start < 0) {
            return start;
        }

        return start + 1;
    }

    public int headerLength() {
        if (isEndClass() || isPingClass()) {
            return 0;
        }

        return MESSAGE_HEADER_LENGTH;
    }

    public int headerStartIndex() {
        int start = classEndIndex();

        if (start < 0) {
            return start;
        }

        return start + SEPARATOR_LENGTH + SEPARATOR_LENGTH;
    }

    public int headerEndIndex() {
        int start = headerStartIndex();

        if (start < 0) {
            return start;
        }

        return start + headerLength();
    }

    public int valueStartIndex() {
        int end = headerEndIndex();

        if (end < 0) {
            return end;
        }

        return end + SEPARATOR_LENGTH;
    }

    public int valueEndIndex() {
        // Non-data classes have no value, so just jump to the index
        if (!isDataClass()) {
            int headerEnd = headerEndIndex();

            if (headerEnd < 0) {
                return headerEnd;
            }

            return headerEnd + SEPARATOR_LENGTH;
        }

        try {
            int start = valueStartIndex();

            if (start < 0) {
                return start;
            }

            return start + headerValueAsSizeValue();
        } catch (Exception e) {
            return -1;
        }
    }

    public byte[] headerValue() {
        int start = headerStartIndex();
        int end = headerEndIndex();

        if (start < 0 || end < 0 || start == end) {
            return new byte[0];
        }

        return Arrays.copyOfRange(this.data, start, end);
    }

    public int headerValueAsSizeValue() throws BEError {
        byte[] value = headerValue();
        String valueAsString = BDTransmissionMessageSegment.bytesToString(value);

        if (!StringFormatting.isStringADouble(valueAsString)) {
            throw new BEError(BEError.Value.corruptedStreamDataHeader);
        }

        int expectedLength = (int)Double.parseDouble(valueAsString);

        if (expectedLength < 0) {
            throw new BEError(BEError.Value.corruptedStreamDataHeader);
        }

        return expectedLength;
    }

    public byte[] classValue() {
        int start = classStartIndex();
        int end = classEndIndex();

        if (start < 0 || end < 0 || end - start != 1) {
            return new byte[0];
        }

        return Arrays.copyOfRange(this.data, start, end);
    }

    public byte[] value() {
        int start = valueStartIndex();
        int end = valueEndIndex();

        if (start < 0 || end < 0 || start == end) {
            return new byte[0];
        }

        return Arrays.copyOfRange(this.data, start, end);
    }

    private int indexOfBytes(@NotNull byte[] bytes) {
        return indexOfBytes(0, bytes);
    }

    private int indexOfBytes(int startIndex, @NotNull byte[] bytes) {
        if (startIndex < 0) {
            return -1;
        }

        if (bytes.length == 0) {
            return -1;
        }

        for (int e = startIndex; e < data.length; e++) {
            if (data[e] == bytes[0] && e + bytes.length <= data.length) {
                boolean match = true;

                for (int i = 1; i < bytes.length; i++) {
                    byte other = bytes[i];

                    if (data[e+i] != other) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    return e;
                }
            }
        }

        return -1;
    }
}
