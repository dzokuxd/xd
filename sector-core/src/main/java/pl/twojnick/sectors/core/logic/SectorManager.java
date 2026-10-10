package pl.twojnick.sectors.core.logic;

import pl.twojnick.sectors.core.config.SectorConfig;
import pl.twojnick.sectors.core.config.SectorConfig.SectorDefinition;
import pl.twojnick.sectors.core.config.SectorConfig.SectorType;
import pl.twojnick.sectors.core.database.PlayerRepository;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SectorManager {
    private final Map<String, SectorDefinition> sectorsById;
    private final PlayerRepository playerRepo;

    public SectorManager(SectorConfig config, PlayerRepository playerRepo) {
        this.sectorsById = new HashMap<>();
        config.sectors.forEach(s -> sectorsById.put(s.id(), s));
        this.playerRepo = playerRepo;
    }

    public SectorDefinition getSector(String id) {
        return sectorsById.get(id);
    }

    public SectorDefinition getLimboSector() {
        return sectorsById.values().stream()
            .filter(s -> s.type() == SectorType.LIMBO)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No LIMBO sector defined!"));
    }

    public CompletableFuture<String> resolveTargetSector(UUID uuid) {
        return playerRepo.getPlayerData(uuid).thenApply(data -> {
            String last = data.lastSector();
            if (last == null || !sectorsById.containsKey(last)) {
                return "guild";
            }
            return last;
        });
    }
}
