package com.xtremerpie.ascension.hud.modules;

import com.xtremerpie.ascension.config.AscensionConfig;
import com.xtremerpie.ascension.hud.HudContext;
import com.xtremerpie.ascension.hud.HudModule;
import com.xtremerpie.ascension.hud.HudRenderer;
import com.xtremerpie.ascension.physics.MovementState;
import com.xtremerpie.ascension.physics.PhysicsSnapshot;
import com.xtremerpie.ascension.util.MathUtil;
import net.minecraft.client.gui.DrawContext;

public final class MovementHudModule implements HudModule {

    private final HudRenderer renderer;
    private PhysicsSnapshot snapshot = PhysicsSnapshot.idle();

    public MovementHudModule(HudRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public String id() {
        return "movement";
    }

    @Override
    public boolean isEnabled(HudContext context) {
        return AscensionConfig.get().movementModuleEnabled;
    }

    @Override
    public boolean shouldDisplay(HudContext context) {
        // Only appears while actually moving meaningfully — spec section 3.
        return context.godModeActive() || snapshot.state() != MovementState.IDLE;
    }

    @Override
    public void update(HudContext context) {
        if (context.client().player == null) return;
        snapshot = context.physics().movement().analyze(context.client().player);
    }

    @Override
    public void render(DrawContext ctx, HudContext context, int screenWidth, int screenHeight, int anchorY) {
        var font = context.client().textRenderer;
        int width = 150;
        int x = 8;
        renderer.drawPanel(ctx, x, anchorY, width, preferredHeight());
        renderer.drawHeading(ctx, font, "Movement", x, anchorY);

        int p = AscensionConfig.get().decimalPrecision;
        int y = anchorY + 16;
        renderer.drawLine(ctx, font, "Speed:", MathUtil.round(snapshot.speedBlocksPerSecond(), p) + " b/s", x, y); y += 10;
        renderer.drawLine(ctx, font, "Horizontal:", MathUtil.round(snapshot.horizontalSpeedBlocksPerSecond(), p) + " b/s", x, y); y += 10;
        renderer.drawLine(ctx, font, "Vertical:", MathUtil.round(snapshot.verticalSpeedBlocksPerSecond(), p) + " b/s", x, y); y += 10;
        renderer.drawLine(ctx, font, "State:", snapshot.state().name(), x, y);
    }

    @Override
    public int preferredHeight() {
        return 56;
    }
}
