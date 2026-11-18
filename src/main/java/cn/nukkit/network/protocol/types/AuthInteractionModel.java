package cn.nukkit.network.protocol.types;

public enum AuthInteractionModel {
    TOUCH, CROSSHAIR, CLASSIC;

    private static final AuthInteractionModel[] VALUES = values();

    public static AuthInteractionModel fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= VALUES.length) {
            throw new IllegalArgumentException("Unknown auth interaction model: " + ordinal);
        }
        return VALUES[ordinal];
    }
}
