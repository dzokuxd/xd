package pl.twojnick.sectors.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;

public class VelocityPluginMessageListener {
    private final VelocityPlugin plugin;

    public VelocityPluginMessageListener(VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!(event.getIdentifier() instanceof MinecraftChannelIdentifier identifier)) return;

        if (identifier.equals(VelocityPlugin.SECTOR_REQUEST_CHANNEL)) {
            if (event.getSource() instanceof RegisteredServer server) {
                String serverName = server.getServerInfo().getName();
                String sectorId = plugin.getSectorIdForServer(serverName);

                if (sectorId != null) {
                    plugin.sendSectorInfo(server, sectorId);
                }
            }
            event.setResult(PluginMessageEvent.ForwardResult.handled());
        }
    }
}