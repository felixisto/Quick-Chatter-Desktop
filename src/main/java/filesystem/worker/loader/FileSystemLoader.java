package filesystem.worker.loader;

import org.jetbrains.annotations.NotNull;

import filesystem.FileSystemReader;
import filesystem.fundamentals.DirectoryPath;
import filesystem.fundamentals.FilePath;
import filesystem.model.DirectoryContents;
import filesystem.model.EntityInfo;
import utilities.DataSize;
import utilities.Path;

public interface FileSystemLoader {
    @NotNull FileSystemReader getReader();

    @NotNull EntityInfo.Directory readRootDirectoryInfo() throws Exception;

    @NotNull DirectoryContents readDirectoryContents(final @NotNull DirectoryPath path) throws Exception;

    @NotNull EntityInfo readEntityInfo(final @NotNull Path path) throws Exception;
    @NotNull EntityInfo.File readFileInfo(final @NotNull FilePath path) throws Exception;
    @NotNull EntityInfo.Directory readDirectoryInfo(final @NotNull DirectoryPath path) throws Exception;

    @NotNull DataSize readDirectorySize(@NotNull DirectoryPath path) throws Exception;

    @NotNull DataSize computeDirectorySize(@NotNull DirectoryContents contents);
}
