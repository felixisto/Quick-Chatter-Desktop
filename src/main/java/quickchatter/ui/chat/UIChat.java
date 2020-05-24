/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui.chat;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickchatter.utilities.StringUtilities;

public class UIChat {
    public final @NotNull String newLine = "\n";

    public final @NotNull String localName;
    public final @NotNull String clientName;
    public final boolean displayDate;
    public final int maxLines;

    private @NotNull String _log = "";

    private @Nullable String _bottomLine = null;

    public UIChat(@NotNull String localName, @NotNull String clientName, boolean displayDate, int maxLines) {
        this.localName = localName;
        this.clientName = clientName;
        this.displayDate = displayDate;
        this.maxLines = maxLines;
    }

    public @NotNull String getLog() {
        return _log;
    }

    public @NotNull String parseStringForSendMessage(@NotNull byte[] bytes) {
        String result = new String(bytes, Charset.defaultCharset());
        return buildTimestamp(localName) + result;
    }

    public @NotNull String parseStringForReceiveMessage(@NotNull byte[] bytes) {
        String result = new String(bytes, Charset.defaultCharset());
        return buildTimestamp(clientName) + result;
    }

    public void onMessageReceived(@NotNull String message) {
        addTextLine(message);
    }

    public void onMessageSent(@NotNull String message) {
        _log = _log.concat(newLine + message);
    }

    public void addTextLine(@NotNull String message) {
        // Remove first line if the max lines is exceeded
        if (numberOfTextLines() + 1 >= maxLines) {
            int firstNewLine = _log.indexOf(newLine);

            if (firstNewLine >= 0 && firstNewLine+1 < _log.length()) {
                _log = _log.substring(firstNewLine+1);
            }
        }

        if (!_log.isEmpty()) {
            _log = _log.concat(newLine + message);
        } else {
            _log = message;
        }
    }

    public @NotNull String buildTimestamp(@NotNull String name) {
        String timestamp = displayDate ? getCurrentTimestamp() + " " : "";

        timestamp = timestamp.concat(!name.isEmpty() ? name + ": " : "");

        return timestamp;
    }

    public @NotNull String getCurrentTimestamp() {
        String pattern = "HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        return simpleDateFormat.format(new Date());
    }

    public int numberOfTextLines() {
        return StringUtilities.occurrencesCount(_log, newLine);
    }

    public void setBottomLineText(@NotNull String message) {
        removeBottomLineText();

        if (message.isEmpty()) {
            return;
        }

        _bottomLine = message;

        _log = _log.concat(buildBottomLineText(message));
    }

    public void removeBottomLineText() {
        if (_bottomLine == null) {
            return;
        }

        _log = StringUtilities.replaceLast(_log, buildBottomLineText(_bottomLine), "");

        _bottomLine = null;
    }

    private @NotNull String buildBottomLineText(@NotNull String message) {
        return newLine + newLine  + message;
    }
}

