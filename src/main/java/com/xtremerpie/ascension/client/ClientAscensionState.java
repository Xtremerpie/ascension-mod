package com.xtremerpie.ascension.client;

import com.xtremerpie.ascension.network.PlayerStatusDto;

/**
 * Client-side singleton holding the most recently received
 * {@link PlayerStatusDto}. Populated by the network receiver registered
 * in {@link AscensionClient}; read by AchievementScreen/BlueprintScreen/
 * StatusScreen. Starts as an empty DTO (level 0, nothing unlocked) rather
 * than null, so screens can render immediately even before the first sync
 * packet arrives after joining.
 */
public final class ClientAscensionState {

    private static volatile PlayerStatusDto latest = new PlayerStatusDto();

    private ClientAscensionState() {
    }

    public static void update(PlayerStatusDto dto) {
        latest = dto;
    }

    public static PlayerStatusDto current() {
        return latest;
    }
}
