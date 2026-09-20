package com.xtremerpie.ascension.targeting;

import com.xtremerpie.ascension.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

/**
 * Converts a raw {@link HitResult} (from {@link RaycastService}) plus the
 * viewing player into a {@link TargetSnapshot}, computing only values the
 * client legitimately has.
 */
public final class EntityTargeting {

    public TargetSnapshot buildSnapshot(PlayerEntity viewer, HitResult hit) {
        if (!(hit instanceof EntityHitResult entityHit)) {
            return TargetSnapshot.none();
        }

        Entity target = entityHit.getEntity();
        Vec3d viewerPos = viewer.getPos();
        Vec3d targetPos = target.getPos();

        double distance = MathUtil.distance3D(viewerPos, targetPos);
        double horizontal = MathUtil.distanceHorizontal(viewerPos, targetPos);
        double vertical = MathUtil.verticalDifference(viewerPos, targetPos);
        double bearing = MathUtil.bearingDegrees(viewerPos, targetPos);
        String compass = MathUtil.compassLabel(bearing);

        Float health = null;
        Float maxHealth = null;
        if (target instanceof LivingEntity living) {
            // getHealth()/getMaxHealth() on an already-rendered entity are
            // exactly the synced values the client display already uses
            // (e.g. for the mob's health bar / boss bar) — not privileged data.
            health = living.getHealth();
            maxHealth = living.getMaxHealth();
        }

        TargetSnapshot.TargetType type = target instanceof PlayerEntity
                ? TargetSnapshot.TargetType.PLAYER
                : TargetSnapshot.TargetType.ENTITY;

        String name = target.getDisplayName() != null
                ? target.getDisplayName().getString()
                : target.getType().getName().getString();

        return new TargetSnapshot(
                type,
                target,
                name,
                distance,
                horizontal,
                vertical,
                bearing,
                compass,
                target.getVelocity(),
                health,
                maxHealth
        );
    }
}
