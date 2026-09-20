package com.xtremerpie.ascension.hud.modules;

import com.xtremerpie.ascension.config.AscensionConfig;
import com.xtremerpie.ascension.hud.HudContext;
import com.xtremerpie.ascension.hud.HudModule;
import com.xtremerpie.ascension.hud.HudRenderer;
import com.xtremerpie.ascension.util.MathUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Shows 3D/horizontal/vertical distance and direction to the nearest other
 * visible player. Contextual: only appears when another player is loaded
 * nearby, otherwise stays hidden (spec section 3/6).
 */
public final class DistanceHudModule implements HudModule {

    private final HudRenderer renderer;
    private AbstractClientPlayerEntity nearest;
    private double distance, horizontal, vertical, bearing;

    public DistanceHudModule(HudRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public String id() {
        return "distance";
    }

    @Override
    public boolean isEnabled(HudContext context) {
        return AscensionConfig.get().distanceModuleEnabled;
    }

    @Override
    public boolean shouldDisplay(HudContext context) {
        return nearest != null;
    }

    @Override
    public void update(HudContext context) {
        var client = context.client();
        nearest = null;
        if (client.player == null || client.world == null) return;

        Vec3d selfPos = client.player.getPos();
        double best = Double.MAX_VALUE;
        int range = AscensionConfig.get().entityScanRangeBlocks;

        for (AbstractClientPlayerEntity other : client.world.getPlayers()) {
            if (other == client.player) continue;
            double d = other.getPos().distanceTo(selfPos);
            if (d < best && d <= range) {
                best = d;
                nearest = other;
            }
        }

        if (nearest != null) {
            Vec3d otherPos = nearest.getPos();
            distance = MathUtil.distance3D(selfPos, otherPos);
            horizontal = MathUtil.distanceHorizontal(selfPos, otherPos);
            vertical = MathUtil.verticalDifference(selfPos, otherPos);
            bearing = MathUtil.bearingDegrees(selfPos, otherPos);
        }
    }

    @Override
    public void render(DrawContext ctx, HudContext context, int screenWidth, int screenHeight, int anchorY) {
        var font = context.client().textRenderer;
        int width = 150;
        int x = screenWidth - width - 8;

        renderer.drawPanel(ctx, x, anchorY, width, preferredHeight());
        renderer.drawHeading(ctx, font, "Nearest Player", x, anchorY);

        int y = anchorY + 16;
        renderer.drawLine(ctx, font, "Name:", nearest.getGameProfile().getName(), x, y); y += 10;
        renderer.drawLine(ctx, font, "Distance:", MathUtil.round(distance, AscensionConfig.get().decimalPrecision) + " m", x, y); y += 10;
        renderer.drawLine(ctx, font, "Horizontal:", MathUtil.round(horizontal, AscensionConfig.get().decimalPrecision) + " m", x, y); y += 10;
        renderer.drawLine(ctx, font, "Vertical:", (vertical >= 0 ? "+" : "") + MathUtil.round(vertical, AscensionConfig.get().decimalPrecision) + " m", x, y); y += 10;
        renderer.drawLine(ctx, font, "Direction:", MathUtil.compassLabel(bearing), x, y);
    }

    @Override
    public int preferredHeight() {
        return 66;
    }
}
