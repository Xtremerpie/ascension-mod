package com.xtremerpie.ascension.network;

import com.google.gson.Gson;
import com.xtremerpie.ascension.persistence.PlayerData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Flat, JSON-serializable snapshot of one player's ASCENSION progress,
 * sent server->client so AchievementScreen/BlueprintScreen/StatusScreen
 * have real data to render instead of nothing. Built fresh from
 * {@link PlayerData} whenever something changes (see call sites in
 * AscensionMod) rather than kept as a standing subscription — this data
 * is small (a few dozen doubles/booleans) so resending the whole snapshot
 * on each relevant change is simpler and safer than diffing it.
 */
public final class PlayerStatusDto {

    private static final Gson GSON = new Gson();

    public int ascensionLevel;
    public double ascensionXp;
    public Map<String, Double> achievementProgress = new HashMap<>();
    public Set<String> completedAchievements = new HashSet<>();
    public Set<String> unlockedBlueprintIds = new HashSet<>();

    public static PlayerStatusDto from(PlayerData data) {
        PlayerStatusDto dto = new PlayerStatusDto();
        dto.ascensionLevel = data.ascensionLevel;
        dto.ascensionXp = data.ascensionXp;
        data.achievementProgress().forEach((id, progress) -> {
            dto.achievementProgress.put(id, progress.currentValue());
            if (progress.isCompleted()) {
                dto.completedAchievements.add(id);
            }
        });
        dto.unlockedBlueprintIds.addAll(data.unlockedBlueprintIds());
        return dto;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static PlayerStatusDto fromJson(String json) {
        PlayerStatusDto dto = GSON.fromJson(json, PlayerStatusDto.class);
        return dto != null ? dto : new PlayerStatusDto();
    }
}
