/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.presenter.worker;

import org.jetbrains.annotations.NotNull;
import quickchatter.filesystem.fundamentals.FilePath;
import quickchatter.utilities.Callback;
import quickchatter.utilities.Path;
import quickchatter.utilities.SimpleCallback;

public interface SendFilePerformerDelegate {
    void onAskedToReceiveFile(@NotNull Callback<Path> accept,
                              @NotNull SimpleCallback deny,
                              @NotNull String name,
                              @NotNull String description);

    void onOtherSideAcceptedTransferAsk();
    void onOtherSideDeniedTransferAsk();

    void onFileTransferComplete(@NotNull FilePath path);
    void onFileTransferCancelled();

    void fileTransferProgressUpdate(double progress);

    void fileSaveFailed(@NotNull Exception error);
}
