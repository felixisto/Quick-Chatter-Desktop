package quickchatter.filesystem.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import quickchatter.filesystem.fundamentals.DirectoryPath;
import quickchatter.filesystem.fundamentals.FilePath;
import quickchatter.utilities.DataSize;
import quickchatter.utilities.Path;

import java.util.Date;

// A directory or a file.
// Thread safe: yes
public interface EntityInfo {
    @NotNull Path getPath();
    @NotNull DataSize getSize();

    interface Directory extends EntityInfo {
        boolean isRoot();
        @NotNull DirectoryPath getDirPath();
        @NotNull DirectoryContents getContents();
    }
    interface File extends EntityInfo {
        @NotNull FilePath getFilePath();
        @Nullable Date getDateCreated();
        @Nullable Date getDateModified();
    }

    class Helper {
        public static boolean isDirectory(@NotNull EntityInfo info) {
            return info instanceof Directory;
        }

        public static boolean isFile(@NotNull EntityInfo info) {
            return info instanceof File;
        }
    }
}
