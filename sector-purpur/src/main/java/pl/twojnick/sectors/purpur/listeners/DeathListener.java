package pl.twojnick.sectors.purpur.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import pl.twojnick.sectors.core.config.SectorConfig.SectorType;
import pl.twojnick.sectors.purpur.PurpurPlugin;

public class DeathListener implements Listener {
    private final PurpurPlugin plugin;

    public DeathListener(PurpurPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        String currentSectorId = plugin.getCurrentSectorId();

        var sector = plugin.getSectorManager().getSector(currentSectorId);
        if (sector != null && (sector.type() == SectorType.GUILD || sector.type() == SectorType.EVENT)) {
            event.setCancelled(true);
            player.setHealth(0);

            String payload = player.getUniqueId() + ":limbo:force_spawn";
            plugin.getMessageBroker().publishTelemetry("sector.transfer.request", payload.getBytes());

            player.sendMessage("§cZginąłeś! Zostaniesz przeniesiony na spawn.");
        }
    }
}
