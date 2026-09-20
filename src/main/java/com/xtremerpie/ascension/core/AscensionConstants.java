package com.xtremerpie.ascension.core;

/**
 * Central constants for the ASCENSION mod. Kept here so mod-id-style
 * strings and tunable defaults aren't scattered/duplicated across systems.
 */
public final class AscensionConstants {

    private AscensionConstants() {
    }

    public static final String MOD_ID = "ascension";
    public static final String MOD_NAME = "Ascension";

    // Default performance-conscious update intervals, in server/client ticks
    // (20 ticks = 1 second). These are overridden by AscensionConfig at
    // runtime; they exist here only as the "factory reset" values.
    public static final int DEFAULT_HUD_UPDATE_INTERVAL_TICKS = 4;      // ~5x/sec
    public static final int DEFAULT_WORLD_SCAN_INTERVAL_TICKS = 20;     // 1x/sec
    public static final int DEFAULT_TRAJECTORY_UPDATE_INTERVAL_TICKS = 2;
    public static final int DEFAULT_ENTITY_SCAN_RANGE_BLOCKS = 32;
    public static final int MAX_ENTITY_SCAN_RANGE_BLOCKS = 64;

    public static final int MAX_STRUCTURE_DIMENSION_BLOCKS = 24;

    public static final double BLOCKS_TO_METERS = 1.0; // 1 Minecraft block == 1 meter, by design (see spec section 6)

    public static String id(String path) {
        return MOD_ID + ":" + path;
    }
}
