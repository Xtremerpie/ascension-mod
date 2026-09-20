package com.xtremerpie.ascension.targeting;

import net.minecraft.client.MinecraftClient;

/**
 * Owns the client-side targeting state for the current tick. HUD modules
 * call {@link #currentTarget()}; {@link #tick()} refreshes it once per
 * game tick (called from the client tick event in AscensionClient), so
 * every HUD module sharing a tick sees a consistent target rather than
 * each re-raycasting independently.
 */
public final class TargetManager {

    private final RaycastService raycastService = new RaycastService();
    private final EntityTargeting entityTargeting = new EntityTargeting();

    private TargetSnapshot currentTarget = TargetSnapshot.none();

    public void tick(MinecraftClient client) {
        if (client.player == null) {
            currentTarget = TargetSnapshot.none();
            return;
        }

        var hit = raycastService.currentHitResult(client);
        if (raycastService.isTargetingEntity(hit)) {
            currentTarget = entityTargeting.buildSnapshot(client.player, hit);
        } else {
            currentTarget = TargetSnapshot.none();
        }
    }

    public TargetSnapshot currentTarget() {
        return currentTarget;
    }
}
