package com.xtremerpie.ascension.achievements;

/**
 * A single data-driven completion condition: "statistic X reaches
 * threshold Y". Deliberately not a scripting/expression language — every
 * condition maps directly onto one of the stat keys tracked by
 * {@code AscensionStatistics}, which keeps achievement JSON simple to
 * author and keeps the check itself O(1) per stat update rather than
 * needing a rules engine.
 *
 * @param statKey        one of AscensionStatistics' stat keys, e.g. "distance_travelled_blocks"
 * @param thresholdValue the value statKey must reach (>=) to complete
 */
public record AchievementCondition(String statKey, double thresholdValue) {

    public boolean isMet(double currentStatValue) {
        return currentStatValue >= thresholdValue;
    }

    public double progressFraction(double currentStatValue) {
        if (thresholdValue <= 0) return 1.0;
        return Math.min(1.0, Math.max(0.0, currentStatValue / thresholdValue));
    }
}
