package com.xtremerpie.ascension.persistence;

import com.xtremerpie.ascension.achievements.AchievementProgress;
import com.xtremerpie.ascension.statistics.AscensionStatistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Everything ASCENSION persists for one player: statistics, achievement
 * progress, Ascension level/XP, and unlocked blueprint ids. One instance
 * per player, round-tripped to disk by {@link PlayerDataManager}.
 */
public final class PlayerData {

    public UUID playerId;

    private final AscensionStatistics statistics = new AscensionStatistics();
    private final Map<String, AchievementProgress> achievementProgress = new HashMap<>();
    private final Set<String> unlockedBlueprintIds = new HashSet<>();

    public int ascensionLevel = 0;
    public double ascensionXp = 0;

    public PlayerData() {
    }

    public PlayerData(UUID playerId) {
        this.playerId = playerId;
    }

    public AscensionStatistics statistics() {
        return statistics;
    }

    public Map<String, AchievementProgress> achievementProgress() {
        return achievementProgress;
    }

    public Set<String> unlockedBlueprintIds() {
        return unlockedBlueprintIds;
    }
}
