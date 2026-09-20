package com.xtremerpie.ascension.structures;

import com.xtremerpie.ascension.persistence.PlayerData;
import com.xtremerpie.ascension.persistence.PlayerDataManager;
import com.xtremerpie.ascension.statistics.StatisticsManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Server-authoritative structure build flow: unlock check → area/world
 * validation (delegated to StructureGenerator) → placement → statistics
 * update. Never trusts the client with placement directly (spec 24/45).
 */
public final class StructureManager {

    private final BlueprintRegistry blueprintRegistry;
    private final StructureGenerator generator = new StructureGenerator();
    private final PlayerDataManager playerDataManager;
    private final StatisticsManager statisticsManager;

    public StructureManager(BlueprintRegistry blueprintRegistry, PlayerDataManager playerDataManager, StatisticsManager statisticsManager) {
        this.blueprintRegistry = blueprintRegistry;
        this.playerDataManager = playerDataManager;
        this.statisticsManager = statisticsManager;
    }

    public StructurePlacementResult build(ServerPlayerEntity player, String blueprintId, BlockPos origin) {
        PlayerData data = playerDataManager.get(player);
        if (!data.unlockedBlueprintIds().contains(blueprintId)) {
            return StructurePlacementResult.failed("Blueprint not unlocked: " + blueprintId);
        }

        var blueprintOpt = blueprintRegistry.get(blueprintId);
        if (blueprintOpt.isEmpty()) {
            return StructurePlacementResult.failed("Unknown blueprint id: " + blueprintId);
        }

        StructurePlacementResult result = generator.generate((ServerWorld) player.getWorld(), blueprintId, blueprintOpt.get(), origin);
        if (result.success()) {
            statisticsManager.onStructureBuilt(player);
        }
        return result;
    }

    public BlueprintRegistry blueprints() {
        return blueprintRegistry;
    }
}
