package pl.twojnick.sectors.purpur.commands.guild;

import pl.twojnick.sectors.core.database.PlayerRepository;
import pl.twojnick.sectors.core.database.Rank;
import pl.twojnick.sectors.core.messaging.MessageBroker;
import pl.twojnick.sectors.purpur.api.command.SectorCommand;
import pl.twojnick.sectors.purpur.api.command.SubCommand;

public class GuildCommand extends SectorCommand {
    public GuildCommand(PlayerRepository playerRepo, MessageBroker messageBroker) {
        super("g", "Komendy gildii", playerRepo);

        addSub(SubCommand.of("zaloz", "Załóż gildię", Rank.VIP, ctx -> {
            if (ctx.player() == null) return;
            ctx.send("§aTworzenie gildii...");
        }));

        addSub(SubCommand.of("opusc", "Opuść gildię", null, ctx -> {
            if (ctx.player() == null) return;
            ctx.send("§aOpuszczasz gildię...");
        }));

        addSub(SubCommand.of("wyrzuc", "Wyrzuć członka", null, ctx -> {
            String target = ctx.arg(0);
            if (target == null) {
                ctx.send("§cUżycie: /g wyrzuc <gracz>");
                return;
            }
            ctx.send("§aWyrzucanie gracza §e" + target + " §az gildii...");
        }));

        addSub(SubCommand.of("zapros", "Zaproś gracza", null, ctx -> {
            String target = ctx.arg(0);
            if (target == null) {
                ctx.send("§cUżycie: /g zapros <gracz>");
                return;
            }
            ctx.send("§aZaproszenie wysłane do §e" + target);
        }));

        addSub(SubCommand.of("info", "Informacje o gildii", null, ctx -> {
            ctx.send("§6=== Informacje o gildii ===");
            ctx.send("§7Nazwa: §eTestGuild");
            ctx.send("§7Członkowie: §e5/20");
        }));
    }
}
