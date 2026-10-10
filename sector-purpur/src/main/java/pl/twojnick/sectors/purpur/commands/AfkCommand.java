package pl.twojnick.sectors.purpur.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.twojnick.sectors.purpur.PurpurPlugin;

public class AfkCommand implements CommandExecutor {
    private final PurpurPlugin plugin;

    public AfkCommand(PurpurPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cTylko gracze mogą użyć tej komendy!");
            return true;
        }
        if (!plugin.getCurrentSectorId().equals("spawn")) {
            sender.sendMessage("§cMożesz użyć /afk tylko na spawnie!");
            return true;
        }
        plugin.getTeleportManager().requestTeleport(player, "afk");
        return true;
    }
}
