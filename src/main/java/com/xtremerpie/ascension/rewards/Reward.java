package com.xtremerpie.ascension.rewards;

import net.minecraft.server.network.ServerPlayerEntity;

/** A single executable, server-authoritative reward action. */
public interface Reward {

    String id();

    RewardResult apply(ServerPlayerEntity player);
}
