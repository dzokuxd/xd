package pl.twojnick.sectors.purpur.api.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.twojnick.sectors.core.database.PlayerData;
import pl.twojnick.sectors.core.database.Rank;

public record CommandContext(
    CommandSender sender,
    String[] args,
    PlayerData playerData
) {
    public Player player() {
        return sender instanceof Player p ? p : null;
    }

    public Rank rank() {
        return playerData != null ? playerData.rank() : Rank.OWNER;
    }

    public boolean hasRank(Rank required) {
        return rank().isAtLeast(required);
    }

    public String arg(int index) {
        return args.length > index ? args[index] : null;
    }

    public String arg(int index, String defaultValue) {
        return args.length > index ? args[index] : defaultValue;
    }

    public void send(String message) {
        sender.sendMessage(message);
    }
}
