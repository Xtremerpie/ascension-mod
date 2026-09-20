package com.xtremerpie.ascension.achievements;

import java.util.List;

public record AchievementDefinition(
        String id,
        String title,
        String description,
        AchievementCategory category,
        AchievementCondition condition,
        List<String> rewardIds,
        boolean repeatable
) {
}
