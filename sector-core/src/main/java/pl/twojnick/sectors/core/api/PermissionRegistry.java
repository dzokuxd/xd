package pl.twojnick.sectors.core.api;

import pl.twojnick.sectors.core.database.Rank;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PermissionRegistry {
    private static final Map<String, Rank> registry = new ConcurrentHashMap<>();

    private PermissionRegistry() {}

    public static void register(String permission, Rank requiredRank) {
        registry.put(permission, requiredRank);
    }

    public static boolean hasAccess(Rank playerRank, String permission) {
        Rank required = registry.get(permission);
        if (required == null) return false;
        return playerRank.isAtLeast(required);
    }

    public static Rank getRequiredRank(String permission) {
        return registry.get(permission);
    }
}
