package filesystem.simple;

import org.jetbrains.annotations.NotNull;

import filesystem.FileSystemReader;
import filesystem.FileSystemWriter;
import filesystem.fundamentals.DirectoryPath;
import filesystem.fundamentals.FileExtension;
import filesystem.fundamentals.FilePath;
import utilities.Logger;
import utilities.Path;

import java.io.File;
import java.net.URI;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

public class SimpleFileSystem implements FileSystemReader, FileSystemWriter {
    public DirectoryPath getAppDirectory() {
        try {
            String path = new File(".").getCanonicalPath();
            return new DirectoryPath(path);
        } catch (Exception e) {
            Logger.error(this, "Failed to get app directory, error: " + e.toString());
            System.exit(0);
        }
        
        return null;
    }

    public DirectoryPath getDataDirectory() {
        try {
            String path = new File(".").getCanonicalPath();
            return new DirectoryPath(path);
        } catch (Exception e) {
            Logger.error(this, "Failed to get app data directory, error: " + e.toString());
            System.exit(0);
        }
        
        return null;
    }

    // # FileSystemReader

    @Override
    public @NotNull DirectoryPath getRootDirectory() {
        return getAppDirectory();
    }

    @Override
    public boolean isEntityDirectory(@NotNull Path path) {
        String pathAsString = path.getPath();

        if (pathAsString == null) {
            return false;
        }

        return new File(pathAsString).isDirectory();
    }

    @Override
    public boolean isEntityFile(@NotNull Path path) {
        return !isEntityDirectory(path);
    }

    @Override
    public @NotNull List<Path> contentsOfDirectory(@NotNull DirectoryPath path, @NotNull List<FileExtension> filterOut) throws Exception {
        String pathAsString = path.getPath();

        if (pathAsString == null) {
            return new ArrayList<>();
        }

        File directory = new File(pathAsString);
        File[] files = directory.listFiles();

        ArrayList<Path> paths =  new ArrayList<>();

        if (files == null) {
            return paths;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                paths.add(new DirectoryPath(file.getPath()));
            } else {
                paths.add(new FilePath(file.getPath()));
            }
        }

        return paths;
    }

    @Override
    public long sizeOfFile(@NotNull FilePath path) throws Exception {
        String pathAsString = path.getPath();

        return new File(pathAsString).length();
    }

    public @NotNull List<Path> contentsOfDirectory(@NotNull DirectoryPath path) throws Exception {
        return contentsOfDirectory(path, new ArrayList<FileExtension>());
    }

    @Override
    public @NotNull Buffer readFromFile(@NotNull FilePath path) throws Exception
    {
        return CharBuffer.allocate(100);
    }

    // # FileSystemWriter

    @Override
    public void writeToFile(@NotNull FilePath path, @NotNull Buffer data) throws Exception {

    }
}
