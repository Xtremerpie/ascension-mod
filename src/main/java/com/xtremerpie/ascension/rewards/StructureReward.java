package com.xtremerpie.ascension.rewards;

import com.xtremerpie.ascension.persistence.PlayerDataManager;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Unlocks a blueprint for later, player-initiated construction via
 * {@code /ascension blueprint} or the Blueprint screen — this reward never
 * places blocks itself (spec section 18: unlocking and building are
 * separate steps).
 */
public final class StructureReward implements Reward {

    private final String id;
    private final String blueprintId;
    private final PlayerDataManager playerDataManager;

    public StructureReward(String id, String blueprintId, PlayerDataManager playerDataManager) {
        this.id = id;
        this.blueprintId = blueprintId;
        this.playerDataManager = playerDataManager;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public RewardResult apply(ServerPlayerEntity player) {
        playerDataManager.get(player).unlockedBlueprintIds().add(blueprintId);
        return RewardResult.ok("Blueprint unlocked: " + blueprintId);
    }
}
