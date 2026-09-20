package com.xtremerpie.ascension.rewards;

import net.minecraft.server.network.ServerPlayerEntity;

/** Grants vanilla Minecraft experience points via the real XP API. */
public final class ExperienceReward implements Reward {

    private final String id;
    private final int amount;

    public ExperienceReward(String id, int amount) {
        this.id = id;
        this.amount = amount;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public RewardResult apply(ServerPlayerEntity player) {
        player.addExperience(amount);
        return RewardResult.ok("+" + amount + " XP");
    }
}
