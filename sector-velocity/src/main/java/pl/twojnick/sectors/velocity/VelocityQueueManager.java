package pl.twojnick.sectors.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.lettuce.core.api.sync.RedisCommands;
import pl.twojnick.sectors.core.database.PlayerRepository;
import pl.twojnick.sectors.core.logic.SectorManager;
import pl.twojnick.sectors.core.messaging.MessageBroker;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VelocityQueueManager {
    private final ProxyServer proxy;
    private final MessageBroker messageBroker;
    private final PlayerRepository playerRepo;
    private final SectorManager sectorManager;
    private final VelocityPlugin plugin;
    private static final String QUEUE_KEY = "sectors:login:queue";
    private static final String FORCE_SPAWN_QUEUE_KEY = "sectors:force_spawn:queue";
    private static final int BATCH_SIZE = 4;
    private final RedisCommands<String, String> redis;

    public VelocityQueueManager(ProxyServer proxy, MessageBroker messageBroker,
                               PlayerRepository playerRepo, SectorManager sectorManager,
                               VelocityPlugin plugin) {
        this.proxy = proxy;
        this.messageBroker = messageBroker;
        this.playerRepo = playerRepo;
        this.sectorManager = sectorManager;
        this.plugin = plugin;
        this.redis = ((VelocityRedisNatsBrokerImpl) messageBroker).getRedisCommands();

        proxy.getScheduler().buildTask(plugin, this::processQueue)
             .delay(3, TimeUnit.SECONDS)
             .repeat(3, TimeUnit.SECONDS)
             .schedule();
    }

    public void addToQueue(Player player, boolean forceSpawn) {
        if (forceSpawn) {
            redis.rpush(FORCE_SPAWN_QUEUE_KEY, player.getUniqueId().toString());
        } else {
            redis.rpush(QUEUE_KEY, player.getUniqueId().toString());
        }
        player.sendMessage(net.kyori.adventure.text.Component.text(
            "§7Zostałeś dodany do kolejki. Pozycja: §e" + getPosition(player.getUniqueId(), forceSpawn)
        ));
    }

    public void addToQueue(Player player) {
        addToQueue(player, false);
    }

    private void processQueue() {
        for (int i = 0; i < BATCH_SIZE; i++) {
            if (!processOneForceSpawn()) break;
        }

        for (int i = 0; i < BATCH_SIZE; i++) {
            if (!processOneNormal()) break;
        }
    }

    private boolean processOneForceSpawn() {
        String uuidStr = redis.lpop(FORCE_SPAWN_QUEUE_KEY);
        if (uuidStr == null) return false;

        UUID uuid = UUID.fromString(uuidStr);
        proxy.getPlayer(uuid).ifPresentOrElse(player -> {
            String targetId = "spawn";
            var targetSector = sectorManager.getSector(targetId);
            var targetServer = proxy.getServer(targetSector.serverName());

            if (targetServer.isPresent()) {
                playerRepo.updateLastSector(uuid, targetId);

                player.createConnectionRequest(targetServer.get()).connect().thenAccept(result -> {
                    if (result.isSuccessful()) {
                        player.sendMessage(net.kyori.adventure.text.Component.text(
                            "§aDołączasz do sektora: §e" + targetId
                        ));
                    } else {
                        player.sendMessage(net.kyori.adventure.text.Component.text(
                            "§cBłąd połączenia z sektorem."
                        ));
                        redis.rpush(FORCE_SPAWN_QUEUE_KEY, uuidStr);
                    }
                });
            } else {
                redis.rpush(FORCE_SPAWN_QUEUE_KEY, uuidStr);
            }
        }, () -> {});

        return true;
    }

    private boolean processOneNormal() {
        String uuidStr = redis.lpop(QUEUE_KEY);
        if (uuidStr == null) return false;

        UUID uuid = UUID.fromString(uuidStr);
        proxy.getPlayer(uuid).ifPresentOrElse(player -> {
            playerRepo.getPlayerData(uuid).thenAccept(data -> {
                String targetId = (data.lastSector() == null) ? "guild" : data.lastSector();
                var targetSector = sectorManager.getSector(targetId);
                var targetServer = proxy.getServer(targetSector.serverName());

                if (targetServer.isPresent()) {
                    playerRepo.updateLastSector(uuid, targetId);

                    player.createConnectionRequest(targetServer.get()).connect().thenAccept(result -> {
                        if (result.isSuccessful()) {
                            player.sendMessage(net.kyori.adventure.text.Component.text(
                                "§aDołączasz do sektora: §e" + targetId
                            ));
                        } else {
                            player.sendMessage(net.kyori.adventure.text.Component.text(
                                "§cBłąd połączenia z sektorem."
                            ));
                            redis.rpush(QUEUE_KEY, uuidStr);
                        }
                    });
                } else {
                    redis.rpush(QUEUE_KEY, uuidStr);
                }
            });
        }, () -> {});

        return true;
    }

    private int getPosition(UUID uuid, boolean forceSpawn) {
        String key = forceSpawn ? FORCE_SPAWN_QUEUE_KEY : QUEUE_KEY;
        Long index = redis.lpos(key, uuid.toString());
        return index != null ? index.intValue() + 1 : -1;
    }
}
