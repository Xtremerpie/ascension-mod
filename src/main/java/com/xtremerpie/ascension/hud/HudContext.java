package com.xtremerpie.ascension.hud;

import com.xtremerpie.ascension.physics.PhysicsCalculator;
import com.xtremerpie.ascension.targeting.TargetManager;
import net.minecraft.client.MinecraftClient;

/**
 * Everything a {@link HudModule} needs to decide whether/what to render,
 * bundled once per frame instead of each module reaching into globals
 * separately. Keeps modules easy to unit-reason-about and easy to disable
 * independently.
 */
public record HudContext(
        MinecraftClient client,
        PhysicsCalculator physics,
        TargetManager targeting,
        HudState state,
        boolean godModeActive
) {
}
