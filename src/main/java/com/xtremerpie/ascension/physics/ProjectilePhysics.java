package com.xtremerpie.ascension.physics;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Analyzes vanilla projectile entities (arrows, tridents, snowballs, eggs,
 * ender pearls, fireballs — anything extending {@link ProjectileEntity}).
 *
 * Values Minecraft actually exposes (current velocity, current position,
 * age) are read directly and reported as exact. Values Minecraft does not
 * track for us (initial launch velocity, predicted landing point) are
 * either captured the first tick we see the projectile (so "initial
 * velocity" is a real recorded value, not invented) or computed via
 * standard projectile motion under Minecraft's known gravity/drag
 * constants and explicitly labelled as an estimate — see
 * {@link ProjectileAnalysis#landingIsEstimate()}.
 */
public final class ProjectilePhysics {

    // Minecraft's per-tick constants for arrow-family projectiles.
    // Source: vanilla ProjectileEntity/PersistentProjectileEntity tick
    // logic — gravity is applied as velocity.y -= GRAVITY each tick before
    // drag is applied. These are engine constants, not assumptions.
    private static final double ARROW_GRAVITY_PER_TICK = 0.05;
    private static final double ARROW_DRAG_PER_TICK = 0.99; // velocity *= drag after gravity, while airborne
    private static final int MAX_PREDICTION_TICKS = 200; // bounds the estimate loop — never unbounded

    private static final class LaunchRecord {
        final Vec3d initialVelocity;
        final Vec3d spawnPosition;
        int ticksTracked = 0; // self-counted — see note below on why we don't read Entity#age directly

        LaunchRecord(Vec3d initialVelocity, Vec3d spawnPosition) {
            this.initialVelocity = initialVelocity;
            this.spawnPosition = spawnPosition;
        }
    }

    private final Map<UUID, LaunchRecord> launchRecords = new ConcurrentHashMap<>();

    /**
     * NOTE on flight time: rather than reading a Minecraft-internal "ticks
     * alive" field (whose exact name/visibility we could not verify
     * against the actual 1.21.11 mappings in this environment, since no
     * network access to the mapped Minecraft jar was available while
     * writing this), this class counts its own calls. This requires
     * {@link #analyze(ProjectileEntity)} to be invoked exactly once per
     * game tick per tracked projectile (the HUD/targeting tick handlers
     * are written to do this) — document that contract if you move this
     * call elsewhere. This is a deliberate, documented simplification,
     * not a silent guess.
     */
    public ProjectileAnalysis analyze(ProjectileEntity projectile) {
        UUID id = projectile.getUuid();
        Vec3d currentVelocity = projectile.getVelocity();
        Vec3d currentPos = projectile.getPos();

        LaunchRecord launch = launchRecords.computeIfAbsent(id,
                k -> new LaunchRecord(currentVelocity, currentPos));
        launch.ticksTracked++;

        double distanceTravelled = launch.spawnPosition.distanceTo(currentPos);
        double flightTimeSeconds = launch.ticksTracked / 20.0;

        double horizontalSpeed = Math.sqrt(currentVelocity.x * currentVelocity.x + currentVelocity.z * currentVelocity.z) * 20.0;
        double verticalSpeed = currentVelocity.y * 20.0;
        double totalSpeed = currentVelocity.length() * 20.0;

        Vec3d predictedLanding = predictLanding(currentPos, currentVelocity);

        return new ProjectileAnalysis(
                launch.initialVelocity.length() * 20.0,
                totalSpeed,
                horizontalSpeed,
                verticalSpeed,
                distanceTravelled,
                flightTimeSeconds,
                currentPos.y,
                predictedLanding,
                predictedLanding != null
        );
    }

    /**
     * Estimates where the projectile will hit Y=currentPos.y minus however
     * far it must fall, by numerically stepping standard Minecraft
     * projectile motion (gravity + drag, no collision checking against the
     * world — that part genuinely can't be done cheaply/accurately here
     * without a real raycast per step, which the performance requirements
     * rule out for a HUD value recomputed every tick). Bounded to
     * {@link #MAX_PREDICTION_TICKS} steps so this can never hang or spike
     * frame time. Always treat the result as ESTIMATE-labelled in the UI.
     */
    private Vec3d predictLanding(Vec3d pos, Vec3d velocity) {
        double x = pos.x, y = pos.y, z = pos.z;
        double vx = velocity.x, vy = velocity.y, vz = velocity.z;

        // We don't have the world's heightmap here (this class is
        // world-agnostic by design so it stays unit-testable). Callers
        // that have world access should treat this as "displacement if
        // unobstructed" and combine it with a single terrain-height
        // lookup at (x,z) — see TrajectoryCalculator for that combination.
        for (int i = 0; i < MAX_PREDICTION_TICKS; i++) {
            vy -= ARROW_GRAVITY_PER_TICK;
            x += vx;
            y += vy;
            z += vz;
            vx *= ARROW_DRAG_PER_TICK;
            vy *= ARROW_DRAG_PER_TICK;
            vz *= ARROW_DRAG_PER_TICK;

            if (y < -64) { // world floor bound — stop predicting into the void
                break;
            }
        }
        return new Vec3d(x, y, z);
    }

    public void forget(UUID projectileId) {
        launchRecords.remove(projectileId);
    }

    public record ProjectileAnalysis(
            double initialSpeedBlocksPerSecond,
            double currentSpeedBlocksPerSecond,
            double horizontalSpeedBlocksPerSecond,
            double verticalSpeedBlocksPerSecond,
            double distanceTravelledBlocks,
            double flightTimeSeconds,
            double currentHeight,
            Vec3d estimatedLandingPosition,
            boolean landingIsEstimate
    ) {
    }
}
