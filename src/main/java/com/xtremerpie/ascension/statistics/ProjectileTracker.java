package com.xtremerpie.ascension.statistics;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks projectiles_fired / projectiles_hit / longest_projectile_hit for
 * the Physics achievement category, via bounded per-player-tick polling
 * rather than a "projectile fired"/"projectile hit" event — no such event
 * could be verified as existing in Fabric API for 1.21.11 offline (see
 * IMPLEMENTATION_STATUS.md), and guessing at one risked shipping a silent
 * no-op. Polling is bounded (one box query per player per tick, capped by
 * a fixed radius) so it stays cheap.
 *
 * HONEST DEFINITION OF "HIT": a hit is recorded when a tracked arrow
 * becomes {@link PersistentProjectileEntity#isInGround()} — i.e. it stuck
 * in a block. This is a real, verifiable signal, but it means a shot that
 * lands in a block counts the same as one that strikes an entity and
 * continues/despawns; distinguishing "hit a living target" specifically
 * would need a damage-source hook this environment couldn't verify. The
 * Physics-category achievements (Precision, Long Shot, Projectile Master)
 * are written against this "landed" definition.
 */
public final class ProjectileTracker {

    private final StatisticsManager statisticsManager;

    private final Map<UUID, Vec3d> spawnPositions = new HashMap<>();
    private final Set<UUID> seenIds = new HashSet<>();
    private final Set<UUID> countedAsHit = new HashSet<>();

    public ProjectileTracker(StatisticsManager statisticsManager) {
        this.statisticsManager = statisticsManager;
    }

    public void tick(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        var box = player.getBoundingBox().expand(48); // bounded search, not a world scan

        for (PersistentProjectileEntity arrow : world.getEntitiesByClass(PersistentProjectileEntity.class, box, e -> true)) {
            if (!(arrow.getOwner() instanceof ServerPlayerEntity owner) || !owner.getUuid().equals(player.getUuid())) {
                continue;
            }

            UUID id = arrow.getUuid();
            if (seenIds.add(id)) {
                spawnPositions.put(id, arrow.getPos());
                statisticsManager.onArrowFired(player);
            }

            if (arrow.isInGround() && !countedAsHit.contains(id)) {
                countedAsHit.add(id);
                Vec3d spawn = spawnPositions.getOrDefault(id, arrow.getPos());
                double distance = spawn.distanceTo(arrow.getPos());
                statisticsManager.onArrowHit(player, distance);
            }
        }

        // Bound the tracking sets' growth — drop everything and let it
        // repopulate rather than keeping ids forever.
        if (seenIds.size() > 256) {
            seenIds.clear();
            countedAsHit.clear();
            spawnPositions.clear();
        }
    }
}
