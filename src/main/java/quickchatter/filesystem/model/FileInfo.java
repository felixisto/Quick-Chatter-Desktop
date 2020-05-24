package quickchatter.filesystem.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import quickchatter.filesystem.fundamentals.FilePath;
import quickchatter.utilities.DataSize;
import quickchatter.utilities.Path;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

// Mutable information source of a file.
// Thread safe: yes
public class FileInfo implements EntityInfo.File {
    public @NotNull AtomicReference<FilePath> path = new AtomicReference<>();
    public @NotNull AtomicReference<DataSize> size = new AtomicReference<>();
    public @NotNull AtomicReference<Date> dateCreated = new AtomicReference<>();
    public @NotNull AtomicReference<Date> dateModified = new AtomicReference<>();

    public FileInfo(@NotNull FilePath path, @NotNull DataSize size) {
        this.path.set(path);
        this.size.set(size);
    }

    @Override
    public @NotNull Path getPath() {
        return path.get();
    }

    @Override
    public @NotNull FilePath getFilePath() {
        return path.get();
    }

    @Override
    public @NotNull DataSize getSize() {
        return size.get();
    }

    @Override
    public @Nullable Date getDateCreated() {
        return dateCreated.get();
    }

    @Override
    public @Nullable Date getDateModified() {
        return dateModified.get();
    }
}