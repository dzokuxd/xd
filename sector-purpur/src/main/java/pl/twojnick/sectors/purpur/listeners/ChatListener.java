package pl.twojnick.sectors.purpur.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import pl.twojnick.sectors.core.database.PlayerRepository;
import pl.twojnick.sectors.core.database.Rank;
import pl.twojnick.sectors.core.messaging.MessageBroker;
import pl.twojnick.sectors.purpur.PurpurPlugin;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class ChatListener implements Listener {
    private final PurpurPlugin plugin;
    private final PlayerRepository playerRepo;
    private final MessageBroker messageBroker;
    private final Gson gson = new Gson();

    public ChatListener(PurpurPlugin plugin) {
        this.plugin = plugin;
        this.playerRepo = plugin.getPlayerRepo();
        this.messageBroker = plugin.getMessageBroker();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        event.setCancelled(true);

        playerRepo.getPlayerData(player.getUniqueId()).thenAccept(data -> {
            String message = PlainTextComponentSerializer.plainText().serialize(event.message());
            String sectorId = plugin.getCurrentSectorId();
            Rank rank = data.rank();

            Map<String, String> chatData = new HashMap<>();
            chatData.put("uuid", player.getUniqueId().toString());
            chatData.put("name", player.getName());
            chatData.put("rank", rank.name());
            chatData.put("sector", sectorId);
            chatData.put("message", message);

            String payload = gson.toJson(chatData);
            messageBroker.publishTelemetry("sector.chat.broadcast", payload.getBytes());
        });
    }
}
