package quickchatter.filesystem.worker.loader;

import org.jetbrains.annotations.NotNull;

import quickchatter.filesystem.FileSystemReader;
import quickchatter.filesystem.fundamentals.DirectoryPath;
import quickchatter.filesystem.fundamentals.FilePath;
import quickchatter.filesystem.model.DirectoryContents;
import quickchatter.filesystem.model.EntityInfo;
import quickchatter.utilities.DataSize;
import quickchatter.utilities.Path;

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
