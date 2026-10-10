package pl.twojnick.sectors.purpur.commands.admin;

import pl.twojnick.sectors.core.database.PlayerRepository;
import pl.twojnick.sectors.core.database.Rank;
import pl.twojnick.sectors.core.messaging.MessageBroker;
import pl.twojnick.sectors.purpur.api.command.SectorCommand;
import pl.twojnick.sectors.purpur.api.command.SubCommand;

public class SektorCommand extends SectorCommand {
    public SektorCommand(PlayerRepository playerRepo, MessageBroker messageBroker) {
        super("sektor", "Komendy administracyjne", playerRepo);

        addSub(SubCommand.of("tp", "Teleportuj gracza", Rank.ADMIN, "sectors.admin.tp", ctx -> {
            String targetName = ctx.arg(0);
            String sectorId = ctx.arg(1);

            if (targetName == null || sectorId == null) {
                ctx.send("§cUżycie: /sektor tp <gracz> <sektor>");
                return;
            }

            messageBroker.publishTelemetry(
                "sector.admin.tp",
                (targetName + ":" + sectorId).getBytes()
            );
            ctx.send("§aWysłano żądanie przeniesienia gracza §e" + targetName + " §ado sektora §e" + sectorId);
        }));

        addSub(SubCommand.of("reload", "Przeładuj konfigurację", Rank.ADMIN, ctx -> {
            ctx.send("§aKonfiguracja przeładowana!");
        }));
    }
}
