/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.utilities;

import java.util.Date;
import org.jetbrains.annotations.NotNull;

public class Timer {
    public final @NotNull TimeValue delay;

    private @NotNull Date _date;

    public Timer(@NotNull TimeValue delay) {
        this(delay, new Date());
    }

    public Timer(@NotNull TimeValue delay, @NotNull Date now) {
        this.delay = delay;
        this._date = now;
    }

    // # Validators

    public @NotNull TimeValue timeElapsedSince(@NotNull Date now) {
        long differenceMS = now.getTime() - _date.getTime();
        return TimeValue.buildMS((int)differenceMS);
    }

    public @NotNull TimeValue timeElapsedSinceNow() {
        return timeElapsedSince(new Date());
    }

    public boolean isExpired() {
        return isExpiredSince(new Date());
    }

    public boolean isExpiredSince(@NotNull Date now) {
        return timeElapsedSince(now).inMS() > delay.inMS();
    }

    // # Operations

    public void reset() {
        _date = new Date();
    }

    public boolean update() {
        boolean expired = isExpired();

        if (expired) {
            reset();
        }

        return expired;
    }
}

