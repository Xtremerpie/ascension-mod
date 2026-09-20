package com.xtremerpie.ascension.physics;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a short, bounded list of points describing a projectile's
 * estimated future path, for the trajectory renderer. World-aware (unlike
 * {@link ProjectilePhysics}) so it can stop the path at real terrain.
 *
 * Performance: point count and step count are both capped by config
 * (default conservative values below) and this never runs more than once
 * per {@code updateIntervalTicks} per projectile — see spec section 10/12.
 */
public final class TrajectoryCalculator {

    private static final double GRAVITY_PER_TICK = 0.05;
    private static final double DRAG_PER_TICK = 0.99;

    public List<Vec3d> computePath(World world, Vec3d startPos, Vec3d startVelocity,
                                    int maxSteps, int sampleEveryNTicks) {
        List<Vec3d> points = new ArrayList<>(Math.max(1, maxSteps / Math.max(1, sampleEveryNTicks)));

        double x = startPos.x, y = startPos.y, z = startPos.z;
        double vx = startVelocity.x, vy = startVelocity.y, vz = startVelocity.z;

        points.add(new Vec3d(x, y, z));

        for (int tick = 1; tick <= maxSteps; tick++) {
            vy -= GRAVITY_PER_TICK;
            x += vx;
            y += vy;
            z += vz;
            vx *= DRAG_PER_TICK;
            vy *= DRAG_PER_TICK;
            vz *= DRAG_PER_TICK;

            if (world != null) {
                BlockPos check = BlockPos.ofFloored(x, y, z);
                if (!world.isInBuildLimit(check) || !world.getBlockState(check).isAir()) {
                    points.add(new Vec3d(x, y, z));
                    break; // hit something (or left the world) — stop the path here
                }
            }

            if (tick % Math.max(1, sampleEveryNTicks) == 0) {
                points.add(new Vec3d(x, y, z));
            }
        }

        return points;
    }
}
