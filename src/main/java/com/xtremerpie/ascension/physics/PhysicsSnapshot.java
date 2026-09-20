package com.xtremerpie.ascension.physics;

/**
 * Immutable snapshot of a moving entity's analyzed physics state for one
 * tick. Produced by {@link MovementPhysics}, consumed by the HUD.
 *
 * All velocity values are derived from Minecraft's own per-tick position
 * delta (current pos - previous pos), which is the same signal the vanilla
 * client already tracks for things like fall damage and sprint particles —
 * ASCENSION does not run a parallel physics simulation, it only measures
 * Minecraft's real one.
 */
public record PhysicsSnapshot(
        double speedBlocksPerSecond,
        double horizontalSpeedBlocksPerSecond,
        double verticalSpeedBlocksPerSecond,
        double directionDegrees,
        double distanceTravelledThisTick,
        MovementState state,
        boolean onGround,
        boolean sprinting,
        boolean sneaking,
        boolean inFluid
) {
    public static PhysicsSnapshot idle() {
        return new PhysicsSnapshot(0, 0, 0, 0, 0, MovementState.IDLE, true, false, false, false);
    }
}
