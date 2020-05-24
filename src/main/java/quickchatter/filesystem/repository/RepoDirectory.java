package quickchatter.filesystem.repository;

import org.jetbrains.annotations.NotNull;

import quickchatter.filesystem.fundamentals.DirectoryPath;
import quickchatter.filesystem.model.DirectoryContents;
import quickchatter.filesystem.model.DirectoryInfo;
import quickchatter.filesystem.model.EntityInfo;
import quickchatter.utilities.DataSize;

// Mutable model of a directory.
// Essentially wraps a EntityInfo.Directory and allows to modify it safely.
// Thread safe: yes (lock)
public class RepoDirectory implements Repository.MutableDirectory {
    private final @NotNull Object lock = new Object();

    private @NotNull EntityInfo.Directory _info;

    public RepoDirectory(@NotNull EntityInfo.Directory info) {
        _info = info;
    }

    public RepoDirectory(@NotNull Repository.MutableDirectory prototype) {
        this(getInfoFrom(prototype));
    }
    
    public @NotNull EntityInfo.Directory getInfo() {
        return _info;
    }

    // # Repository.MutableDirectory

    @Override
    public @NotNull Repository.MutableDirectory copy() {
        EntityInfo.Directory info;

        synchronized (lock) {
            info = this._info;
        }

        return new RepoDirectory(info);
    }

    @Override
    public boolean isRoot() {
        synchronized (lock) {
            return _info.isRoot();
        }
    }

    @Override
    public @NotNull DirectoryPath getDirPath() {
        synchronized (lock) {
            return _info.getDirPath();
        }
    }

    @Override
    public @NotNull String getAbsolutePath() {
        return getDirPath().getPath();
    }

    @Override
    public @NotNull String getName() {
        return getDirPath().getLastComponent();
    }

    @NotNull
    public @Override DataSize getSize() {
        synchronized (lock) {
            return _info.getSize();
        }
    }

    @Override
    public @NotNull DirectoryContents getContents() {
        synchronized (lock) {
            return _info.getContents();
        }
    }

    @Override
    public void assign(@NotNull Repository.Directory prototype) {
        DirectoryInfo info = getInfoFrom(prototype);

        synchronized (lock) {
            this._info = info;
        }
    }

    // # Utilities

    private static @NotNull DirectoryInfo getInfoFrom(@NotNull Repository.Directory prototype) {
        return new DirectoryInfo(prototype.getDirPath(), prototype.getSize(), prototype.getContents(), prototype.isRoot());
    }
}
