/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui.viewmodel;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class FileSystemEntityViewModel {
    public final boolean isDirectory;
    public final boolean isFile;
    public final @NotNull String name;
    public final @NotNull String size;
    public final @NotNull String dateCreated;
    public final @NotNull String dateModified;

    public FileSystemEntityViewModel(boolean isDirectory,
                                     @NotNull String name,
                                     @NotNull String size,
                                     @NotNull String dateCreated,
                                     @NotNull String dateModified) {
        this.isDirectory = isDirectory;
        this.isFile = !this.isDirectory;
        this.name = name;
        this.size = size;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof FileSystemEntityViewModel) {
            FileSystemEntityViewModel otherModel = (FileSystemEntityViewModel)object;

            if (isDirectory != otherModel.isDirectory || isFile != otherModel.isFile || !name.equals(otherModel.name)) {
                return false;
            }

            if (!size.equals(otherModel.size) || !dateCreated.equals(otherModel.dateCreated) || !dateModified.equals(otherModel.dateModified)) {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.isDirectory ? 1 : 0);
        hash = 23 * hash + (this.isFile ? 1 : 0);
        hash = 23 * hash + Objects.hashCode(this.name);
        hash = 23 * hash + Objects.hashCode(this.size);
        hash = 23 * hash + Objects.hashCode(this.dateCreated);
        hash = 23 * hash + Objects.hashCode(this.dateModified);
        return hash;
    }
}

