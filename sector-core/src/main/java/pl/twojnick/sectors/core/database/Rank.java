package pl.twojnick.sectors.core.database;

public enum Rank {
    PLAYER(0),
    VIP(10),
    SVIP(20),
    MVP(30),
    HELPER(50),
    MOD(70),
    ADMIN(90),
    OWNER(100);

    private final int weight;

    Rank(int weight) {
        this.weight = weight;
    }

    public boolean isAtLeast(Rank required) {
        return this.weight >= required.weight;
    }

    public static Rank fromString(String name) {
        if (name == null) return PLAYER;
        try {
            return Rank.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PLAYER;
        }
    }
}
