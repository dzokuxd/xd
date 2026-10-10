package pl.twojnick.sectors.purpur;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import pl.twojnick.sectors.core.config.SectorConfig;
import pl.twojnick.sectors.core.database.DatabaseManager;
import pl.twojnick.sectors.core.database.PlayerRepository;
import pl.twojnick.sectors.core.database.Rank;
import pl.twojnick.sectors.core.logic.SectorManager;
import pl.twojnick.sectors.core.messaging.MessageBroker;
import pl.twojnick.sectors.core.status.SectorStatusTracker;
import pl.twojnick.sectors.core.api.PermissionRegistry;
import pl.twojnick.sectors.purpur.teleport.TeleportManager;
import pl.twojnick.sectors.purpur.sync.PlayerDataSyncManager;
import pl.twojnick.sectors.purpur.sync.PlayerDataRestoreListener;
import pl.twojnick.sectors.purpur.listeners.DeathListener;
import pl.twojnick.sectors.purpur.listeners.AfkManager;
import pl.twojnick.sectors.purpur.listeners.ChatListener;
import pl.twojnick.sectors.purpur.listeners.HeartbeatListener;
import pl.twojnick.sectors.purpur.commands.SpawnCommand;
import pl.twojnick.sectors.purpur.commands.AfkCommand;
import pl.twojnick.sectors.purpur.commands.kit.KitCommand;
import pl.twojnick.sectors.purpur.commands.admin.RankCommand;
import pl.twojnick.sectors.purpur.commands.admin.SektorCommand;
import pl.twojnick.sectors.purpur.commands.guild.GuildCommand;
import pl.twojnick.sectors.purpur.api.command.CommandRegistry;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PurpurPlugin extends JavaPlugin implements PluginMessageListener {
    private SectorConfig config;
    private SectorManager sectorManager;
    private DatabaseManager dbManager;
    private PlayerRepository playerRepo;
    private MessageBroker messageBroker;
    private SectorStatusTracker statusTracker;
    private TeleportManager teleportManager;
    private PlayerDataSyncManager playerDataSyncManager;
    private String currentSectorId;
    private boolean sectorDetected = false;

    @Override
    public void onEnable() {
        // Ładowanie konfiguracji za pomocą BoostedYAML
        try {
            YamlDocument yamlDoc = YamlDocument.create(
                    new File(getDataFolder(), "config.yml"),
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

        } catch (IOException e) {
            getLogger().severe("Nie udało się załadować konfiguracji: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.dbManager = new DatabaseManager();
        this.dbManager.init(config);

        this.playerRepo = new PlayerRepository(dbManager.getDataSource(), dbManager.getExecutor());
        this.sectorManager = new SectorManager(config, playerRepo);
        this.statusTracker = new SectorStatusTracker();

        this.messageBroker = new RedisNatsBrokerImpl(config);
        this.messageBroker.connect();

        this.teleportManager = new TeleportManager(this, messageBroker, statusTracker);
        this.playerDataSyncManager = new PlayerDataSyncManager(this);

        // Zarejestruj kanały Plugin Message
        getServer().getMessenger().registerIncomingPluginChannel(this, "sectors:sector_info", this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "sectors:sector_request");

        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new AfkManager(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDataRestoreListener(this), this);
        getServer().getPluginManager().registerEvents(new HeartbeatListener(this), this);

        registerPermissions();
        registerCommands();

        // Poproś Velocity o nazwę sektora
        requestSectorFromVelocity();

        getLogger().info("Sectors Purpur plugin enabled!");
    }

    private void requestSectorFromVelocity() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                getLogger().warning("Brak graczy do wysłania Plugin Message. Używam fallback z config.");
                this.currentSectorId = "spawn";
                this.sectorDetected = true;
                return;
            }

            Player player = Bukkit.getOnlinePlayers().iterator().next();
            player.sendPluginMessage(this, "sectors:sector_request", new byte[0]);
            getLogger().info("Wysłano żądanie nazwy sektora do Velocity...");

            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (!sectorDetected) {
                    getLogger().warning("Timeout! Nie otrzymano odpowiedzi od Velocity. Używam fallback.");
                    this.currentSectorId = "spawn";
                    this.sectorDetected = true;
                }
            }, 100L);
        }, 40L);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (channel.equals("sectors:sector_info")) {
            String sectorId = new String(message, StandardCharsets.UTF_8);
            this.currentSectorId = sectorId;
            this.sectorDetected = true;
            getLogger().info("✓ Otrzymano nazwę sektora od Velocity: " + sectorId);
        }
    }

    @Override
    public void onDisable() {
        if (messageBroker != null) messageBroker.disconnect();
        if (dbManager != null) dbManager.close();
    }

    private void registerPermissions() {
        PermissionRegistry.register("kit.vip", Rank.VIP);
        PermissionRegistry.register("kit.svip", Rank.SVIP);
        PermissionRegistry.register("kit.mvp", Rank.MVP);

        PermissionRegistry.register("cmd.fly", Rank.MVP);
        PermissionRegistry.register("cmd.heal", Rank.SVIP);
        PermissionRegistry.register("cmd.god", Rank.ADMIN);
        PermissionRegistry.register("cmd.vanish", Rank.MOD);

        PermissionRegistry.register("sector.tp", Rank.ADMIN);
        PermissionRegistry.register("sector.event", Rank.ADMIN);

        PermissionRegistry.register("guild.create", Rank.VIP);
    }

    private void registerCommands() {
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("afk").setExecutor(new AfkCommand(this));

        CommandRegistry registry = new CommandRegistry();
        registry.add(new KitCommand(playerRepo));
        registry.add(new RankCommand(playerRepo));
        registry.add(new SektorCommand(playerRepo, messageBroker));
        registry.add(new GuildCommand(playerRepo, messageBroker));
        registry.registerAll(this);
    }

    public SectorManager getSectorManager() { return sectorManager; }
    public MessageBroker getMessageBroker() { return messageBroker; }
    public DatabaseManager getDbManager() { return dbManager; }
    public PlayerRepository getPlayerRepo() { return playerRepo; }
    public SectorStatusTracker getStatusTracker() { return statusTracker; }
    public TeleportManager getTeleportManager() { return teleportManager; }
    public PlayerDataSyncManager getPlayerDataSyncManager() { return playerDataSyncManager; }
    public String getCurrentSectorId() { return currentSectorId; }
}