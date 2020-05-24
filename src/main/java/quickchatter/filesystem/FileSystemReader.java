package quickchatter.filesystem;

import org.jetbrains.annotations.NotNull;

import quickchatter.filesystem.fundamentals.DirectoryPath;
import quickchatter.filesystem.fundamentals.FileExtension;
import quickchatter.filesystem.fundamentals.FilePath;
import quickchatter.utilities.Path;
import java.nio.Buffer;
import java.util.List;

public interface FileSystemReader {
    @NotNull DirectoryPath getRootDirectory() throws Exception;

    boolean isEntityDirectory(@NotNull Path path);
    boolean isEntityFile(@NotNull Path path);

    @NotNull List<Path> contentsOfDirectory(@NotNull DirectoryPath path, @NotNull List<FileExtension> filterOut) throws Exception;

    long sizeOfFile(@NotNull FilePath path) throws Exception;

    @NotNull Buffer readFromFile(@NotNull FilePath path) throws Exception;
}
