package pl.twojnick.sectors.core.config;

import java.util.List;

public class SectorConfig {
    public String dbHost = "mysql.titanaxe.com";
    public int dbPort = 3306;
    public String dbUser = "srv325281";
    public String dbPassword = "Vaw4wCB4";
    public String dbName = "srv325281";

    public String redisHost = "localhost";
    public int redisPort = 6379;
    public String redisPassword = "";

    public String natsUrl = "nats://localhost:4222";

    public List<SectorDefinition> sectors = List.of(
            new SectorDefinition("limbo", "purpur-limbo", SectorType.LIMBO, null),
            new SectorDefinition("spawn", "purpur-spawn", SectorType.SPAWN, null),
            new SectorDefinition("guild", "purpur-guild", SectorType.GUILD, null),
            new SectorDefinition("afk", "purpur-afk", SectorType.AFK, null),
            new SectorDefinition("event", "purpur-event", SectorType.EVENT, "sectors.access.event")
    );

    public record SectorDefinition(
            String id,
            String serverName,
            SectorType type,
            String requiredPermission
    ) {}

    public enum SectorType {
        LIMBO, SPAWN, GUILD, AFK, EVENT
    }
}