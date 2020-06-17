/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.presenter;

import java.io.File;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import filesystem.fundamentals.FilePath;
import filesystem.model.EntityInfo;
import filesystem.worker.loader.FileSystemLoader;
import quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import utilities.Callback;
import utilities.Errors;
import utilities.Logger;
import utilities.LooperService;
import utilities.Parser;
import utilities.Path;
import utilities.SimpleCallback;

public class FileDestinationPickerPresenter extends FilePickerPresenter implements BasePresenter.FileDestinationPicker {
    private @Nullable BasePresenterDelegate.FilePicker _pickerDelegate;
    private @NotNull FileDestinationHandler _destinationHandler;
    private @NotNull String _name = "file";

    public FileDestinationPickerPresenter(@NotNull FileSystemLoader loader,
                                          @NotNull EntityInfo.Directory rootInfo,
                                          @NotNull Parser<EntityInfo, FileSystemEntityViewModel> parser,
                                          @NotNull FileDestinationHandler destinationHandler) {
        super(loader, rootInfo, parser);
        _destinationHandler = destinationHandler;
    }

    // # Presenter.FileDestinationPicker

    @Override
    public void start(@NotNull BasePresenterDelegate.FilePicker filePickerDelegate,
                      @NotNull BasePresenterDelegate.FileSystemNavigation navigationDelegate,
                      @NotNull BasePresenterDelegate.FileSystemDirectory directoryDelegate) throws Exception {
        super.start(filePickerDelegate, navigationDelegate, directoryDelegate);
        
        _pickerDelegate = filePickerDelegate;
    }

    @Override
    public void pickFile(@NotNull FileSystemEntityViewModel entity) {
        Path path = pathOfEntityInfoViewModel(entity);

        if (!(path instanceof FilePath) || entity.isDirectory) {
            Logger.error(this, "Cannot pick file, given model is invalid or corrupted");
            return;
        }

        pickFileWithPath((FilePath)path);
    }

    @Override
    public void pickDirectory(@NotNull FileSystemEntityViewModel entity) {
        final Path path = pathOfEntityInfoViewModel(entity);

        if (path == null || entity.isFile) {
            Logger.error(this, "Cannot pick directory, given model is invalid or corrupted");
            return;
        }

        Logger.message(this, "Picked destination directory " + path.toString() + ", please enter file name");

        String initialName = getName();

        _destinationHandler.onPickFileName(new Callback<String>() {
            @Override
            public void perform(String argument) {
                try {
                    setName(argument);
                    FilePath destinationPath = buildPath(path);

                    Logger.message(this, "Picked destination " + destinationPath);

                    // Now pick file, if chosen name overwrites something, the overwrite dialog
                    // will be handled properly
                    pickFileWithPath(destinationPath);
                } catch (Exception e) {
                    Logger.warning(this, "Invalid file name, cannot pick destination, error: " + e);
                }
            }
        }, initialName);
    }
    
    @Override
    public @NotNull FileDestinationHandler getDestinationHandler() {
        return _destinationHandler;
    }

    @Override
    public @NotNull FilePath buildPath(@NotNull Path base) {
        return new FilePath(base, getName());
    }

    @Override
    public boolean isPathAvailable(@NotNull FilePath path) {
        String pathString = path.getPath();

        if (pathString.isEmpty()) {
           return false;
        }

        File file = new File(pathString);
        return !file.exists();
    }

    @Override
    public @NotNull String getName() {
        return _name;
    }

    @Override
    public void setName(@NotNull String name) throws Exception {
        if (name.isEmpty()) {
            Errors.throwInvalidArgument("Name cannot be empty");
        }

        _name = name;
    }
    
    // # Internals

    private void pickFileWithPath(@NotNull final FilePath path) {
        if (isPathAvailable(path)) {
            chooseFile(path);
            return;
        }

        Logger.message(this, "Picked file " + path.toString() + " already exists, overwrite?");

        _destinationHandler.onFileOverwrite(new Callback<Boolean>() {
            @Override
            public void perform(Boolean confirm) {
                if (confirm) {
                    chooseFile(path);
                } else {
                    Logger.message(this, "Cancel overwrite file");
                }
            }
        });
    }

    private void chooseFile(final @NotNull FilePath path) {
        Logger.message(this, "Pick file " + path.toString());

        LooperService.getShared().performOnAWT(new SimpleCallback() {
            @Override
            public void perform() {
                if (_pickerDelegate == null) {
                    return;
                }

                try {
                    _pickerDelegate.onPickFile(path);
                } catch (Exception e) {

                }
            }
        });
    }
}

