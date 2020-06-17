/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.presenter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import filesystem.fundamentals.DirectoryPath;
import filesystem.fundamentals.FilePath;
import filesystem.model.DirectoryContents;
import filesystem.model.EntityInfo;
import filesystem.repository.RepoDirectory;
import filesystem.repository.RepoDirectoryCache;
import filesystem.repository.Repository;
import filesystem.worker.loader.FileSystemLoader;
import quickchatter.mvp.MVP;
import quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import utilities.Logger;
import utilities.LooperService;
import utilities.Parser;
import utilities.Path;
import utilities.SafeMutableArray;
import utilities.SimpleCallback;

/// A massive presenter that implements various file picking functionality.
public class FilePickerPresenter implements BasePresenter.FilePicker, BasePresenter.FileSystemNavigation, BasePresenter.FileSystemDirectory {
    private @Nullable BasePresenterDelegate.FileSystemDirectory _dirDelegate;
    private @Nullable BasePresenterDelegate.FileSystemNavigation _navDelegate;
    private @Nullable BasePresenterDelegate.FilePicker _pickerDelegate;

    private final EntityInfo.Directory _rootInfo;
    private final @NotNull Repository.MutableDirectory _rootRepo;
    private final @NotNull FileSystemLoader _loader;
    private final @NotNull Parser<EntityInfo, FileSystemEntityViewModel> _parser;

    private final @NotNull RepoDirectoryCache _cache = new RepoDirectoryCache();

    private final @NotNull AtomicReference<DirectoryPath> _currentPath = new AtomicReference<>();

    private final @NotNull RepoDirectory _currentDirectory;
    private final @NotNull SafeMutableArray<FileSystemEntityViewModel> _directoryContentsViewModels = new SafeMutableArray<>();

    public FilePickerPresenter(@NotNull FileSystemLoader loader,
                               @NotNull EntityInfo.Directory rootInfo,
                               @NotNull Parser<EntityInfo, FileSystemEntityViewModel> parser) {
        _loader = loader;
        _parser = parser;

        _rootInfo = rootInfo;
        _rootRepo = new RepoDirectory(_rootInfo);

        _currentPath.set(_rootInfo.getDirPath());

        _currentDirectory = new RepoDirectory(_rootRepo);
    }

    // # Presenter.FilePicker

    @Override
    public @NotNull MVP.View getView() {
        return _dirDelegate;
    }
    
    @Override
    public void start(@NotNull BasePresenterDelegate.FilePicker filePickerDelegate,
                      @NotNull BasePresenterDelegate.FileSystemNavigation navigationDelegate,
                      @NotNull BasePresenterDelegate.FileSystemDirectory directoryDelegate) throws Exception {
        _pickerDelegate = filePickerDelegate;
        start(navigationDelegate);
        start(directoryDelegate);
    }

    @Override
    public @NotNull DirectoryPath getDirectoryPath() {
        return _currentPath.get();
    }

    @Override
    public void onNavigateBack() {
        // Already at root
        if (getDirectoryPath().equals(_rootInfo.getDirPath())) {
            return;
        }

        Logger.message(this, "Navigate back");

        final URI parentPath = getDirectoryPath().pathWithoutLastComponent().getURL();
        final DirectoryPath previousDirPath = new DirectoryPath(parentPath);

        LooperService.getShared().asyncInBackground(new SimpleCallback() {
            @Override
            public void perform() {
                try {
                    navigateTo(_loader.readDirectoryInfo(previousDirPath));
                } catch (Exception e) {

                }
            }
        });
    }

    // # Presenter.FileSystemDirectory

    @Override
    public void start(@NotNull BasePresenterDelegate.FileSystemDirectory delegate) throws Exception {
        _dirDelegate = delegate;

        updateDirectoryContents();
    }
    
    @Override
    public @NotNull FileSystemEntityViewModel getCurrentDirectory() {
        try {
            return _parser.parse(_currentDirectory.getInfo());
        } catch (Exception e) {
            // Not sure how to handle this
            return null;
        }
    }

    @Override
    public @Nullable FileSystemEntityViewModel getEntityInfoAt(int index) {
        List<EntityInfo> entities = _currentDirectory.getContents().entitiesCopy();

        if (index < 0 || index >= entities.size()) {
            Logger.warning(this, "Cannot navigate to item out of bounds index " + index);
            return null;
        }

        try {
            return _parser.parse(entities.get(index));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void navigateToEntityAt(int index) {
        List<EntityInfo> entities = _currentDirectory.getContents().entitiesCopy();

        if (index < 0 || index >= entities.size()) {
            Logger.warning(this, "Cannot navigate to out of bounds index " + index);
            return;
        }
        
        Logger.message(this, "Try to navigate to entity at index " + index);

        navigateTo(entities.get(index));
    }

    @Override
    public void scrollBy(double scrollValue) {

    }

    @Override
    public void closeWithoutPick() {
        Logger.message(this, "Close without picking");

        LooperService.getShared().performOnAWT(new SimpleCallback() {
            @Override
            public void perform() {
                if (_pickerDelegate == null) {
                    return;
                }

                try {
                    _pickerDelegate.onCloseWithoutPicking();
                } catch (Exception e) {

                }
            }
        });
    }

    @Override
    public void pickFile(@NotNull FileSystemEntityViewModel entity) {
        EntityInfo info = entityInfoForViewModel(entity);

        if (info == null || !(info instanceof EntityInfo.File)) {
            Logger.error(this, "Failed to pick file, given model is invalid or corrupted");
            return;
        }

        final FilePath path = ((EntityInfo.File)info).getFilePath();

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

    @Override
    public void pickDirectory(@NotNull FileSystemEntityViewModel entity) {
        EntityInfo info = entityInfoForViewModel(entity);

        if (info == null || !(info instanceof EntityInfo.Directory)) {
            Logger.error(this, "Failed to pick directory, given model is invalid or corrupted");
            return;
        }

        final DirectoryPath path = ((EntityInfo.Directory)info).getDirPath();

        Logger.message(this, "Pick directory " + path.toString());

        LooperService.getShared().performOnAWT(new SimpleCallback() {
            @Override
            public void perform() {
                if (_pickerDelegate == null) {
                    return;
                }

                try {
                    _pickerDelegate.onPickDirectory(path);
                } catch (Exception e) {

                }
            }
        });
    }
    
    @Override
    public @NotNull FileSystemNavigation getNavigationSubpresenter() {
        return this;
    }

    @Override
    public @NotNull FileSystemDirectory getSystemDirectorySubpresenter() {
        return this;
    }

    // # Presenter.FileSystemNavigation

    @Override
    public @NotNull DirectoryPath getRootDirectoryPath() {
        return _rootInfo.getDirPath();
    }

    @Override
    public void start(@NotNull BasePresenterDelegate.FileSystemNavigation delegate) throws Exception {
        _navDelegate = delegate;

        updateNavigationInfo();
    }
    
    // # Helpers

    // Get the corresponding entitiy info for the given entity view model.
    public @Nullable EntityInfo entityInfoForViewModel(@NotNull FileSystemEntityViewModel vm) {
        String currentDirName = _currentDirectory.getInfo().getPath().getLastComponent();
        
        if (currentDirName.equals(vm.name)) {
            return _currentDirectory.getInfo();
        }
        
        @Nullable EntityInfo matchingInfo = null;

        for (EntityInfo info : _currentDirectory.getContents().entitiesCopy()) {
            String infoName = info.getPath().getLastComponent();
            
            if (infoName.equals(vm.name)) {
                matchingInfo = info;
                break;
            }
        }

        return matchingInfo;
    }

    // Get the path for the given entity view model.
    public @Nullable Path pathOfEntityInfoViewModel(@NotNull FileSystemEntityViewModel vm) {
        EntityInfo info = entityInfoForViewModel(vm);

        if (info != null) {
            return info.getPath();
        }

        return null;
    }

    // # Navigation

    private void navigateTo(@NotNull EntityInfo info) {
        if (info instanceof EntityInfo.Directory) {
            openDirectory(((EntityInfo.Directory) info).getDirPath());
        }
    }

    private void openDirectory(@NotNull DirectoryPath path) {
        Logger.message(this, "Navigate to directory " + path.toString());
        
        _currentPath.set(path);

        updateDirectoryContents();
    }

    // # Update data

    private void updateDirectoryContents() {
        final FilePickerPresenter self = this;

        LooperService.getShared().asyncInBackground(new SimpleCallback() {
            @Override
            public void perform() {
                Repository.MutableDirectory repo = getCurrentDirectoryContents();

                if (repo != null) {
                    updateDirectoryContents(repo);
                    updateDelegateDirectoryContents(repo);
                } else {
                    Logger.warning(self, "Failed to retrieve current directory contents");
                    clearDelegateDirectoryContents();
                }
            }
        });
    }

    private void updateNavigationInfo() {
        LooperService.getShared().performOnAWT(new SimpleCallback() {
            @Override
            public void perform() {
                if (_navDelegate != null) {
                    _navDelegate.setCurrentPath(getDirectoryPath());
                }
            }
        });
    }

    private @Nullable Repository.MutableDirectory getCurrentDirectoryContents() {
        Repository.MutableDirectory cached = _cache.cachedRepository(_cache.getRelation().buildCacheKeyFromProperty(getDirectoryPath()));

        if (cached != null) {
            return cached;
        }

        try {
            cached = new RepoDirectory(_loader.readDirectoryInfo(getDirectoryPath()));
            _cache.cacheRepository(cached);
        } catch (Exception e) {
        }

        return cached;
    }

    private void updateDirectoryContents(@NotNull Repository.MutableDirectory repo) {
        _currentDirectory.assign(repo);
        _cache.cacheRepository(repo);
    }

    private void clearDelegateDirectoryContents() {
        LooperService.getShared().performOnAWT(new SimpleCallback() {
            @Override
            public void perform() {
                _directoryContentsViewModels.removeAll();

                if (_dirDelegate == null) {
                    return;
                }

                _dirDelegate.setEntityData(new ArrayList<FileSystemEntityViewModel>());
            }
        });
    }

    private void updateDelegateDirectoryContents(@NotNull Repository.MutableDirectory repo) {
        final ArrayList<FileSystemEntityViewModel> data = new ArrayList<>();

        try {
            data.addAll(parse(repo.getContents()));
        } catch (Exception e) {

        }

        LooperService.getShared().performOnAWT(new SimpleCallback() {
            @Override
            public void perform() {
                _directoryContentsViewModels.removeAll();
                _directoryContentsViewModels.addAll(data);

                if (_dirDelegate == null) {
                    return;
                }

                _dirDelegate.setEntityData(data);

                updateNavigationInfo();
            }
        });
    }

    private @NotNull List<FileSystemEntityViewModel> parse(@NotNull DirectoryContents contents) {
        ArrayList<FileSystemEntityViewModel> entities = new ArrayList<>();

        for (EntityInfo entity : contents.entitiesCopy()) {
            try {
                entities.add(_parser.parse(entity));
            } catch (Exception e) {
            }
        }

        return entities;
    }
}
