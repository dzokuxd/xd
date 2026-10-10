package pl.twojnick.sectors.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import pl.twojnick.sectors.core.database.PlayerRepository;
import pl.twojnick.sectors.core.logic.SectorManager;
import pl.twojnick.sectors.core.messaging.MessageBroker;
import java.util.UUID;

public class VelocityTransferHandler {
    private final ProxyServer proxy;
    private final MessageBroker messageBroker;
    private final PlayerRepository playerRepo;
    private final SectorManager sectorManager;
    private final VelocityQueueManager queueManager;

    public VelocityTransferHandler(ProxyServer proxy, MessageBroker messageBroker,
                                   PlayerRepository playerRepo, SectorManager sectorManager,
                                   VelocityQueueManager queueManager) {
        this.proxy = proxy;
        this.messageBroker = messageBroker;
        this.playerRepo = playerRepo;
        this.sectorManager = sectorManager;
        this.queueManager = queueManager;

        messageBroker.subscribeTelemetry("sector.transfer.request", this::handleTransferRequest);
        messageBroker.subscribeTelemetry("sector.admin.tp", this::handleAdminTp);
    }

    private void handleTransferRequest(byte[] data) {
        String payload = new String(data);
        String[] parts = payload.split(":");
        if (parts.length < 2) return;

        UUID uuid = UUID.fromString(parts[0]);
        String targetSectorId = parts[1];
        boolean forceSpawn = parts.length >= 3 && parts[2].equals("force_spawn");

        proxy.getPlayer(uuid).ifPresent(player -> {
            if (targetSectorId.equals("limbo") && forceSpawn) {
                var limboSector = sectorManager.getSector("limbo");
                if (limboSector != null) {
                    proxy.getServer(limboSector.serverName()).ifPresent(limboServer -> {
                        player.createConnectionRequest(limboServer).connect();
                        queueManager.addToQueue(player, true);
                    });
                }
                return;
            }

            var targetSector = sectorManager.getSector(targetSectorId);
            if (targetSector == null) return;

            proxy.getServer(targetSector.serverName()).ifPresent(targetServer -> {
                playerRepo.updateLastSector(uuid, targetSectorId);
                player.createConnectionRequest(targetServer).connect();
            });
        });
    }

    private void handleAdminTp(byte[] data) {
        String payload = new String(data);
        String[] parts = payload.split(":");
        if (parts.length < 2) return;

        String targetPlayerName = parts[0];
        String targetSectorId = parts[1];

        proxy.getPlayer(targetPlayerName).ifPresent(player -> {
            var targetSector = sectorManager.getSector(targetSectorId);
            if (targetSector == null) return;

            proxy.getServer(targetSector.serverName()).ifPresent(targetServer -> {
                playerRepo.updateLastSector(player.getUniqueId(), targetSectorId);
                player.createConnectionRequest(targetServer).connect();
            });
        });
    }
}
