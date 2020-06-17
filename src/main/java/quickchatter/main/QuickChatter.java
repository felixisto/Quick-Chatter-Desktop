/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.main;

import org.jetbrains.annotations.NotNull;
import filesystem.simple.SimpleFileSystem;
import filesystem.worker.loader.FileSystemLoaderLocal;
import quickchatter.navigation.PrimaryRouter;
import quickchatter.presenter.FilePickerPresenter;
import quickchatter.ui.viewcontroller.FilePickerViewController;

public class QuickChatter {
    private static @NotNull PrimaryRouter _router;
    
    private static FilePickerPresenter presenter;
    private static FilePickerViewController vc;
    
    private static SimpleFileSystem simpleFS;
    private static FileSystemLoaderLocal loaderFS;
    
    public static void main(String[] arg) {
        // Create router and start first screen
        _router = new PrimaryRouter();
    }
}
