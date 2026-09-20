package com.xtremerpie.ascension.hud.modules;

import com.xtremerpie.ascension.config.AscensionConfig;
import com.xtremerpie.ascension.hud.HudContext;
import com.xtremerpie.ascension.hud.HudModule;
import com.xtremerpie.ascension.hud.HudRenderer;
import com.xtremerpie.ascension.physics.ProjectilePhysics;
import com.xtremerpie.ascension.util.MathUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;

public final class ProjectileHudModule implements HudModule {

    private final HudRenderer renderer;
    private ProjectileEntity tracked;
    private ProjectilePhysics.ProjectileAnalysis analysis;

    public ProjectileHudModule(HudRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public String id() {
        return "projectile";
    }

    @Override
    public boolean isEnabled(HudContext context) {
        return AscensionConfig.get().projectileModuleEnabled;
    }

    @Override
    public boolean shouldDisplay(HudContext context) {
        return tracked != null;
    }

    @Override
    public void update(HudContext context) {
        var client = context.client();
        tracked = null;
        if (client.player == null || client.world == null) return;

        // Find the nearest of the player's own in-flight arrows within
        // scan range — bounded search, not a world-wide scan (spec 12).
        int range = AscensionConfig.get().entityScanRangeBlocks;
        var box = client.player.getBoundingBox().expand(range);
        double best = Double.MAX_VALUE;

        for (var entity : client.world.getEntitiesByClass(PersistentProjectileEntity.class, box, e -> true)) {
            if (entity.getOwner() != client.player) continue;
            double d = entity.getPos().distanceTo(client.player.getPos());
            if (d < best) {
                best = d;
                tracked = entity;
            }
        }

        if (tracked != null) {
            analysis = context.physics().projectile().analyze(tracked);
        }
    }

    @Override
    public void render(DrawContext ctx, HudContext context, int screenWidth, int screenHeight, int anchorY) {
        var font = context.client().textRenderer;
        int width = 160;
        int x = 8;
        renderer.drawPanel(ctx, x, anchorY, width, preferredHeight());
        renderer.drawHeading(ctx, font, "Projectile", x, anchorY);

        int p = AscensionConfig.get().decimalPrecision;
        int y = anchorY + 16;
        renderer.drawLine(ctx, font, "Speed:", MathUtil.round(analysis.currentSpeedBlocksPerSecond(), p) + " b/s", x, y); y += 10;
        renderer.drawLine(ctx, font, "Distance:", MathUtil.round(analysis.distanceTravelledBlocks(), p) + " blocks", x, y); y += 10;
        renderer.drawLine(ctx, font, "Flight time:", MathUtil.round(analysis.flightTimeSeconds(), 1) + " s", x, y); y += 10;
        renderer.drawLine(ctx, font, "Landing:", "(ESTIMATE)", x, y);
    }

    @Override
    public int preferredHeight() {
        return 56;
    }
}
