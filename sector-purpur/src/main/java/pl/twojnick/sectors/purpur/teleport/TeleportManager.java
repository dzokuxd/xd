package pl.twojnick.sectors.purpur.teleport;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import pl.twojnick.sectors.core.status.SectorStatusTracker;
import pl.twojnick.sectors.core.messaging.MessageBroker;
import pl.twojnick.sectors.purpur.sync.SyncedPlayerData;
import pl.twojnick.sectors.purpur.PurpurPlugin;
import pl.twojnick.sectors.purpur.sync.PlayerDataCollector;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportManager implements Listener {
    private final PurpurPlugin plugin;
    private final MessageBroker messageBroker;
    private final SectorStatusTracker statusTracker;
    private final Map<UUID, String> pendingTeleports = new ConcurrentHashMap<>();

    public TeleportManager(PurpurPlugin plugin, MessageBroker messageBroker, SectorStatusTracker statusTracker) {
        this.plugin = plugin;
        this.messageBroker = messageBroker;
        this.statusTracker = statusTracker;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void requestTeleport(Player player, String targetSectorId) {
        if (!statusTracker.isSectorActive(targetSectorId)) {
            player.sendMessage(Component.text("§cSektor " + targetSectorId + " jest obecnie niedostępny lub wyłączony.").color(NamedTextColor.RED));
            return;
        }

        pendingTeleports.put(player.getUniqueId(), targetSectorId);
        player.sendActionBar(Component.text("§aTeleportacja za 15s... Nie ruszaj się!").color(NamedTextColor.GREEN));

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            executeTeleport(player, targetSectorId);
        }, 300L);
    }

    private void executeTeleport(Player player, String targetSectorId) {
        if (!pendingTeleports.containsKey(player.getUniqueId())) return;
        pendingTeleports.remove(player.getUniqueId());
        if (!player.isOnline()) return;

        if (!statusTracker.isSectorActive(targetSectorId)) {
            player.sendMessage(Component.text("§cTeleportacja anulowana: Sektor stał się niedostępny w trakcie odliczania!").color(NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f);
            return;
        }

        String currentSector = plugin.getCurrentSectorId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!currentSector.equals("event") && !targetSectorId.equals("event")) {
                SyncedPlayerData playerData = PlayerDataCollector.collect(player, currentSector);
                plugin.getPlayerDataSyncManager().savePlayerData(playerData).thenRun(() -> {
                    performTeleport(player, targetSectorId);
                });
            } else {
                performTeleport(player, targetSectorId);
            }
        });
    }

    private void performTeleport(Player player, String targetSectorId) {
        player.sendActionBar(Component.text("§aPrzenoszenie do sektora: §e" + targetSectorId).color(NamedTextColor.GREEN));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);

        String payload = player.getUniqueId() + ":" + targetSectorId;
        messageBroker.publishTelemetry("sector.transfer.request", payload.getBytes());
    }

    public void cancelTeleport(UUID uuid, String reason) {
        if (pendingTeleports.remove(uuid) != null) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                player.sendActionBar(Component.text("§c" + reason).color(NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (pendingTeleports.containsKey(uuid)) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                cancelTeleport(uuid, "Teleportacja anulowana: wykryto ruch!");
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancelTeleport(event.getPlayer().getUniqueId(), "Teleportacja anulowana: gracz opuścił serwer.");
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        cancelTeleport(event.getEntity().getUniqueId(), "Teleportacja anulowana: zginąłeś!");
    }
}
