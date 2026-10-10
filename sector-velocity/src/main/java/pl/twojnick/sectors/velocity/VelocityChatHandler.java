package pl.twojnick.sectors.velocity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import pl.twojnick.sectors.core.database.Rank;
import pl.twojnick.sectors.core.messaging.MessageBroker;
import java.util.Map;

public class VelocityChatHandler {
    private final ProxyServer proxy;
    private final MessageBroker messageBroker;
    private final Gson gson = new Gson();

    public VelocityChatHandler(ProxyServer proxy, MessageBroker messageBroker) {
        this.proxy = proxy;
        this.messageBroker = messageBroker;

        messageBroker.subscribeTelemetry("sector.chat.broadcast", this::handleChat);
    }

    private void handleChat(byte[] data) {
        String payload = new String(data);

        Map<String, String> chatData = gson.fromJson(payload, new TypeToken<Map<String, String>>(){}.getType());

        String playerName = chatData.get("name");
        String rankName = chatData.get("rank");
        String sectorId = chatData.get("sector");
        String message = chatData.get("message");

        Rank rank = Rank.fromString(rankName);

        Component formattedMessage = buildFormattedMessage(sectorId, rank, playerName, message);

        for (Player player : proxy.getAllPlayers()) {
            player.sendMessage(formattedMessage);
        }
    }

    private Component buildFormattedMessage(String sectorId, Rank rank, String playerName, String message) {
        Component sectorComponent = Component.text("[" + sectorId.toUpperCase() + "] ")
            .color(NamedTextColor.GRAY)
            .decorate(TextDecoration.BOLD);

        Component rankComponent = Component.text("[" + rank.name() + "] ")
            .color(getRankColor(rank))
            .decorate(TextDecoration.BOLD);

        Component nameComponent = Component.text(playerName + ": ")
            .color(NamedTextColor.WHITE)
            .decorate(TextDecoration.BOLD);

        Component messageComponent = Component.text(message)
            .color(NamedTextColor.GRAY);

        return sectorComponent.append(rankComponent).append(nameComponent).append(messageComponent);
    }

    private NamedTextColor getRankColor(Rank rank) {
        return switch (rank) {
            case PLAYER -> NamedTextColor.GRAY;
            case VIP -> NamedTextColor.GREEN;
            case SVIP -> NamedTextColor.AQUA;
            case MVP -> NamedTextColor.GOLD;
            case HELPER -> NamedTextColor.BLUE;
            case MOD -> NamedTextColor.DARK_PURPLE;
            case ADMIN -> NamedTextColor.RED;
            case OWNER -> NamedTextColor.LIGHT_PURPLE;
        };
    }
}
