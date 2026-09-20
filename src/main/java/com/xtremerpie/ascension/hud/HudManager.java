package com.xtremerpie.ascension.hud;

import com.xtremerpie.ascension.config.AscensionConfig;
import com.xtremerpie.ascension.hud.modules.*;
import com.xtremerpie.ascension.physics.PhysicsCalculator;
import com.xtremerpie.ascension.targeting.TargetManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

/**
 * Owns the ordered list of HUD modules, drives their update() calls at
 * each module's configured interval, and lays them out as a vertically
 * stacked column in the top-left of the screen (contextual modules only
 * appear when {@link HudModule#shouldDisplay} is true, so the stack
 * naturally grows/shrinks with what's relevant — spec section 3).
 */
public final class HudManager {

    private final HudState state = new HudState();
    private final HudRenderer renderer = new HudRenderer();
    private final PhysicsCalculator physics = new PhysicsCalculator();
    private final TargetManager targeting = new TargetManager();

    private final List<HudModule> modules;

    private int tickCounter = 0;

    public HudManager() {
        this.modules = List.of(
                new TargetHudModule(renderer),
                new DistanceHudModule(renderer),
                new MovementHudModule(renderer),
                new PhysicsHudModule(renderer),
                new ProjectileHudModule(renderer),
                new WorldHudModule(renderer),
                new AchievementHudModule(renderer),
                new ProgressionHudModule(renderer),
                new NotificationHudModule(renderer)
        );
    }

    public HudState state() {
        return state;
    }

    public void tick(MinecraftClient client) {
        tickCounter++;
        targeting.tick(client);

        AscensionConfig config = AscensionConfig.get();
        HudContext ctx = new HudContext(client, physics, targeting, state, state.isGodModeActive());

        for (HudModule module : modules) {
            if (!module.isEnabled(ctx)) continue;
            int interval = config.updateIntervalTicksFor(module.id());
            if (tickCounter % Math.max(1, interval) == 0) {
                module.update(ctx);
            }
        }
    }

    public void render(DrawContext drawContext, MinecraftClient client) {
        if (!state.isHudVisible() || client.player == null) return;

        AscensionConfig config = AscensionConfig.get();
        if (!config.hudEnabled()) return;

        HudContext ctx = new HudContext(client, physics, targeting, state, state.isGodModeActive());

        int screenWidth = drawContext.getScaledWindowWidth();
        int screenHeight = drawContext.getScaledWindowHeight();

        int y = 8;
        for (HudModule module : modules) {
            if (!module.isEnabled(ctx) || !module.shouldDisplay(ctx)) continue;
            module.render(drawContext, ctx, screenWidth, screenHeight, y);
            y += module.preferredHeight() + 4;
        }
    }
}
