package com.xtremerpie.ascension.achievements;

import com.xtremerpie.ascension.persistence.PlayerData;
import com.xtremerpie.ascension.rewards.RewardManager;
import com.xtremerpie.ascension.statistics.AscensionStatistics;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.BiConsumer;

/**
 * Server-authoritative. Checks every registered achievement's condition
 * against the player's current statistics and, on completion, applies
 * rewards exactly once (unless {@code repeatable}) via
 * {@link RewardManager}. Never runs on the client — spec section 34
 * (rewards/achievement completion must be server-authoritative).
 */
public final class AchievementManager {

    private final AchievementRegistry registry;
    private final RewardManager rewardManager;

    /** Invoked with (player, definition) whenever an achievement newly completes, for notifications/networking. */
    private BiConsumer<ServerPlayerEntity, AchievementDefinition> onCompleted = (p, d) -> {};

    public AchievementManager(AchievementRegistry registry, RewardManager rewardManager) {
        this.registry = registry;
        this.rewardManager = rewardManager;
    }

    public void setOnCompleted(BiConsumer<ServerPlayerEntity, AchievementDefinition> callback) {
        this.onCompleted = callback;
    }

    /**
     * Re-evaluates every achievement for one player. Cheap: this is a
     * linear scan over the (small, ~31) achievement list comparing
     * already-tracked stat values — no world/entity queries happen here.
     * Called after any statistic changes (see StatisticsManager), not
     * every tick unconditionally.
     */
    public void evaluate(ServerPlayerEntity player, PlayerData data) {
        AscensionStatistics stats = data.statistics();

        for (AchievementDefinition def : registry.all().values()) {
            AchievementProgress progress = data.achievementProgress()
                    .computeIfAbsent(def.id(), AchievementProgress::new);

            if (progress.isCompleted() && !def.repeatable()) {
                continue;
            }

            double statValue = stats.get(def.condition().statKey());
            progress.setCurrentValue(statValue);

            if (def.condition().isMet(statValue) && (!progress.isCompleted() || def.repeatable())) {
                complete(player, data, def, progress);
            }
        }
    }

    private void complete(ServerPlayerEntity player, PlayerData data, AchievementDefinition def, AchievementProgress progress) {
        progress.markCompleted();
        data.statistics().increment("achievements_completed", 1);
        rewardManager.grantAll(player, def.rewardIds());
        onCompleted.accept(player, def);
    }

    public AchievementRegistry registry() {
        return registry;
    }
}
