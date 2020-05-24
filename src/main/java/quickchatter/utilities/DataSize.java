/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.utilities;

import org.jetbrains.annotations.NotNull;
import java.util.Locale;

public class DataSize {
    public static @NotNull DataSize zero() {
        return new DataSize(0);
    }

    public static @NotNull DataSize copy(@NotNull DataSize other) {
        return new DataSize(other);
    }

    public static @NotNull DataSize buildBytes(int bytes) {
        return new DataSize((int) bytes);
    }
    public static @NotNull DataSize buildBytes(long bytes) {
        return new DataSize(bytes);
    }

    public static @NotNull DataSize buildKB(double kb) {
        return new DataSize(kb * 1024);
    }

    public static @NotNull DataSize buildMB(double mb) {
        return new DataSize(mb * 1024 * 1024);
    }

    public static @NotNull DataSize buildGB(double gb) {
        return new DataSize(gb * 1024 * 1024 * 1024);
    }

    private final long _value;

    private DataSize(double bytes) {
        this._value = bytes >= 0 ? ((int)bytes) : 0;
    }
    private DataSize(@NotNull DataSize other) {
        this._value = other._value;
    }

    public long inBytes() {
        return _value;
    }

    public double inKB() {
        return inBytes() / 1024.0;
    }

    public double inMB() {
        return inKB() / 1024.0;
    }

    public double inGB() {
        return inMB() / 1024.0;
    }

    @Override
    public @NotNull String toString() {
        double gb = inGB();

        if (gb >= 1) {
            return String.format(Locale.getDefault(), "%.1f GB", gb);
        }

        double mb = inMB();

        if (mb >= 1) {
            return String.format(Locale.getDefault(), "%.1f MB", mb);
        }

        double kb = inKB();

        if (kb >= 1) {
            return String.format(Locale.getDefault(), "%.1f KB", kb);
        }

        long b = inBytes();

        return String.format(Locale.getDefault(), "%d bytes", b);
    }
}

