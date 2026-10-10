package pl.twojnick.sectors.purpur.listeners;

import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import pl.twojnick.sectors.purpur.PurpurPlugin;

public class HeartbeatListener implements Listener {
    private final PurpurPlugin plugin;

    public HeartbeatListener(PurpurPlugin plugin) {
        this.plugin = plugin;

        new BukkitRunnable() {
            @Override
            public void run() {
                String sectorId = plugin.getCurrentSectorId();
                String payload = sectorId + ":true";
                plugin.getMessageBroker().publishTelemetry("sector.heartbeat", payload.getBytes());
            }
        }.runTaskTimer(plugin, 100L, 100L);

        plugin.getMessageBroker().subscribeTelemetry("sector.status.update", data -> {
            String payload = new String(data);
            String[] parts = payload.split(":");
            if (parts.length == 2) {
                String sectorId = parts[0];
                boolean isOnline = Boolean.parseBoolean(parts[1]);
                plugin.getStatusTracker().updateStatus(sectorId, isOnline);
            }
        });
    }
}
