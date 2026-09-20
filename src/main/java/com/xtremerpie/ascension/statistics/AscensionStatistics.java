package com.xtremerpie.ascension.statistics;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Flat, persisted per-player statistic map. Deliberately a simple
 * String→double map (rather than dozens of typed fields) so
 * {@link com.xtremerpie.ascension.achievements.AchievementCondition} can
 * reference any stat by key without a matching Java getter needing to
 * exist for every future stat — new stats/achievements only require a new
 * JSON file plus one {@code increment}/{@code setIfGreater} call site.
 *
 * Known keys used by the bundled achievements (spec section 32/33):
 * distance_travelled_blocks, time_played_ticks, entities_observed,
 * projectiles_fired, projectiles_hit, longest_projectile_hit_blocks,
 * highest_velocity_blocks_per_second, highest_altitude_y,
 * depth_below_sea_level_blocks, structures_built, blocks_placed,
 * mobs_killed, achievements_completed, ascension_level,
 * ascension_xp_earned.
 */
public final class AscensionStatistics {

    private final Map<String, Double> values = new LinkedHashMap<>();

    public double get(String key) {
        return values.getOrDefault(key, 0.0);
    }

    public void set(String key, double value) {
        values.put(key, value);
    }

    public void increment(String key, double amount) {
        values.merge(key, amount, Double::sum);
    }

    /** Sets the value only if it's a new maximum — for "highest X" / "longest Y" stats. */
    public void setIfGreater(String key, double candidate) {
        if (candidate > get(key)) {
            values.put(key, candidate);
        }
    }

    public Map<String, Double> asMap() {
        return values;
    }

    public void loadFrom(Map<String, Double> source) {
        values.clear();
        if (source != null) values.putAll(source);
    }
}
