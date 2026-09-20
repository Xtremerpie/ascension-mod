package com.xtremerpie.ascension.util;

import net.minecraft.util.math.Vec3d;

/**
 * Pure math helpers used by the physics/targeting/HUD systems. No Minecraft
 * world/entity access here on purpose — keep this class trivially testable
 * without a running game instance.
 */
public final class MathUtil {

    private MathUtil() {
    }

    /** 3D straight-line distance between two points, full double precision. */
    public static double distance3D(Vec3d a, Vec3d b) {
        return a.distanceTo(b);
    }

    /** Horizontal-plane (X/Z) distance, ignoring Y. */
    public static double distanceHorizontal(Vec3d a, Vec3d b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Signed vertical difference (b relative to a). Positive = b is higher. */
    public static double verticalDifference(Vec3d a, Vec3d b) {
        return b.y - a.y;
    }

    /**
     * Compass bearing in degrees [0, 360) from `from` toward `to`, using
     * Minecraft's convention where yaw 0 = south, -90 = east... but for a
     * human-readable compass we compute true north-based bearing instead,
     * matching what a player expects from an "NE" style label.
     */
    public static double bearingDegrees(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double angle = Math.toDegrees(Math.atan2(dx, -dz));
        return normalizeDegrees(angle);
    }

    public static double normalizeDegrees(double degrees) {
        double d = degrees % 360.0;
        if (d < 0) d += 360.0;
        return d;
    }

    /** Converts a bearing in degrees to an 8-point compass label. */
    public static String compassLabel(double bearingDegrees) {
        String[] labels = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int index = (int) Math.round(normalizeDegrees(bearingDegrees) / 45.0) % 8;
        return labels[index];
    }

    public static double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
