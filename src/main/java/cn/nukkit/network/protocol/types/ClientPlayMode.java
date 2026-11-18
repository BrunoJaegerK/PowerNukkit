package cn.nukkit.network.protocol.types;

public enum ClientPlayMode {
    NORMAL, TEASER, SCREEN, VIEWER, REALITY, PLACEMENT, LIVING_ROOM, EXIT_LEVEL,
    EXIT_LEVEL_LIVING_ROOM;

    private static final ClientPlayMode[] VALUES = values();

    public static ClientPlayMode fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= VALUES.length) {
            throw new IllegalArgumentException("Unknown client play mode: " + ordinal);
        }
        return VALUES[ordinal];
    }
}
