package pl.twojnick.sectors.core.database;

import java.util.UUID;

public record PlayerData(
    UUID uuid,
    String lastSector,
    Rank rank
) {}
