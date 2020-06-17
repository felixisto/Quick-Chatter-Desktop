package filesystem.fundamentals;

import utilities.Path;
import utilities.PathBuilder;
import org.jetbrains.annotations.NotNull;
import java.net.URI;
import java.util.Objects;
import utilities.FileSystem;

public class DirectoryPath implements Path {
    private final @NotNull URI _url;

    public DirectoryPath(@NotNull URI url) {
        this._url = url;
    }

    public DirectoryPath(@NotNull Path path) {
        this._url = path.getURL();
    }
    
    public DirectoryPath(@NotNull String path) {
        this(new PathBuilder(path).get());
    }

    @Override
    public @NotNull String toString() {
        return getPath();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Path) {
            return ((Path)other).getURL().equals(_url);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this._url);
        return hash;
    }

    @Override
    public @NotNull URI getURL() {
        return _url;
    }
    
    @Override
    public @NotNull String getPath() {
        String path = PathBuilder.convertURIToFilePath(_url);
        return path != null ? path : "";
    }

    @Override
    public @NotNull String getLastComponent() {
        PathBuilder builder = new PathBuilder(this);
        return builder.getLastComponent();
    }
    
    @Override
    public @NotNull String getSeparator() {
        return FileSystem.getDirectorySeparator();
    }

    public @NotNull DirectoryPath pathWithoutLastComponent() {
        PathBuilder builder = new PathBuilder(this);
        builder.removeLastComponent();
        return new DirectoryPath(builder.get());
    }
}
