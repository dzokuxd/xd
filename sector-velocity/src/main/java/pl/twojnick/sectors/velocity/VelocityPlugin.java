package pl.twojnick.sectors.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import pl.twojnick.sectors.core.config.SectorConfig;
import pl.twojnick.sectors.core.database.DatabaseManager;
import pl.twojnick.sectors.core.database.PlayerRepository;
import pl.twojnick.sectors.core.logic.SectorManager;
import pl.twojnick.sectors.core.messaging.MessageBroker;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;

@Plugin(id = "sectors-velocity", name = "SectorsVelocity", version = "1.0")
public class VelocityPlugin {
    private final ProxyServer server;
    private final Path dataDirectory;
    private SectorConfig config;
    private MessageBroker messageBroker;
    private DatabaseManager dbManager;
    private PlayerRepository playerRepo;
    private SectorManager sectorManager;
    private VelocityQueueManager queueManager;
    private VelocityTransferHandler transferHandler;
    private VelocityChatHandler chatHandler;

    private final Map<String, String> serverToSectorMap = new HashMap<>();

    public static final MinecraftChannelIdentifier SECTOR_INFO_CHANNEL =
            MinecraftChannelIdentifier.create("sectors", "sector_info");
    public static final MinecraftChannelIdentifier SECTOR_REQUEST_CHANNEL =
            MinecraftChannelIdentifier.create("sectors", "sector_request");

    @Inject
    public VelocityPlugin(ProxyServer server, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Ładowanie konfiguracji za pomocą BoostedYAML
        try {
            YamlDocument yamlDoc = YamlDocument.create(
                    dataDirectory.resolve("config.yml").toFile(),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.DEFAULT
            );

            this.config = new SectorConfig();
            this.config.dbHost = yamlDoc.getString("dbHost", "localhost");
            this.config.dbPort = yamlDoc.getInt("dbPort", 3306);
            this.config.dbUser = yamlDoc.getString("dbUser", "root");
            this.config.dbPassword = yamlDoc.getString("dbPassword", "password");
            this.config.dbName = yamlDoc.getString("dbName", "sectors");

            this.config.redisHost = yamlDoc.getString("redisHost", "localhost");
            this.config.redisPort = yamlDoc.getInt("redisPort", 6379);
            this.config.redisPassword = yamlDoc.getString("redisPassword", "");
            this.config.natsUrl = yamlDoc.getString("natsUrl", "nats://localhost:4222");

        } catch (Exception e) {
            return;
        }

        this.dbManager = new DatabaseManager();
        this.dbManager.init(config);

        this.playerRepo = new PlayerRepository(dbManager.getDataSource(), dbManager.getExecutor());
        this.sectorManager = new SectorManager(config, playerRepo);

        this.messageBroker = new VelocityRedisNatsBrokerImpl(config);
        this.messageBroker.connect();

        this.queueManager = new VelocityQueueManager(server, messageBroker, playerRepo, sectorManager, this);
        this.transferHandler = new VelocityTransferHandler(server, messageBroker, playerRepo, sectorManager, queueManager);
        this.chatHandler = new VelocityChatHandler(server, messageBroker);

        buildServerToSectorMap();

        server.getChannelRegistrar().register(SECTOR_INFO_CHANNEL);
        server.getChannelRegistrar().register(SECTOR_REQUEST_CHANNEL);

        server.getEventManager().register(this, new VelocityLoginListener(server, sectorManager, queueManager));
        server.getEventManager().register(this, new VelocityHeartbeatHandler(server, messageBroker));
        server.getEventManager().register(this, new VelocityPluginMessageListener(this));
    }

    private void buildServerToSectorMap() {
        config.sectors.forEach(sector -> {
            serverToSectorMap.put(sector.serverName(), sector.id());
        });
    }

    public String getSectorIdForServer(String serverName) {
        return serverToSectorMap.get(serverName);
    }

    public void sendSectorInfo(RegisteredServer targetServer, String sectorId) {
        byte[] data = sectorId.getBytes(StandardCharsets.UTF_8);
        targetServer.sendPluginMessage(SECTOR_INFO_CHANNEL, data);
    }

    public ProxyServer getServer() { return server; }
    public SectorManager getSectorManager() { return sectorManager; }
    public PlayerRepository getPlayerRepo() { return playerRepo; }
}