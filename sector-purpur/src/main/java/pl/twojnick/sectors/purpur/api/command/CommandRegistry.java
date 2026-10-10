package pl.twojnick.sectors.purpur.api.command;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;

public class CommandRegistry {
    private final List<SectorCommand> commands = new ArrayList<>();

    public CommandRegistry add(SectorCommand command) {
        commands.add(command);
        return this;
    }

    public void registerAll(JavaPlugin plugin) {
        commands.forEach(cmd -> cmd.register(plugin));
    }
}
