package pl.twojnick.sectors.purpur.sync;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import pl.twojnick.sectors.purpur.PurpurPlugin;
import java.util.ArrayList;
import java.util.List;

public class PlayerDataRestoreListener implements Listener {
    private final PurpurPlugin plugin;
    private final PlayerDataSyncManager syncManager;

    public PlayerDataRestoreListener(PurpurPlugin plugin) {
        this.plugin = plugin;
        this.syncManager = plugin.getPlayerDataSyncManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        syncManager.loadPlayerData(player.getUniqueId()).thenAccept(data -> {
            if (data == null) return;

            Bukkit.getScheduler().runTask(plugin, () -> {
                restorePlayerData(player, data);
            });
        });
    }

    private void restorePlayerData(Player player, SyncedPlayerData data) {
        player.getInventory().setContents(data.inventory());
        player.getInventory().setArmorContents(data.armorContents());
        player.getEnderChest().setContents(data.enderChest());
        player.getInventory().setItemInOffHand(data.offHand());

        player.setHealth(data.health());
        player.setFoodLevel(data.foodLevel());
        player.setSaturation(data.saturation());
        player.setExhaustion(data.exhaustion());

        player.setLevel(data.level());
        player.setExp(data.exp());
        player.setTotalExperience(data.totalExperience());

        List<PotionEffect> currentEffects = new ArrayList<>(player.getActivePotionEffects());
        currentEffects.forEach(effect -> player.removePotionEffect(effect.getType()));
        data.activeEffects().forEach(effect -> player.addPotionEffect(effect));

        player.setGameMode(data.gameMode());
        player.setAllowFlight(data.allowFlight());
        player.setFlying(data.isFlying());

        player.updateInventory();
    }
}
