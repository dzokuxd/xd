package pl.twojnick.sectors.purpur.api.command;

import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import pl.twojnick.sectors.core.database.PlayerRepository;
import java.util.*;

public class SectorCommand implements CommandExecutor, TabCompleter {
    private final String name;
    private final String description;
    private final PlayerRepository playerRepo;
    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();

    public SectorCommand(String name, String description, PlayerRepository playerRepo) {
        this.name = name;
        this.description = description;
        this.playerRepo = playerRepo;
    }

    public SectorCommand addSub(SubCommand sub) {
        subCommands.put(sub.getName().toLowerCase(), sub);
        return this;
    }

    public void register(JavaPlugin plugin) {
        PluginCommand cmd = plugin.getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
            cmd.setDescription(description);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof org.bukkit.entity.Player player) {
            playerRepo.getPlayerData(player.getUniqueId()).thenAccept(data -> {
                CommandContext ctx = new CommandContext(sender, args, data);
                executeCommand(ctx);
            });
        } else {
            CommandContext ctx = new CommandContext(sender, args, null);
            executeCommand(ctx);
        }
        return true;
    }

    private void executeCommand(CommandContext ctx) {
        if (ctx.args().length == 0) {
            showHelp(ctx);
            return;
        }

        String subName = ctx.arg(0).toLowerCase();
        SubCommand sub = subCommands.get(subName);

        if (sub == null) {
            ctx.send("§cNieznana subkomenda: §e" + subName);
            showHelp(ctx);
            return;
        }

        String[] newArgs = Arrays.copyOfRange(ctx.args(), 1, ctx.args().length);
        CommandContext newCtx = new CommandContext(ctx.sender(), newArgs, ctx.playerData());
        sub.execute(newCtx);
    }

    private void showHelp(CommandContext ctx) {
        ctx.send("§6=== §e" + name + " §6===");
        for (SubCommand sub : subCommands.values()) {
            if (sub.getRequiredRank() != null && !ctx.hasRank(sub.getRequiredRank())) continue;
            if (sub.getPermission() != null && !ctx.sender().hasPermission(sub.getPermission())) continue;

            String rankInfo = sub.getRequiredRank() != null ?
                " §7[§e" + sub.getRequiredRank().name() + "§7]" : "";
            ctx.send("§7/" + name + " " + sub.getName() + " §8- " + sub.getDescription() + rankInfo);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return subCommands.values().stream()
                .map(SubCommand::getName)
                .filter(name -> name.startsWith(args[0].toLowerCase()))
                .toList();
        }
        return Collections.emptyList();
    }
}
