package filesystem.repository;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import filesystem.fundamentals.DirectoryPath;
import filesystem.model.DirectoryContents;
import utilities.DataSize;

// Holds business logic data.
// Usually written to by use cases and read by view models.
// Thread safe: yes
public interface Repository {
    // A repository that can be deep copied and assigned to another prototype of the same type.
    interface Copyable <T> extends Repository {
        @NotNull T copy();
    }

    // A repository that can be deep copied and assigned to another prototype of the same type.
    interface Assignable <T> extends Repository {
        void assign(@NotNull T prototype);
    }

    // A repository that can be deep copied and assigned to another prototype of the same type.
    interface CopyableAndAssignable <T> extends Repository.Copyable <T>, Repository.Assignable <T> {

    }

    // A repository cache, that contains copies of already loaded copies of repositories.
    // Each repository copy is identified by a key string.
    interface Cache <T, C> extends Repository {
        @NotNull CacheKeyToValueRelation<T, C> getRelation();

        @Nullable T cachedRepository(@NotNull String key);
        void cacheRepository(@NotNull T repo);
    }

    // Maps keys to values.
    interface CacheKeyToValueRelation<T, C> {
        @NotNull String buildCacheKeyFromRepo(@NotNull T repo);
        @NotNull String buildCacheKeyFromProperty(@NotNull C property);
    }

    interface Directory extends Repository {
        boolean isRoot();
        @NotNull DirectoryPath getDirPath();
        @NotNull String getAbsolutePath();
        @NotNull String getName();
        @NotNull DataSize getSize();
        @NotNull DirectoryContents getContents();
    }

    interface MutableDirectory extends Directory, Repository.Copyable<MutableDirectory>, Repository.Assignable<Directory> {

    }
}
