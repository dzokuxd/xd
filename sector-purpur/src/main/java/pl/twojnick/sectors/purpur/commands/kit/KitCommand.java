package pl.twojnick.sectors.purpur.commands.kit;

import pl.twojnick.sectors.core.database.PlayerRepository;
import pl.twojnick.sectors.core.database.Rank;
import pl.twojnick.sectors.purpur.api.command.SectorCommand;
import pl.twojnick.sectors.purpur.api.command.SubCommand;

public class KitCommand extends SectorCommand {
    public KitCommand(PlayerRepository playerRepo) {
        super("kit", "Odbieranie kitów", playerRepo);

        addSub(SubCommand.of("vip", "Kit VIP", Rank.VIP, ctx -> {
            if (ctx.player() == null) return;
            ctx.send("§aOtrzymano kit §eVIP§a!");
        }));

        addSub(SubCommand.of("svip", "Kit SVIP", Rank.SVIP, ctx -> {
            if (ctx.player() == null) return;
            ctx.send("§aOtrzymano kit §eSVIP§a!");
        }));

        addSub(SubCommand.of("mvp", "Kit MVP", Rank.MVP, ctx -> {
            if (ctx.player() == null) return;
            ctx.send("§aOtrzymano kit §eMVP§a!");
        }));
    }
}
