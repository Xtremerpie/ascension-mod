package com.xtremerpie.ascension.physics;

/**
 * Single access point the rest of the mod uses to reach the physics
 * subsystem, so HUD modules and commands don't each construct their own
 * MovementPhysics/ProjectilePhysics/TrajectoryCalculator instances (which
 * would break the per-entity tracking state those classes keep).
 */
public final class PhysicsCalculator {

    private final MovementPhysics movementPhysics = new MovementPhysics();
    private final ProjectilePhysics projectilePhysics = new ProjectilePhysics();
    private final TrajectoryCalculator trajectoryCalculator = new TrajectoryCalculator();

    public MovementPhysics movement() {
        return movementPhysics;
    }

    public ProjectilePhysics projectile() {
        return projectilePhysics;
    }

    public TrajectoryCalculator trajectory() {
        return trajectoryCalculator;
    }
}
