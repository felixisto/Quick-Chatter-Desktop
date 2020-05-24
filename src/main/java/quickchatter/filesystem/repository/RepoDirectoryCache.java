package quickchatter.filesystem.repository;

import org.jetbrains.annotations.NotNull;

import quickchatter.filesystem.fundamentals.DirectoryPath;

import java.util.HashMap;

public class RepoDirectoryCache extends RepoCache<Repository.MutableDirectory, DirectoryPath> {
    public static @NotNull RepoDirectoryCache buildEmpty() {
        return new RepoDirectoryCache();
    }

    public static @NotNull RepoDirectoryCache build(@NotNull HashMap<String, Repository.MutableDirectory> cache) {
        return new RepoDirectoryCache(cache);
    }

    public static @NotNull RepoDirectoryCache buildWithInitial(@NotNull Repository.MutableDirectory initialCachedRepo) {
        RepoDirectoryCache cache = new RepoDirectoryCache();
        cache.cacheRepository(initialCachedRepo);
        return cache;
    }

    public RepoDirectoryCache() {
        this(new HashMap<String, Repository.MutableDirectory>());
    }

    public RepoDirectoryCache(@NotNull HashMap<String, Repository.MutableDirectory> cache) {
        super(cache);
    }

    public @NotNull CacheKeyToValueRelation<Repository.MutableDirectory, DirectoryPath> getRelation() {
        return new CacheKeyToValueRelation<Repository.MutableDirectory, DirectoryPath>() {
            @Override
            public @NotNull String buildCacheKeyFromRepo(@NotNull Repository.MutableDirectory repo) {
                return repo.getDirPath().getPath();
            }

            @Override
            public @NotNull String buildCacheKeyFromProperty(@NotNull DirectoryPath property) {
                return property.getPath();
            }
        };
    }
}
