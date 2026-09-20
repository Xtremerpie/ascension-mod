package com.xtremerpie.ascension.targeting;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * Client-side only. Reuses Minecraft's own crosshair raycast
 * ({@code MinecraftClient#crosshairTarget}), which the client already
 * computes every frame for block breaking / entity interaction, instead of
 * firing an extra raycast of our own. This is deliberate for the
 * performance requirements in spec section 12 ("avoid excessive
 * raycasts") — ASCENSION adds zero raycasts here, it just reads a result
 * that already exists.
 */
public final class RaycastService {

    public HitResult currentHitResult(MinecraftClient client) {
        return client.crosshairTarget;
    }

    public boolean isTargetingEntity(HitResult hit) {
        return hit instanceof EntityHitResult;
    }

    public boolean isTargetingBlock(HitResult hit) {
        return hit instanceof BlockHitResult && hit.getType() == HitResult.Type.BLOCK;
    }
}
