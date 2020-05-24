package quickchatter.network.basic;

import org.jetbrains.annotations.NotNull;

public interface TransmissionMessagePart {
    @NotNull TransmissionType getType();
    int partIndex();
    int partsCount();

    interface Ping extends TransmissionMessagePart {

    }

    interface Start extends TransmissionMessagePart {
        int expectedLength();
    }

    interface Data extends TransmissionMessagePart {
        @NotNull byte[] getData();
    }

    interface End extends TransmissionMessagePart {

    }
}
