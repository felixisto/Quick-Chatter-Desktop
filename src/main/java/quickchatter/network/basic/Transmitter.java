package quickchatter.network.basic;

import org.jetbrains.annotations.NotNull;

import quickchatter.utilities.Callback;
import quickchatter.utilities.TimeValue;

import java.util.List;

public interface Transmitter {
    void start() throws Exception;
    void stop();

    @NotNull List<TransmissionLine.Input> getInputLines();
    @NotNull List<TransmissionLine.Output> getOutputLines();

    boolean isPingActive();
    @NotNull TimeValue getPingDelay();
    void setPingDelay(@NotNull TimeValue delay);
    void activatePing();
    void deactivatePing();

    interface Writer extends Transmitter {
        void sendMessage(@NotNull TransmissionMessage message) throws Exception;
    }

    interface Reader extends Transmitter {
        void readNewMessages(@NotNull TransmissionType type, @NotNull Callback<List<TransmissionMessage>> completion) throws Exception;
    }

    interface Service extends Transmitter {
        void readAllNewMessages();

        @NotNull PingStatusChecker getPingStatusChecker();

        void subscribe(@NotNull TransmitterListener listener);
        void unsubscribe(@NotNull TransmitterListener listener);
    }

    interface ReaderWriter extends Writer, Reader {

    }
}
