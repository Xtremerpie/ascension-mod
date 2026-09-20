package com.xtremerpie.ascension.targeting;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * A safe, read-only description of whatever the player is currently
 * looking at. Every field here is something the vanilla client already
 * legitimately has (entity is either loaded/rendered client-side, or it's
 * a block the client has already received). This deliberately does NOT
 * expose anything a normal client wouldn't already know — e.g. health is
 * only ever populated from the entity's already-synced health value, never
 * queried server-side out-of-band.
 */
public record TargetSnapshot(
        TargetType type,
        Entity entity,
        String displayName,
        double distance,
        double horizontalDistance,
        double verticalDifference,
        double bearingDegrees,
        String compassLabel,
        Vec3d velocity,
        Float health,
        Float maxHealth
) {
    public enum TargetType {
        NONE, PLAYER, ENTITY, BLOCK
    }

    public static TargetSnapshot none() {
        return new TargetSnapshot(TargetType.NONE, null, null, 0, 0, 0, 0, "-", Vec3d.ZERO, null, null);
    }

    public boolean isPresent() {
        return type != TargetType.NONE;
    }
}
