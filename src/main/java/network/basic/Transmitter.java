package network.basic;

import org.jetbrains.annotations.NotNull;

import utilities.Callback;
import utilities.TimeValue;

import java.util.List;

public interface Transmitter {
    void start() throws Exception;
    void stop();

    interface Pinger {
        boolean isPingActive();
        @NotNull TimeValue getPingDelay();
        void setPingDelay(@NotNull TimeValue delay);
        void activatePing();
        void deactivatePing();
    }
    
    interface Reader extends Transmitter {
        @NotNull List<TransmissionLine.Input> getInputLines();
        
        void readNewMessages(@NotNull TransmissionType type, @NotNull Callback<List<TransmissionMessage>> completion) throws Exception;
    }

    interface Writer extends Transmitter {
        @NotNull List<TransmissionLine.Output> getOutputLines();
        
        void sendMessage(@NotNull TransmissionMessage message) throws Exception;
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
