package pl.twojnick.sectors.purpur.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.twojnick.sectors.core.database.PlayerRepository;
import pl.twojnick.sectors.core.database.Rank;
import pl.twojnick.sectors.purpur.api.command.SectorCommand;
import pl.twojnick.sectors.purpur.api.command.SubCommand;

public class RankCommand extends SectorCommand {
    private final PlayerRepository playerRepo;

    public RankCommand(PlayerRepository playerRepo) {
        super("rank", "Zarządzanie rangami", playerRepo);
        this.playerRepo = playerRepo;

        addSub(SubCommand.of("set", "Ustaw rangę", Rank.ADMIN, "sectors.admin.rank", ctx -> {
            String targetName = ctx.arg(0);
            String rankName = ctx.arg(1);

            if (targetName == null || rankName == null) {
                ctx.send("§cUżycie: /rank set <gracz> <ranga>");
                return;
            }

            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                ctx.send("§cGracz nie jest online!");
                return;
            }

            try {
                Rank rank = Rank.valueOf(rankName.toUpperCase());
                playerRepo.setRank(target.getUniqueId(), rank).thenRun(() -> {
                    ctx.send("§aUstawiono rangę §e" + rank.name() + " §adla §e" + target.getName());
                    target.sendMessage("§aTwoja ranga: §e" + rank.name());
                });
            } catch (IllegalArgumentException e) {
                ctx.send("§cNieznana ranga. Dostępne: §ePLAYER, VIP, SVIP, MVP, HELPER, MOD, ADMIN, OWNER");
            }
        }));
    }
}
