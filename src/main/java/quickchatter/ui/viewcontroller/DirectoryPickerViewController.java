/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui.viewcontroller;

import org.jetbrains.annotations.NotNull;
import filesystem.fundamentals.DirectoryPath;
import filesystem.fundamentals.FilePath;
import quickchatter.navigation.Router;
import quickchatter.presenter.BasePresenter;
import quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import utilities.Callback;
import utilities.Path;
import utilities.SimpleCallback;

public class DirectoryPickerViewController extends FilePickerViewController {
    private @NotNull BasePresenter.FilePicker _filePickerPresenter;
    private @NotNull Router.Primary _router;
    private @NotNull Callback<Path> _pickCallback;
    
    public DirectoryPickerViewController(@NotNull BasePresenter.FilePicker filePickerPresenter,
            @NotNull BasePresenter.FileSystemNavigation navigationPresenter,
            @NotNull BasePresenter.FileSystemDirectory directoryPresenter,
            @NotNull Router.Primary router,
            @NotNull Callback<Path> pickCallback,
            @NotNull SimpleCallback noPickCallback,
            @NotNull String description) {
        super(filePickerPresenter, navigationPresenter, directoryPresenter, router, pickCallback, noPickCallback, description);
        
        _filePickerPresenter = filePickerPresenter;
        _router = router;
        _pickCallback = pickCallback;
    }
    
    // # FragmentFilePicker basic functionality
    
    @Override
    public void onItemPickClick(int index) {
        // If clicked entity is directory, pick it
        FileSystemEntityViewModel entity = entityAt(index);
        
        if (entity == null) {
            return;
        }

        if (entity.isDirectory) {
            _filePickerPresenter.pickDirectory(entity);
        }
    }

    @Override
    public void onNoItemPickClick() {
        try {
            _filePickerPresenter.pickDirectory(_filePickerPresenter.getSystemDirectorySubpresenter().getCurrentDirectory());
        } catch (Exception e) {

        }
    }
    
    // # PresenterDelegate.FilePicker

    @Override
    public void onPickFile(@NotNull FilePath path) throws Exception {

    }

    @Override
    public void onPickDirectory(@NotNull DirectoryPath path) throws Exception {
        _router.navigateBack();

        _pickCallback.perform(path);
    }
}
