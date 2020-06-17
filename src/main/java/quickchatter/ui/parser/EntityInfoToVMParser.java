/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui.parser;

import org.jetbrains.annotations.NotNull;
import filesystem.model.EntityInfo;
import quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import utilities.Parser;

public class EntityInfoToVMParser implements Parser<EntityInfo, FileSystemEntityViewModel> {
    @Override
    public @NotNull FileSystemEntityViewModel parse(@NotNull EntityInfo info) throws Exception {
        boolean isDir = EntityInfo.Helper.isDirectory(info);
        String name = info.getPath().getLastComponent();
        String size = "";
        String dateCreated = "";
        String dateModified = "";

        if (info instanceof EntityInfo.Directory) {
            EntityInfo.Directory dir = (EntityInfo.Directory)info;

            isDir = true;
            size = dir.getSize().toString();
        }

        if (info instanceof EntityInfo.File) {
            EntityInfo.File file = (EntityInfo.File)info;

            isDir = false;
            size = file.getSize().toString();
        }

        return new FileSystemEntityViewModel(isDir, name, size, dateCreated, dateModified);
    }
}
