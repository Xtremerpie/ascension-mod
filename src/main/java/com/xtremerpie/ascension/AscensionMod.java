package com.xtremerpie.ascension;

import com.xtremerpie.ascension.achievements.AchievementManager;
import com.xtremerpie.ascension.achievements.AchievementRegistry;
import com.xtremerpie.ascension.commands.AscensionCommands;
import com.xtremerpie.ascension.config.AscensionConfig;
import com.xtremerpie.ascension.core.AscensionConstants;
import com.xtremerpie.ascension.core.AscensionEvents;
import com.xtremerpie.ascension.network.AscensionNetworking;
import com.xtremerpie.ascension.persistence.PlayerDataManager;
import com.xtremerpie.ascension.progression.AscensionManager;
import com.xtremerpie.ascension.rewards.RewardManager;
import com.xtremerpie.ascension.statistics.StatisticsManager;
import com.xtremerpie.ascension.structures.BlueprintRegistry;
import com.xtremerpie.ascension.structures.StructureManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Common (client+server) entrypoint. Constructs and wires every
 * server-authoritative manager (spec section 27's "Core"/"AscensionMod")
 * and registers commands/networking/events. Contains no client-only
 * (rendering) imports — see {@link com.xtremerpie.ascension.client.AscensionClient}
 * for that side, kept strictly separate so a dedicated server never loads
 * client classes (spec section 7).
 */
public final class AscensionMod implements ModInitializer {

    private static AscensionMod instance;

    private final PlayerDataManager playerDataManager = new PlayerDataManager();
    private final AchievementRegistry achievementRegistry = new AchievementRegistry();
    private final BlueprintRegistry blueprintRegistry = new BlueprintRegistry();

    private AscensionManager ascensionManager;
    private RewardManager rewardManager;
    private AchievementManager achievementManager;
    private StatisticsManager statisticsManager;
    private StructureManager structureManager;

    @Override
    public void onInitialize() {
        instance = this;

        AscensionConfig.load(FabricLoader.getInstance().getConfigDir().resolve(AscensionConstants.MOD_ID + ".json"));

        ascensionManager = new AscensionManager(playerDataManager);
        rewardManager = new RewardManager(playerDataManager, ascensionManager);
        achievementManager = new AchievementManager(achievementRegistry, rewardManager);
        statisticsManager = new StatisticsManager(playerDataManager, achievementManager);
        structureManager = new StructureManager(blueprintRegistry, playerDataManager, statisticsManager);

        ClassLoader classLoader = AscensionMod.class.getClassLoader();
        achievementRegistry.loadAll(classLoader);
        rewardManager.loadAll(classLoader);
        blueprintRegistry.loadAll(classLoader);

        achievementManager.setOnCompleted((player, def) -> {
            AscensionNetworking.sendAchievementComplete(player, def.title(), def.description());
            syncStatus(player);
        });

        ascensionManager.setOnLevelUp((player, level) -> {
            AscensionNetworking.sendLevelUp(player, level,
                    com.xtremerpie.ascension.progression.AscensionLevel.milestoneUnlockedAt(level));
            syncStatus(player);
        });

        AscensionNetworking.registerCommon();
        AscensionEvents.register(playerDataManager, statisticsManager);
        AscensionCommands.register(playerDataManager, achievementManager, ascensionManager, rewardManager, structureManager);

        // Initial sync so AchievementScreen/BlueprintScreen have real data
        // as soon as a player joins, not only after their first achievement.
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> syncStatus(handler.getPlayer()));

        System.out.println("[Ascension] Loaded " + achievementRegistry.count() + " achievements, "
                + rewardManager.count() + " rewards, " + blueprintRegistry.all().size() + " blueprints.");
    }

    /** Pushes this player's current progress to their own client. Called after any change to it. */
    public void syncStatus(net.minecraft.server.network.ServerPlayerEntity player) {
        var data = playerDataManager.get(player);
        AscensionNetworking.sendPlayerStatus(player, com.xtremerpie.ascension.network.PlayerStatusDto.from(data).toJson());
    }

    public static AscensionMod get() {
        return instance;
    }

    public PlayerDataManager playerDataManager() {
        return playerDataManager;
    }

    public AchievementManager achievementManager() {
        return achievementManager;
    }

    public AscensionManager ascensionManager() {
        return ascensionManager;
    }

    public RewardManager rewardManager() {
        return rewardManager;
    }

    public StatisticsManager statisticsManager() {
        return statisticsManager;
    }

    public StructureManager structureManager() {
        return structureManager;
    }

    public BlueprintRegistry blueprintRegistry() {
        return blueprintRegistry;
    }
}
