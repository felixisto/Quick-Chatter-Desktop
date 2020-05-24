package quickchatter.filesystem.worker.loader;

import org.jetbrains.annotations.NotNull;
import quickchatter.filesystem.FileSystemReader;
import quickchatter.filesystem.fundamentals.DirectoryPath;
import quickchatter.filesystem.fundamentals.FileExtension;
import quickchatter.filesystem.fundamentals.FilePath;
import quickchatter.filesystem.model.DirectoryContents;
import quickchatter.filesystem.model.DirectoryInfo;
import quickchatter.filesystem.model.EntityInfo;
import quickchatter.filesystem.model.FileInfo;
import quickchatter.utilities.Errors;
import quickchatter.utilities.DataSize;
import quickchatter.utilities.Path;

import java.util.ArrayList;
import java.util.List;

// A file system loader for the local device disk.
public class FileSystemLoaderLocal implements FileSystemLoader {
    private final @NotNull FileSystemReader _reader;

    public FileSystemLoaderLocal(@NotNull FileSystemReader reader) {
        this._reader = reader;
    }

    @Override
    public @NotNull FileSystemReader getReader() {
        return _reader;
    }

    @Override
    public @NotNull EntityInfo.Directory readRootDirectoryInfo() throws Exception {
        return readDirectoryInfo(_reader.getRootDirectory());
    }

    @Override
    public @NotNull DirectoryContents readDirectoryContents(final @NotNull DirectoryPath path) throws Exception {
        List<Path> paths = _reader.contentsOfDirectory(path, new ArrayList<FileExtension>());
        ArrayList<EntityInfo> entities = new ArrayList<>();

        for (Path p : paths) {
            if (path.equals(p)) {
                Errors.throwIllegalStateError("Directory trying to load itself");
            }

            try {
                entities.add(readEntityInfo(p));
            } catch (Exception e) {

            }
        }

        return new DirectoryContents(entities);
    }

    @Override
    public @NotNull EntityInfo readEntityInfo(final @NotNull Path path) throws Exception {
        if (_reader.isEntityDirectory(path)) {
            return readDirectoryInfo(new DirectoryPath(path));
        }

        return readFileInfo(new FilePath(path));
    }

    @Override
    public @NotNull EntityInfo.File readFileInfo(final @NotNull FilePath path) throws Exception {
        DataSize size = DataSize.buildBytes(_reader.sizeOfFile(path));
        FileInfo info = new FileInfo(path, size);

        return info;
    }

    @Override
    public @NotNull EntityInfo.Directory readDirectoryInfo(final @NotNull DirectoryPath path) throws Exception {
        DirectoryContents contents = readDirectoryContents(path);
        boolean isRoot = path.equals(_reader.getRootDirectory());
        return new DirectoryInfo(path, computeDirectorySize(contents), contents, isRoot);
    }

    @Override
    public @NotNull DataSize readDirectorySize(@NotNull DirectoryPath path) throws Exception {
        DirectoryContents contents = readDirectoryContents(path);
        return computeDirectorySize(contents);
    }

    @Override
    public @NotNull DataSize computeDirectorySize(@NotNull DirectoryContents contents) {
        long totalSize = 0;

        for (EntityInfo info : contents.entitiesCopy()) {
            totalSize += info.getSize().inBytes();
        }

        return DataSize.buildBytes(totalSize);
    }
}
