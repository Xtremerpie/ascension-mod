package com.xtremerpie.ascension.progression;

import com.xtremerpie.ascension.persistence.PlayerData;
import com.xtremerpie.ascension.persistence.PlayerDataManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.BiConsumer;

/**
 * Server-authoritative Ascension XP/level tracking. Progression is
 * intentionally NOT required for vanilla survival (spec 22: "do not make
 * progression mandatory") — nothing here blocks normal gameplay, it only
 * unlocks optional HUD/structure features per {@link AscensionLevel}.
 */
public final class AscensionManager {

    private final PlayerDataManager playerDataManager;
    private BiConsumer<ServerPlayerEntity, Integer> onLevelUp = (p, lvl) -> {};

    public AscensionManager(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public void setOnLevelUp(BiConsumer<ServerPlayerEntity, Integer> callback) {
        this.onLevelUp = callback;
    }

    public void grantXp(ServerPlayerEntity player, double amount) {
        if (amount <= 0) return;
        PlayerData data = playerDataManager.get(player);
        int levelBefore = AscensionLevel.levelForXp(data.ascensionXp);

        data.ascensionXp += amount;
        data.statistics().increment("ascension_xp_earned", amount);

        int levelAfter = AscensionLevel.levelForXp(data.ascensionXp);
        if (levelAfter > levelBefore) {
            data.ascensionLevel = levelAfter;
            data.statistics().set("ascension_level", levelAfter);
            onLevelUp.accept(player, levelAfter);
        }
    }

    public boolean hasUnlocked(PlayerData data, int requiredLevel) {
        return data.ascensionLevel >= requiredLevel;
    }
}
