package com.xtremerpie.ascension.statistics;

import com.xtremerpie.ascension.achievements.AchievementManager;
import com.xtremerpie.ascension.persistence.PlayerData;
import com.xtremerpie.ascension.persistence.PlayerDataManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side glue: small, explicit methods called from real gameplay
 * events (player tick, entity death, projectile hit, structure built,
 * block placed) that update {@link AscensionStatistics} and then trigger
 * {@link AchievementManager#evaluate}. Kept as narrow event-shaped methods
 * rather than a generic "fire event" bus, so it's obvious from a call site
 * what actually happened.
 */
public final class StatisticsManager {

    private final PlayerDataManager playerDataManager;
    private final AchievementManager achievementManager;
    private final ProjectileTracker projectileTracker;

    // Per-player last-tick position, purely for distance-travelled
    // accumulation — bounded to currently-online players.
    private final Map<UUID, Vec3d> lastTickPosition = new HashMap<>();

    public StatisticsManager(PlayerDataManager playerDataManager, AchievementManager achievementManager) {
        this.playerDataManager = playerDataManager;
        this.achievementManager = achievementManager;
        this.projectileTracker = new ProjectileTracker(this);
    }

    public void onPlayerTick(ServerPlayerEntity player) {
        PlayerData data = playerDataManager.get(player);
        AscensionStatistics stats = data.statistics();

        stats.increment("time_played_ticks", 1);

        Vec3d current = player.getPos();
        Vec3d last = lastTickPosition.put(player.getUuid(), current);
        if (last != null) {
            double distanceThisTick = last.distanceTo(current);
            stats.increment("distance_travelled_blocks", distanceThisTick);
            stats.setIfGreater("highest_velocity_blocks_per_second", distanceThisTick * 20.0);
        }

        stats.setIfGreater("highest_altitude_y", current.y);
        double seaLevel = 64.0;
        stats.setIfGreater("depth_below_sea_level_blocks", Math.max(0, seaLevel - current.y));

        projectileTracker.tick(player);

        achievementManager.evaluate(player, data);
    }

    public void onMobKilled(ServerPlayerEntity player) {
        PlayerData data = playerDataManager.get(player);
        data.statistics().increment("mobs_killed", 1);
        achievementManager.evaluate(player, data);
    }

    public void onArrowFired(ServerPlayerEntity player) {
        PlayerData data = playerDataManager.get(player);
        data.statistics().increment("projectiles_fired", 1);
        achievementManager.evaluate(player, data);
    }

    public void onArrowHit(ServerPlayerEntity player, double distanceBlocks) {
        PlayerData data = playerDataManager.get(player);
        data.statistics().increment("projectiles_hit", 1);
        data.statistics().setIfGreater("longest_projectile_hit_blocks", distanceBlocks);
        achievementManager.evaluate(player, data);
    }

    public void onSpeedMeasured(ServerPlayerEntity player, double blocksPerSecond) {
        PlayerData data = playerDataManager.get(player);
        data.statistics().setIfGreater("highest_velocity_blocks_per_second", blocksPerSecond);
        achievementManager.evaluate(player, data);
    }

    public void onBlockPlaced(ServerPlayerEntity player, int count) {
        PlayerData data = playerDataManager.get(player);
        data.statistics().increment("blocks_placed", count);
        achievementManager.evaluate(player, data);
    }

    public void onStructureBuilt(ServerPlayerEntity player) {
        PlayerData data = playerDataManager.get(player);
        data.statistics().increment("structures_built", 1);
        achievementManager.evaluate(player, data);
    }

    public void onEntityObserved(ServerPlayerEntity player) {
        PlayerData data = playerDataManager.get(player);
        data.statistics().increment("entities_observed", 1);
        achievementManager.evaluate(player, data);
    }

    public void forgetPlayer(UUID playerId) {
        lastTickPosition.remove(playerId);
    }
}
