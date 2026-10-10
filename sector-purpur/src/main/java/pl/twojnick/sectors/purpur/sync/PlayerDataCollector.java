package pl.twojnick.sectors.purpur.sync;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import java.util.ArrayList;
import java.util.List;

public class PlayerDataCollector {

    public static SyncedPlayerData collect(Player player, String sourceSector) {
        ItemStack[] inventory = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();
        ItemStack[] enderChest = player.getEnderChest().getContents();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        double health = player.getHealth();
        int foodLevel = player.getFoodLevel();
        float saturation = player.getSaturation();
        float exhaustion = player.getExhaustion();

        int level = player.getLevel();
        float exp = player.getExp();
        int totalExperience = player.getTotalExperience();

        List<PotionEffect> effects = new ArrayList<>(player.getActivePotionEffects());

        GameMode gameMode = player.getGameMode();
        boolean allowFlight = player.getAllowFlight();
        boolean isFlying = player.isFlying();

        return new SyncedPlayerData(
            player.getUniqueId(),
            inventory,
            armor,
            enderChest,
            offHand,
            health,
            foodLevel,
            saturation,
            exhaustion,
            level,
            exp,
            totalExperience,
            effects,
            gameMode,
            allowFlight,
            isFlying,
            System.currentTimeMillis(),
            sourceSector
        );
    }
}
