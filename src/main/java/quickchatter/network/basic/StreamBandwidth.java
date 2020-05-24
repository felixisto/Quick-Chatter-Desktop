package quickchatter.network.basic;

import org.jetbrains.annotations.NotNull;

import quickchatter.utilities.DataSize;
import quickchatter.utilities.TimeValue;

public interface StreamBandwidth {
    // Streams will flush in this specific data chunk size.
    @NotNull DataSize getFlushDataRate();

    // When a stream flushes a timer is started. If the timer expires before being
    // restarted by another flush data rate, it will force flush.
    @NotNull TimeValue getForceFlushTime();

    interface Boostable extends StreamBandwidth {
        void boostFlushRate(double multiplier);
        void revertBoost();
    }

    interface Tracker extends StreamBandwidth {
        interface Read extends Tracker {
            void read(int length);
        }

        interface Write extends Tracker {
            void write(int length);
        }

        interface Monitor extends Tracker {
            // Returns estimated rate that is transferred per second.
            @NotNull DataSize getEstimatedCurrentRate();
        }
    }
}
