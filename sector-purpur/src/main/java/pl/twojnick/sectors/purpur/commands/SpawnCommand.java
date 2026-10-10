package pl.twojnick.sectors.purpur.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.twojnick.sectors.purpur.PurpurPlugin;

public class SpawnCommand implements CommandExecutor {
    private final PurpurPlugin plugin;

    public SpawnCommand(PurpurPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cTylko gracze mogą użyć tej komendy!");
            return true;
        }
        plugin.getTeleportManager().requestTeleport(player, "spawn");
        return true;
    }
}
