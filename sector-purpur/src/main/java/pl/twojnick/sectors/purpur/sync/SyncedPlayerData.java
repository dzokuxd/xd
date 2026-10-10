package pl.twojnick.sectors.purpur.sync;

import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import java.util.List;
import java.util.UUID;

public record SyncedPlayerData(
    UUID uuid,

    ItemStack[] inventory,
    ItemStack[] armorContents,
    ItemStack[] enderChest,
    ItemStack offHand,

    double health,
    int foodLevel,
    float saturation,
    float exhaustion,

    int level,
    float exp,
    int totalExperience,

    List<PotionEffect> activeEffects,

    GameMode gameMode,
    boolean allowFlight,
    boolean isFlying,

    long timestamp,
    String sourceSector
) {}
