package network.basic;

import org.jetbrains.annotations.NotNull;

import utilities.TimeValue;

public interface PingStatusChecker {
    @NotNull TimeValue timeElapsedSinceLastPing();
    boolean isConnectionTimeout();
}
