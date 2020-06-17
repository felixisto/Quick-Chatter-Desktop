package filesystem.fundamentals;

import org.jetbrains.annotations.NotNull;

public enum FileExtension {
    unknown, txt, pdf, doc, dox, jpeg, png, gif, mp3, mp4, avi, mkv, wav, webm, flv, temp, xml;

    public @NotNull String asString() {
        if (this == unknown) {
            return "";
        }

        return this.name();
    }
}
