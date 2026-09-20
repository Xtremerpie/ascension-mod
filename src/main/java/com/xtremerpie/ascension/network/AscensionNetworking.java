package com.xtremerpie.ascension.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class AscensionNetworking {

    private AscensionNetworking() {
    }

    /** Call once from common mod init — registers both S2C payload types. */
    public static void registerCommon() {
        PayloadTypeRegistry.playS2C().register(NetworkPackets.AchievementCompletePayload.ID, NetworkPackets.AchievementCompletePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkPackets.LevelUpPayload.ID, NetworkPackets.LevelUpPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NetworkPackets.PlayerStatusPayload.ID, NetworkPackets.PlayerStatusPayload.CODEC);
    }

    public static void sendPlayerStatus(ServerPlayerEntity player, String statusJson) {
        ServerPlayNetworking.send(player, new NetworkPackets.PlayerStatusPayload(statusJson));
    }

    public static void sendAchievementComplete(ServerPlayerEntity player, String title, String description) {
        ServerPlayNetworking.send(player, new NetworkPackets.AchievementCompletePayload(title, description));
    }

    public static void sendLevelUp(ServerPlayerEntity player, int newLevel, String milestone) {
        ServerPlayNetworking.send(player, new NetworkPackets.LevelUpPayload(newLevel, milestone));
    }
}
