package com.xtremerpie.ascension.rewards;

import com.xtremerpie.ascension.progression.AscensionManager;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ProgressionReward implements Reward {

    private final String id;
    private final double ascensionXpAmount;
    private final AscensionManager ascensionManager;

    public ProgressionReward(String id, double ascensionXpAmount, AscensionManager ascensionManager) {
        this.id = id;
        this.ascensionXpAmount = ascensionXpAmount;
        this.ascensionManager = ascensionManager;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public RewardResult apply(ServerPlayerEntity player) {
        ascensionManager.grantXp(player, ascensionXpAmount);
        return RewardResult.ok("+" + (int) ascensionXpAmount + " Ascension XP");
    }
}
