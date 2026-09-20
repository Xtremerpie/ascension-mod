package com.xtremerpie.ascension.network;

import com.xtremerpie.ascension.core.AscensionConstants;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Server-to-client payloads carrying notification data. Uses the
 * CustomPayload/PacketCodec networking API introduced around Minecraft
 * 1.20.5. CONFIDENCE NOTE: this is the part of the codebase with the
 * highest risk of a signature mismatch against the exact 1.21.11 API
 * surface, since it could not be compiled/verified against the real game
 * jar in this environment (see IMPLEMENTATION_STATUS.md). If the build
 * fails here first, check the current {@code PacketCodec}/{@code
 * CustomPayload} javadoc for 1.21.11 and adjust these two records —
 * nothing else in the mod depends on the exact codec shape.
 */
public final class NetworkPackets {

    private NetworkPackets() {
    }

    public record AchievementCompletePayload(String title, String description) implements CustomPayload {
        public static final CustomPayload.Id<AchievementCompletePayload> ID =
                new CustomPayload.Id<>(Identifier.of(AscensionConstants.MOD_ID, "achievement_complete"));

        public static final PacketCodec<RegistryByteBuf, AchievementCompletePayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, AchievementCompletePayload::title,
                PacketCodecs.STRING, AchievementCompletePayload::description,
                AchievementCompletePayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record LevelUpPayload(int newLevel, String milestoneUnlocked) implements CustomPayload {
        public static final CustomPayload.Id<LevelUpPayload> ID =
                new CustomPayload.Id<>(Identifier.of(AscensionConstants.MOD_ID, "level_up"));

        public static final PacketCodec<RegistryByteBuf, LevelUpPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.INTEGER, LevelUpPayload::newLevel,
                PacketCodecs.STRING, p -> p.milestoneUnlocked() == null ? "" : p.milestoneUnlocked(),
                LevelUpPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /**
     * Carries the requesting player's own progress data (level/XP,
     * per-achievement progress, unlocked blueprints) so AchievementScreen
     * and BlueprintScreen can render real data instead of nothing.
     * Deliberately encoded as one JSON string via the same STRING codec
     * pattern used above, rather than a structured Map codec — lowest
     * risk given neither could be compiled/verified against the real
     * 1.21.11 API offline: a wrong PacketCodec.tuple arity is a compile
     * error, a wrong JSON shape is not.
     */
    public record PlayerStatusPayload(String statusJson) implements CustomPayload {
        public static final CustomPayload.Id<PlayerStatusPayload> ID =
                new CustomPayload.Id<>(Identifier.of(AscensionConstants.MOD_ID, "player_status"));

        public static final PacketCodec<RegistryByteBuf, PlayerStatusPayload> CODEC =
                PacketCodec.tuple(PacketCodecs.STRING, PlayerStatusPayload::statusJson, PlayerStatusPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
