package quickchatter.filesystem.model;

import org.jetbrains.annotations.NotNull;

import quickchatter.filesystem.fundamentals.DirectoryPath;
import quickchatter.filesystem.repository.Repository;
import quickchatter.utilities.DataSize;
import quickchatter.utilities.Path;

// Immutable model of a directory.
// Thread safe: yes (immutable)
public class DirectoryInfo implements EntityInfo.Directory {
    final @NotNull DirectoryPath path;
    final @NotNull DataSize size;
    final @NotNull DirectoryContents contents;
    final boolean isRoot;

    public DirectoryInfo(@NotNull DirectoryPath path, @NotNull DataSize size, @NotNull DirectoryContents contents) {
        this(path, size, contents, false);
    }

    public DirectoryInfo(@NotNull DirectoryPath path, @NotNull DataSize size, @NotNull DirectoryContents contents, boolean isRoot) {
        this.path = path;
        this.size = size;
        this.contents = contents;
        this.isRoot = isRoot;
    }

    public DirectoryInfo(@NotNull Repository.Directory prototype) {
        this(new DirectoryPath(prototype.getDirPath()), prototype.getSize(), prototype.getContents(), prototype.isRoot());
    }

    public DirectoryInfo(@NotNull EntityInfo.Directory info) {
        this(new DirectoryPath(info.getPath()), info.getSize(), info.getContents(), info.isRoot());
    }

    // # EntityInfo.Directory

    @Override
    public boolean isRoot() {
        return isRoot;
    }

    @Override
    public @NotNull Path getPath() {
        return getDirPath();
    }

    @Override
    public @NotNull DirectoryPath getDirPath() {
        return path;
    }

    @Override
    public @NotNull DataSize getSize() {
        return size;
    }

    @Override
    public @NotNull DirectoryContents getContents() {
        return contents;
    }
}
