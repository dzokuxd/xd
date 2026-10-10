package pl.twojnick.sectors.purpur.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import pl.twojnick.sectors.purpur.PurpurPlugin;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AfkManager implements Listener {
    private final PurpurPlugin plugin;
    private final Map<UUID, Long> lastActivity = new ConcurrentHashMap<>();

    public AfkManager(PurpurPlugin plugin) {
        this.plugin = plugin;

        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    Long last = lastActivity.get(player.getUniqueId());
                    if (last != null && (now - last) > 60_000) {
                        if (plugin.getCurrentSectorId().equals("spawn")) {
                            plugin.getMessageBroker().publishTelemetry(
                                "sector.transfer.request",
                                (player.getUniqueId() + ":afk").getBytes()
                            );
                            lastActivity.remove(player.getUniqueId());
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            lastActivity.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastActivity.remove(event.getPlayer().getUniqueId());
    }
}
