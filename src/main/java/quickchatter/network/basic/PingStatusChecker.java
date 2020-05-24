package quickchatter.network.basic;

import org.jetbrains.annotations.NotNull;

import quickchatter.utilities.TimeValue;

public interface PingStatusChecker {
    @NotNull TimeValue timeElapsedSinceLastPing();
    boolean isConnectionTimeout();
}
