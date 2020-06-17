/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui.viewcontroller;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import filesystem.fundamentals.DirectoryPath;
import filesystem.fundamentals.FilePath;
import quickchatter.mvp.MVP;
import quickchatter.navigation.Router;
import quickchatter.presenter.BasePresenter;
import quickchatter.ui.listdata.FileSystemEntitiesTableData;
import quickchatter.ui.view.BaseView;
import quickchatter.ui.view.FilePickerFrame;
import quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import utilities.Callback;
import utilities.Logger;
import utilities.Path;
import utilities.SimpleCallback;

public class FilePickerViewController implements BaseViewController.FilePicker {
    public static final int MAX_ENTITIES_ROWS = 256;
    public static final int MAX_ENTITIES_COLUMNS = 2;
    
    private final @NotNull BasePresenter.FilePicker _filePickerPresenter;
    private final @NotNull BasePresenter.FileSystemNavigation _navigationPresenter;
    private final @NotNull BasePresenter.FileSystemDirectory _directoryPresenter;
    private final @NotNull Router.Primary _router;
    private final @NotNull FilePickerFrame _view;
    
    private final @NotNull Callback<Path> _pickCallback;
    private final @NotNull SimpleCallback _noPickCallback;
    private final @NotNull String _description;
    
    private @NotNull List<FileSystemEntityViewModel> _entities = new ArrayList<>();
    
    public FilePickerViewController(@NotNull BasePresenter.FilePicker filePickerPresenter,
            @NotNull BasePresenter.FileSystemNavigation navigationPresenter,
            @NotNull BasePresenter.FileSystemDirectory directoryPresenter,
            @NotNull Router.Primary router,
            @NotNull Callback<Path> pickCallback,
            @NotNull SimpleCallback noPickCallback,
            @NotNull String description) {
        _filePickerPresenter = filePickerPresenter;
        _navigationPresenter = navigationPresenter;
        _directoryPresenter = directoryPresenter;
        _router = router;
        
        _view = new FilePickerFrame(true);
        _view.setDescription(description);
        
        _pickCallback = pickCallback;
        _noPickCallback = noPickCallback;
        _description = description;
    }
    
    private void setupView() {
        _view.onQuitClick = new SimpleCallback() {
            @Override
            public void perform() {
                onCloseWithoutPicking();
            }
        };
        
        _view.onBackClick = new SimpleCallback() {
            @Override
            public void perform() {
                _navigationPresenter.onNavigateBack();
            }
        };
        
        _view.onItemDoubleClick = new Callback<Integer>() {
            @Override
            public void perform(Integer index) {
                if (index >= 0 && index < _entities.size()) {
                    onItemClick(index);
                }
            }
        };
        
        _view.onPickClick = new Callback<Integer>() {
            @Override
            public void perform(Integer index) {
                if (_view.getSelectedIndex() >= 0) {
                    onItemClick(index);
                } else {
                    onPickClick();
                }
            }
        };
    }
    
    // # FragmentFilePicker user events

    public boolean isSelectButtonEnabled() {
        return true;
    }

    public void onCloseClick() {
        _filePickerPresenter.closeWithoutPick();
    }

    public void onPickClick() {
        // Do nothing by default
    }

    public void onItemClick(int index) {
        // If clicked entity is directory, navigate
        BasePresenter.FileSystemDirectory dirPresenter = _filePickerPresenter.getSystemDirectorySubpresenter();
        FileSystemEntityViewModel entity = dirPresenter.getEntityInfoAt(index);

        if (entity == null) {
            return;
        }

        if (entity.isDirectory) {
            dirPresenter.navigateToEntityAt(index);
        } else {
            onPickFileEntity(entity);
        }
    }

    public void onPickFileEntity(@NotNull FileSystemEntityViewModel entity) {
        _filePickerPresenter.pickFile(entity);
    }
    
    // # BaseViewController.FilePicker

    @Override
    public MVP.View getView() {
        return _view;
    }

    @Override
    public MVP.Presenter getPresenter() {
        return _filePickerPresenter;
    }
    
    @Override
    public @NotNull BaseView getViewAsBaseView() {
        return _view;
    }

    @Override
    public void navigateBack() {
        onCloseWithoutPicking();
    }

    @Override
    public void onStart() {
        setupView();
        
        _view.onStart();
        
        try {
            _filePickerPresenter.start(this, this, this);
        } catch (Exception e) {
            Logger.error(this, "Failed to start, error: " + e.toString());
        }
    }

    @Override
    public void onSuspended() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onTerminate() {
        _view.onTerminate();
    }

    @Override
    public void onCloseWithoutPicking() {
        _router.navigateBack();
        
        _noPickCallback.perform();
    }

    @Override
    public void onPickFile(@NotNull FilePath path) throws Exception {
        _router.navigateBack();
        
        _pickCallback.perform(path);
    }

    @Override
    public void onPickDirectory(@NotNull DirectoryPath path) throws Exception {
        // Do nothing, this view controller picks only files
    }

    @Override
    public void setCurrentPath(@NotNull DirectoryPath path) {
        Path rootPath = _navigationPresenter.getRootDirectoryPath();
        String rootPathAsString = rootPath.toString();
        String pathAsString = path.toString();
        
        if (rootPath.equals(path)) {
            _view.setCurrentPathToRoot();
        } else {
            _view.setCurrentPath(pathAsString.replace(rootPathAsString, FilePickerFrame.ROOT_PATH));
        }
    }

    @Override
    public void setEntityData(@NotNull FileSystemEntityViewModel entity) {
        // No need to do anything
    }

    @Override
    public void setEntityData(@NotNull List<FileSystemEntityViewModel> entities) {
        // Alert the view
        _entities = entities;
        _view.setEntitiesData(new FileSystemEntitiesTableData(MAX_ENTITIES_ROWS, MAX_ENTITIES_COLUMNS, entities));
    }
}
