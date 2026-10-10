package pl.twojnick.sectors.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import pl.twojnick.sectors.core.messaging.MessageBroker;

public class VelocityHeartbeatHandler {
    private final ProxyServer proxy;
    private final MessageBroker messageBroker;

    public VelocityHeartbeatHandler(ProxyServer proxy, MessageBroker messageBroker) {
        this.proxy = proxy;
        this.messageBroker = messageBroker;

        messageBroker.subscribeTelemetry("sector.heartbeat", data -> {
            String payload = new String(data);
            String[] parts = payload.split(":");
            if (parts.length == 2) {
                String sectorId = parts[0];
                boolean isOnline = Boolean.parseBoolean(parts[1]);
                messageBroker.publishTelemetry("sector.status.update", (sectorId + ":" + isOnline).getBytes());
            }
        });
    }
}
