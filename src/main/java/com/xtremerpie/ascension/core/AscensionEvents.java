package com.xtremerpie.ascension.core;

import com.xtremerpie.ascension.persistence.PlayerDataManager;
import com.xtremerpie.ascension.statistics.StatisticsManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Registers ASCENSION's server-side event hooks against real, stable
 * Fabric API events.
 *
 * HONEST SCOPE NOTE: two hooks below (arrow-fired/arrow-hit distance, and
 * manual block placement for the Building category) are NOT wired here.
 * Fabric API does not ship a simple, long-stable "block placed by player"
 * or "projectile fired/landed" event equivalent to
 * {@code PlayerBlockBreakEvents} for breaking — reliably capturing those
 * needs either a Mixin into the relevant vanilla methods (whose exact
 * 1.21.11 method names/signatures could not be verified against the real
 * game jar in this environment — see IMPLEMENTATION_STATUS.md) or a
 * specific Fabric API event that should be confirmed against the current
 * Fabric API javadocs for 1.21.11 before wiring. {@link StatisticsManager}
 * already exposes {@code onArrowFired}, {@code onArrowHit}, and
 * {@code onBlockPlaced} as ready-to-call public methods — connecting them
 * is a local follow-up, not a redesign.
 */
public final class AscensionEvents {

    private AscensionEvents() {
    }

    public static void register(PlayerDataManager playerDataManager, StatisticsManager statisticsManager) {
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                statisticsManager.onPlayerTick(player);
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof HostileEntity) {
                ServerPlayerEntity killer = attackerAsPlayer(damageSource);
                if (killer != null) {
                    statisticsManager.onMobKilled(killer);
                }
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(playerDataManager::init);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> playerDataManager.saveAll());
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> playerDataManager.saveAndUnload(handler.getPlayer()));
    }

    private static ServerPlayerEntity attackerAsPlayer(DamageSource source) {
        return source.getAttacker() instanceof ServerPlayerEntity player ? player : null;
    }
}
