package com.xtremerpie.ascension.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.xtremerpie.ascension.achievements.AchievementManager;
import com.xtremerpie.ascension.persistence.PlayerData;
import com.xtremerpie.ascension.persistence.PlayerDataManager;
import com.xtremerpie.ascension.progression.AscensionLevel;
import com.xtremerpie.ascension.progression.AscensionManager;
import com.xtremerpie.ascension.rewards.RewardManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Implements the /ascension command tree (spec section 25/35).
 * Read-only subcommands (status/achievements/stats) are open to any
 * player. Administrative subcommands (unlock/reward/level/reload) require
 * permission level 2 (the same level vanilla requires for /gamemode),
 * matching spec section 45: "protect admin commands... do not expose
 * administrative commands to normal players."
 */
public final class AscensionCommands {

    private static final int ADMIN_PERMISSION_LEVEL = 2;

    public static void register(PlayerDataManager playerDataManager, AchievementManager achievementManager,
                                 AscensionManager ascensionManager, RewardManager rewardManager,
                                 com.xtremerpie.ascension.structures.StructureManager structureManager) {
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                registerTree(dispatcher, playerDataManager, achievementManager, ascensionManager, rewardManager, structureManager));
    }

    private static int build(ServerCommandSource source, com.xtremerpie.ascension.structures.StructureManager structureManager, String blueprintId) {
        ServerPlayerEntity player = requirePlayer(source);
        if (player == null) return 0;
        var result = structureManager.build(player, blueprintId, player.getBlockPos());
        if (result.success()) {
            source.sendFeedback(() -> Text.literal("Built " + blueprintId + " (" + result.blocksPlaced() + " blocks)."), true);
            com.xtremerpie.ascension.AscensionMod.get().syncStatus(player);
            return 1;
        } else {
            source.sendError(Text.literal("Build failed: " + result.message()));
            return 0;
        }
    }

    private static void registerTree(CommandDispatcher<ServerCommandSource> dispatcher,
                                      PlayerDataManager playerDataManager, AchievementManager achievementManager,
                                      AscensionManager ascensionManager, RewardManager rewardManager,
                                      com.xtremerpie.ascension.structures.StructureManager structureManager) {
        dispatcher.register(CommandManager.literal("ascension")
                .executes(ctx -> status(ctx.getSource(), playerDataManager))
                .then(CommandManager.literal("status").executes(ctx -> status(ctx.getSource(), playerDataManager)))
                .then(CommandManager.literal("achievements").executes(ctx -> achievements(ctx.getSource(), playerDataManager, achievementManager)))
                .then(CommandManager.literal("stats").executes(ctx -> stats(ctx.getSource(), playerDataManager)))
                .then(CommandManager.literal("blueprints").executes(ctx -> blueprints(ctx.getSource(), playerDataManager)))
                .then(CommandManager.literal("debug").executes(ctx -> debug(ctx.getSource())))
                .then(CommandManager.literal("build")
                        .then(CommandManager.argument("blueprintId", StringArgumentType.word())
                                .executes(ctx -> build(ctx.getSource(), structureManager, StringArgumentType.getString(ctx, "blueprintId")))))
                .then(CommandManager.literal("reload")
                        .requires(src -> src.hasPermissionLevel(ADMIN_PERMISSION_LEVEL))
                        .executes(ctx -> reload(ctx.getSource(), achievementManager, rewardManager)))
                .then(CommandManager.literal("unlock")
                        .requires(src -> src.hasPermissionLevel(ADMIN_PERMISSION_LEVEL))
                        .then(CommandManager.argument("blueprintId", StringArgumentType.word())
                                .executes(ctx -> unlock(ctx.getSource(), playerDataManager, StringArgumentType.getString(ctx, "blueprintId")))))
                .then(CommandManager.literal("reward")
                        .requires(src -> src.hasPermissionLevel(ADMIN_PERMISSION_LEVEL))
                        .then(CommandManager.argument("rewardId", StringArgumentType.word())
                                .executes(ctx -> reward(ctx.getSource(), rewardManager, StringArgumentType.getString(ctx, "rewardId")))))
                .then(CommandManager.literal("level")
                        .requires(src -> src.hasPermissionLevel(ADMIN_PERMISSION_LEVEL))
                        .then(CommandManager.argument("value", IntegerArgumentType.integer(0, AscensionLevel.MAX_LEVEL))
                                .executes(ctx -> setLevel(ctx.getSource(), playerDataManager, ascensionManager, IntegerArgumentType.getInteger(ctx, "value")))))
        );
    }

    private static ServerPlayerEntity requirePlayer(ServerCommandSource source) {
        return source.getPlayer();
    }

    private static int status(ServerCommandSource source, PlayerDataManager playerDataManager) {
        ServerPlayerEntity player = requirePlayer(source);
        if (player == null) {
            source.sendError(Text.literal("This command must be run by a player."));
            return 0;
        }
        PlayerData data = playerDataManager.get(player);
        source.sendFeedback(() -> Text.literal(String.format(
                "Ascension Level %d (%.0f / %.0f XP) — %d achievements completed",
                data.ascensionLevel, AscensionLevel.xpIntoCurrentLevel(data.ascensionXp),
                AscensionLevel.xpNeededForNextLevel(data.ascensionXp),
                (int) data.statistics().get("achievements_completed"))), false);
        return 1;
    }

    private static int achievements(ServerCommandSource source, PlayerDataManager playerDataManager, AchievementManager achievementManager) {
        ServerPlayerEntity player = requirePlayer(source);
        if (player == null) return 0;
        PlayerData data = playerDataManager.get(player);
        long completed = data.achievementProgress().values().stream().filter(p -> p.isCompleted()).count();
        source.sendFeedback(() -> Text.literal(completed + " / " + achievementManager.registry().count() + " achievements completed."), false);
        return 1;
    }

    private static int stats(ServerCommandSource source, PlayerDataManager playerDataManager) {
        ServerPlayerEntity player = requirePlayer(source);
        if (player == null) return 0;
        PlayerData data = playerDataManager.get(player);
        data.statistics().asMap().forEach((key, value) ->
                source.sendFeedback(() -> Text.literal(key + ": " + value), false));
        return 1;
    }

    private static int blueprints(ServerCommandSource source, PlayerDataManager playerDataManager) {
        ServerPlayerEntity player = requirePlayer(source);
        if (player == null) return 0;
        PlayerData data = playerDataManager.get(player);
        List<String> unlocked = data.unlockedBlueprintIds().stream().toList();
        source.sendFeedback(() -> Text.literal("Unlocked blueprints: " + (unlocked.isEmpty() ? "none yet" : String.join(", ", unlocked))), false);
        return 1;
    }

    private static int debug(ServerCommandSource source) {
        com.xtremerpie.ascension.config.AscensionConfig config = com.xtremerpie.ascension.config.AscensionConfig.get();
        config.debugMode = !config.debugMode;
        com.xtremerpie.ascension.config.AscensionConfig.save();
        source.sendFeedback(() -> Text.literal("Ascension debug mode: " + (config.debugMode ? "ON" : "OFF")), false);
        return 1;
    }

    private static int reload(ServerCommandSource source, AchievementManager achievementManager, RewardManager rewardManager) {
        ClassLoader cl = AscensionCommands.class.getClassLoader();
        achievementManager.registry().loadAll(cl);
        rewardManager.loadAll(cl);
        source.sendFeedback(() -> Text.literal("Reloaded " + achievementManager.registry().count() + " achievements and "
                + rewardManager.count() + " rewards from data files."), true);
        return 1;
    }

    private static int unlock(ServerCommandSource source, PlayerDataManager playerDataManager, String blueprintId) {
        ServerPlayerEntity player = requirePlayer(source);
        if (player == null) return 0;
        playerDataManager.get(player).unlockedBlueprintIds().add(blueprintId);
        source.sendFeedback(() -> Text.literal("Unlocked blueprint: " + blueprintId), true);
        com.xtremerpie.ascension.AscensionMod.get().syncStatus(player);
        return 1;
    }

    private static int reward(ServerCommandSource source, RewardManager rewardManager, String rewardId) {
        ServerPlayerEntity player = requirePlayer(source);
        if (player == null) return 0;
        rewardManager.grantAll(player, List.of(rewardId));
        source.sendFeedback(() -> Text.literal("Granted reward: " + rewardId), true);
        com.xtremerpie.ascension.AscensionMod.get().syncStatus(player);
        return 1;
    }

    private static int setLevel(ServerCommandSource source, PlayerDataManager playerDataManager, AscensionManager ascensionManager, int value) {
        ServerPlayerEntity player = requirePlayer(source);
        if (player == null) return 0;
        PlayerData data = playerDataManager.get(player);
        double targetXp = AscensionLevel.xpRequiredForLevel(value);
        double delta = targetXp - data.ascensionXp;
        ascensionManager.grantXp(player, Math.max(0, delta));
        data.ascensionLevel = value;
        source.sendFeedback(() -> Text.literal("Set Ascension level to " + value), true);
        com.xtremerpie.ascension.AscensionMod.get().syncStatus(player);
        return 1;
    }
}
