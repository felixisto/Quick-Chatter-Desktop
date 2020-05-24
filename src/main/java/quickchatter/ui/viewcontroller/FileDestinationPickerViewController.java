/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui.viewcontroller;

import org.jetbrains.annotations.NotNull;
import quickchatter.filesystem.fundamentals.FilePath;
import quickchatter.navigation.Router;
import quickchatter.presenter.BasePresenter;
import quickchatter.utilities.Callback;
import quickchatter.utilities.Path;
import quickchatter.utilities.SimpleCallback;

public class FileDestinationPickerViewController extends DirectoryPickerViewController {
    private @NotNull BasePresenter.FilePicker _filePickerPresenter;
    private @NotNull Router.Primary _router;
    private @NotNull Callback<Path> _pickCallback;
    
    // Hold strong reference to this
    private @NotNull BasePresenter.FileDestinationHandler _fileDestinationHandler;
    
    public FileDestinationPickerViewController(@NotNull BasePresenter.FilePicker filePickerPresenter,
            @NotNull BasePresenter.FileSystemNavigation navigationPresenter,
            @NotNull BasePresenter.FileSystemDirectory directoryPresenter,
            @NotNull Router.Primary router,
            @NotNull BasePresenter.FileDestinationHandler fileDestinationHandler,
            @NotNull Callback<Path> pickCallback,
            @NotNull SimpleCallback noPickCallback,
            @NotNull String description) {
        super(filePickerPresenter, navigationPresenter, directoryPresenter, router, pickCallback, noPickCallback, description);
        
        _filePickerPresenter = filePickerPresenter;
        _router = router;
        _pickCallback = pickCallback;
        _fileDestinationHandler = fileDestinationHandler;
    }
    
    // # PresenterDelegate.FilePicker

    @Override
    public void onPickFile(@NotNull FilePath path) throws Exception {
        // Override this and call this, because in FragmentDirectoryPicker this method does nothing
        _router.navigateBack();
        
        _pickCallback.perform(path);
    }
}
