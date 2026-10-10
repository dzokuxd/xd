package pl.twojnick.sectors.purpur.sync;

import io.lettuce.core.api.sync.RedisCommands;
import pl.twojnick.sectors.purpur.PurpurPlugin;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerDataSyncManager {
    private static final String REDIS_KEY_PREFIX = "player:sync:";
    private static final int TTL_SECONDS = 300;

    private final PurpurPlugin plugin;
    private final RedisCommands<String, String> redis;

    public PlayerDataSyncManager(PurpurPlugin plugin) {
        this.plugin = plugin;
        this.redis = ((pl.twojnick.sectors.purpur.RedisNatsBrokerImpl) plugin.getMessageBroker()).getRedisCommands();
    }

    public CompletableFuture<Void> savePlayerData(SyncedPlayerData data) {
        return CompletableFuture.runAsync(() -> {
            String key = REDIS_KEY_PREFIX + data.uuid();
            String json = PlayerDataSerializer.serialize(data);
            redis.setex(key, TTL_SECONDS, json);
        });
    }

    public CompletableFuture<SyncedPlayerData> loadPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String key = REDIS_KEY_PREFIX + uuid;
            String json = redis.getdel(key);

            if (json == null) {
                return null;
            }

            return PlayerDataSerializer.deserialize(json);
        });
    }
}
