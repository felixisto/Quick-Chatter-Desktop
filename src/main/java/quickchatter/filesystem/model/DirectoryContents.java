package quickchatter.filesystem.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import quickchatter.utilities.CollectionUtilities;

import java.util.ArrayList;
import java.util.List;

// A list of directory entities (files and directories).
// Thread safe: yes (immutable)
public class DirectoryContents {
    private final @NotNull List<EntityInfo> _entities;

    public static @NotNull DirectoryContents buildBlank() {
        return new DirectoryContents();
    }

    public DirectoryContents() {
        this._entities = new ArrayList<EntityInfo>();
    }

    public DirectoryContents(@Nullable List<EntityInfo> entities) {
        this._entities = entities != null ? CollectionUtilities.copy(entities) : new ArrayList<EntityInfo>();
    }

    public @NotNull List<EntityInfo> entitiesCopy() {
        return CollectionUtilities.copy(_entities);
    }

    public int size() {
        return _entities.size();
    }

    public @NotNull EntityInfo get(int index) {
        return _entities.get(index);
    }
}
