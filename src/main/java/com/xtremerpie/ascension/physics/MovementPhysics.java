package com.xtremerpie.ascension.physics;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Measures an entity's real per-tick displacement and turns it into
 * human-readable speed/direction/state values.
 *
 * IMPORTANT: this is an ANALYSIS layer, not a physics engine. It never
 * predicts or alters motion — it only diffs the entity's actual position
 * between two calls (one call is expected per game tick per tracked
 * entity) and reports on it. This matches spec section 8/12: "the system
 * must not create its own fake physics that contradicts Minecraft."
 *
 * Tick length is fixed at 1/20s (Minecraft's server tick rate); this is a
 * Minecraft engine constant, not an assumption.
 */
public final class MovementPhysics {

    private static final double SECONDS_PER_TICK = 1.0 / 20.0;

    // Per-entity last-known position, so we can diff on the next tick.
    // Bounded by however many entities are actually being observed by HUD
    // modules or the targeting system — never the whole world — so this
    // map stays small in practice. Stale entries are pruned by callers via
    // {@link #forget(UUID)} when an entity stops being observed.
    private final Map<UUID, Vec3d> lastPositions = new ConcurrentHashMap<>();

    public PhysicsSnapshot analyze(Entity entity) {
        if (entity == null) {
            return PhysicsSnapshot.idle();
        }

        Vec3d current = entity.getPos();
        Vec3d previous = lastPositions.put(entity.getUuid(), current);

        if (previous == null) {
            // First observation of this entity — no delta available yet.
            return PhysicsSnapshot.idle();
        }

        double dx = current.x - previous.x;
        double dy = current.y - previous.y;
        double dz = current.z - previous.z;

        double distanceThisTick = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double horizontalDistanceThisTick = Math.sqrt(dx * dx + dz * dz);

        double speed = distanceThisTick / SECONDS_PER_TICK;
        double horizontalSpeed = horizontalDistanceThisTick / SECONDS_PER_TICK;
        double verticalSpeed = dy / SECONDS_PER_TICK;

        double direction = (horizontalDistanceThisTick > 1.0e-6)
                ? normalizeDegrees(Math.toDegrees(Math.atan2(dx, -dz)))
                : entity.getYaw();

        boolean onGround = entity.isOnGround();
        boolean sprinting = entity.isSprinting();
        boolean sneaking = entity.isSneaking();
        boolean inFluid = entity.isTouchingWater() || entity.isInLava();

        MovementState state = classify(entity, horizontalSpeed, verticalSpeed, onGround, sprinting, sneaking, inFluid);

        return new PhysicsSnapshot(
                speed,
                horizontalSpeed,
                verticalSpeed,
                direction,
                distanceThisTick,
                state,
                onGround,
                sprinting,
                sneaking,
                inFluid
        );
    }

    private MovementState classify(Entity entity, double horizontalSpeed, double verticalSpeed,
                                    boolean onGround, boolean sprinting, boolean sneaking, boolean inFluid) {
        if (entity instanceof PlayerEntity player && (player.getAbilities().flying)) {
            return MovementState.FLYING;
        }
        if (entity instanceof LivingEntity living && living.isClimbing()) {
            return MovementState.CLIMBING;
        }
        if (inFluid) {
            return MovementState.SWIMMING;
        }
        if (!onGround) {
            if (verticalSpeed < -1.0) {
                return MovementState.FALLING;
            }
            if (verticalSpeed > 1.0) {
                return MovementState.JUMPING;
            }
        }
        if (sneaking && horizontalSpeed > 0.05) {
            return MovementState.CROUCHING;
        }
        if (sprinting && horizontalSpeed > 0.05) {
            return MovementState.SPRINTING;
        }
        if (horizontalSpeed > 3.5) {
            return MovementState.RUNNING;
        }
        if (horizontalSpeed > 0.15) {
            return MovementState.WALKING;
        }
        return MovementState.IDLE;
    }

    private static double normalizeDegrees(double degrees) {
        double d = degrees % 360.0;
        if (d < 0) d += 360.0;
        return d;
    }

    /** Call when an entity is no longer being observed, to bound map growth. */
    public void forget(UUID entityId) {
        lastPositions.remove(entityId);
    }

    public int trackedEntityCount() {
        return lastPositions.size();
    }
}
