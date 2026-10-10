package pl.twojnick.sectors.velocity.api.command;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import pl.twojnick.sectors.core.database.PlayerRepository;
import java.util.*;

public class SectorCommand implements SimpleCommand {
    private final String name;
    private final String description;
    private final PlayerRepository playerRepo;
    private final ProxyServer proxy;
    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();

    public SectorCommand(String name, String description, PlayerRepository playerRepo, ProxyServer proxy) {
        this.name = name;
        this.description = description;
        this.playerRepo = playerRepo;
        this.proxy = proxy;
    }

    public SectorCommand addSub(SubCommand sub) {
        subCommands.put(sub.getName().toLowerCase(), sub);
        return this;
    }

    public void register() {
        CommandMeta meta = proxy.getCommandManager().metaBuilder(name).build();
        proxy.getCommandManager().register(meta, this);
    }

    @Override
    public void execute(Invocation invocation) {
        var sender = invocation.source();
        String[] args = invocation.arguments();

        if (sender instanceof com.velocitypowered.api.proxy.Player player) {
            playerRepo.getPlayerData(player.getUniqueId()).thenAccept(data -> {
                CommandContext ctx = new CommandContext(sender, args, data);
                executeCommand(ctx);
            });
        } else {
            CommandContext ctx = new CommandContext(sender, args, null);
            executeCommand(ctx);
        }
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
            String rankInfo = sub.getRequiredRank() != null ?
                " §7[§e" + sub.getRequiredRank().name() + "§7]" : "";
            ctx.send("§7/" + name + " " + sub.getName() + " §8- " + sub.getDescription() + rankInfo);
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 1) {
            return subCommands.values().stream()
                .map(SubCommand::getName)
                .filter(name -> name.startsWith(args[0].toLowerCase()))
                .toList();
        }
        return Collections.emptyList();
    }
}
