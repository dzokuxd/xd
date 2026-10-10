package pl.twojnick.sectors.velocity.api.command;

import pl.twojnick.sectors.core.database.Rank;
import java.util.function.Consumer;

public class SubCommand {
    private final String name;
    private final String description;
    private final Rank requiredRank;
    private final String permission;
    private final Consumer<CommandContext> executor;

    public SubCommand(String name, String description, Rank requiredRank,
                     String permission, Consumer<CommandContext> executor) {
        this.name = name;
        this.description = description;
        this.requiredRank = requiredRank;
        this.permission = permission;
        this.executor = executor;
    }

    public static SubCommand of(String name, String description, Consumer<CommandContext> executor) {
        return new SubCommand(name, description, null, null, executor);
    }

    public static SubCommand of(String name, String description, Rank requiredRank,
                                Consumer<CommandContext> executor) {
        return new SubCommand(name, description, requiredRank, null, executor);
    }

    public static SubCommand of(String name, String description, Rank requiredRank,
                                String permission, Consumer<CommandContext> executor) {
        return new SubCommand(name, description, requiredRank, permission, executor);
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Rank getRequiredRank() { return requiredRank; }
    public String getPermission() { return permission; }

    public boolean execute(CommandContext ctx) {
        if (requiredRank != null && !ctx.hasRank(requiredRank)) {
            ctx.send("§cTa komenda wymaga rangi §e" + requiredRank.name() + "§c!");
            return false;
        }

        if (permission != null && !ctx.sender().hasPermission(permission)) {
            ctx.send("§cBrak uprawnień!");
            return false;
        }

        executor.accept(ctx);
        return true;
    }
}
