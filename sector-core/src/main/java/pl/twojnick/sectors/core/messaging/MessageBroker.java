package pl.twojnick.sectors.core.messaging;

import java.util.function.Consumer;

public interface MessageBroker {
    void connect();
    void disconnect();

    void publishTelemetry(String subject, byte[] data);
    void subscribeTelemetry(String subject, Consumer<byte[]> handler);

    void publishStateTransfer(String channel, String payload);
    void subscribeStateTransfer(String channel, Consumer<String> handler);
}
