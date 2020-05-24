package quickchatter.network.basic;

import org.jetbrains.annotations.NotNull;

public interface Scanner {
    boolean isRunning();

    void start() throws Exception;
    void stop() throws Exception;

    @NotNull ScannerConfiguration getConfiguration();
    void setConfiguration(@NotNull ScannerConfiguration configuration) throws Exception;
}
