package filesystem;

import org.jetbrains.annotations.NotNull;

import filesystem.fundamentals.FilePath;

import java.nio.Buffer;

public interface FileSystemWriter {
    void writeToFile(@NotNull FilePath path, @NotNull Buffer data) throws Exception;
}
