package com.xtremerpie.ascension.hud.modules;

import com.xtremerpie.ascension.config.AscensionConfig;
import com.xtremerpie.ascension.hud.HudContext;
import com.xtremerpie.ascension.hud.HudModule;
import com.xtremerpie.ascension.hud.HudRenderer;
import com.xtremerpie.ascension.physics.MovementState;
import com.xtremerpie.ascension.physics.PhysicsSnapshot;
import com.xtremerpie.ascension.util.MathUtil;
import net.minecraft.client.gui.DrawContext;

/**
 * Fall-specific readout (spec section 3: "if the player is falling, show
 * fall/velocity information"). Distinct from MovementHudModule because it
 * only activates for the falling case and adds an estimated impact
 * velocity, clearly labelled ESTIMATE since Minecraft doesn't expose a
 * precomputed landing height to the client ahead of time.
 */
public final class PhysicsHudModule implements HudModule {

    private final HudRenderer renderer;
    private PhysicsSnapshot snapshot = PhysicsSnapshot.idle();
    private double startFallHeight = Double.NaN;

    public PhysicsHudModule(HudRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public String id() {
        return "physics";
    }

    @Override
    public boolean isEnabled(HudContext context) {
        return AscensionConfig.get().physicsModuleEnabled;
    }

    @Override
    public boolean shouldDisplay(HudContext context) {
        return snapshot.state() == MovementState.FALLING;
    }

    @Override
    public void update(HudContext context) {
        if (context.client().player == null) return;
        snapshot = context.physics().movement().analyze(context.client().player);

        if (snapshot.state() == MovementState.FALLING) {
            if (Double.isNaN(startFallHeight)) {
                startFallHeight = context.client().player.getPos().y;
            }
        } else {
            startFallHeight = Double.NaN;
        }
    }

    @Override
    public void render(DrawContext ctx, HudContext context, int screenWidth, int screenHeight, int anchorY) {
        var font = context.client().textRenderer;
        int width = 150;
        int x = 8;
        renderer.drawPanel(ctx, x, anchorY, width, preferredHeight());
        renderer.drawHeading(ctx, font, "Fall", x, anchorY);

        double currentY = context.client().player.getPos().y;
        double fallenSoFar = Double.isNaN(startFallHeight) ? 0 : startFallHeight - currentY;
        int p = AscensionConfig.get().decimalPrecision;

        int y = anchorY + 16;
        renderer.drawLine(ctx, font, "Fallen:", MathUtil.round(Math.max(0, fallenSoFar), p) + " m", x, y); y += 10;
        renderer.drawLine(ctx, font, "Velocity:", MathUtil.round(Math.abs(snapshot.verticalSpeedBlocksPerSecond()), p) + " b/s", x, y); y += 10;
        renderer.drawLine(ctx, font, "Est. impact:", estimateImpact(fallenSoFar) + " (ESTIMATE)", x, y);
    }

    private String estimateImpact(double fallenSoFar) {
        // Simple fall-damage-relevant threshold, purely informational —
        // not an exact Minecraft damage formula, and labelled as such.
        if (fallenSoFar < 3) return "safe";
        if (fallenSoFar < 8) return "minor";
        if (fallenSoFar < 15) return "moderate";
        return "severe";
    }

    @Override
    public int preferredHeight() {
        return 56;
    }
}
