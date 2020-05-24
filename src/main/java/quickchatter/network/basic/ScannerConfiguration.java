package quickchatter.network.basic;

import org.jetbrains.annotations.NotNull;

import quickchatter.utilities.Copyable;
import quickchatter.utilities.TimeValue;

public class ScannerConfiguration implements Copyable<ScannerConfiguration> {
    public boolean retryForever = false;
    public int retryCount = 3;
    public @NotNull TimeValue retryDelay = TimeValue.buildSeconds(5);

    @Override
    public ScannerConfiguration copy() {
        ScannerConfiguration conf = new ScannerConfiguration();

        conf.retryForever = retryForever;
        conf.retryCount = retryCount;
        conf.retryDelay = TimeValue.copy(retryDelay);

        return conf;
    }
}
