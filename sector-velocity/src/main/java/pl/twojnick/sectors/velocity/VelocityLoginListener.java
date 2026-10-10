package pl.twojnick.sectors.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import pl.twojnick.sectors.core.logic.SectorManager;

public class VelocityLoginListener {
    private final com.velocitypowered.api.proxy.ProxyServer proxy;
    private final SectorManager sectorManager;
    private final VelocityQueueManager queueManager;

    public VelocityLoginListener(com.velocitypowered.api.proxy.ProxyServer proxy,
                                SectorManager sectorManager,
                                VelocityQueueManager queueManager) {
        this.proxy = proxy;
        this.sectorManager = sectorManager;
        this.queueManager = queueManager;
    }

    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        Player player = event.getPlayer();

        var limboSector = sectorManager.getLimboSector();
        proxy.getServer(limboSector.serverName()).ifPresent(limboServer -> {
            event.setInitialServer(limboServer);
            queueManager.addToQueue(player);
        });
    }
}
