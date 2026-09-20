package com.xtremerpie.ascension.hud.modules;

import com.xtremerpie.ascension.config.AscensionConfig;
import com.xtremerpie.ascension.hud.HudContext;
import com.xtremerpie.ascension.hud.HudModule;
import com.xtremerpie.ascension.hud.HudRenderer;
import com.xtremerpie.ascension.targeting.TargetSnapshot;
import net.minecraft.client.gui.DrawContext;

import java.util.Locale;

public final class TargetHudModule implements HudModule {

    private final HudRenderer renderer;
    private TargetSnapshot snapshot = TargetSnapshot.none();

    public TargetHudModule(HudRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public String id() {
        return "target";
    }

    @Override
    public boolean isEnabled(HudContext context) {
        return AscensionConfig.get().targetModuleEnabled;
    }

    @Override
    public boolean shouldDisplay(HudContext context) {
        return snapshot.isPresent();
    }

    @Override
    public void update(HudContext context) {
        snapshot = context.targeting().currentTarget();
    }

    @Override
    public void render(DrawContext ctx, HudContext context, int screenWidth, int screenHeight, int anchorY) {
        var font = context.client().textRenderer;
        int width = 150;
        int height = preferredHeight();
        int x = 8;

        renderer.drawPanel(ctx, x, anchorY, width, height);
        renderer.drawHeading(ctx, font, snapshot.type() == TargetSnapshot.TargetType.PLAYER ? "Player" : "Entity", x, anchorY);

        int lineY = anchorY + 16;
        renderer.drawLine(ctx, font, "Name:", snapshot.displayName(), x, lineY);
        lineY += 10;
        renderer.drawLine(ctx, font, "Distance:", format(snapshot.distance()), x, lineY);
        lineY += 10;
        renderer.drawLine(ctx, font, "Height diff:", formatSigned(snapshot.verticalDifference()), x, lineY);
        lineY += 10;
        renderer.drawLine(ctx, font, "Direction:", snapshot.compassLabel(), x, lineY);

        if (snapshot.health() != null) {
            lineY += 10;
            renderer.drawLine(ctx, font, "Health:", String.format(Locale.ROOT, "%.1f / %.1f", snapshot.health(), snapshot.maxHealth()), x, lineY);
        }
    }

    @Override
    public int preferredHeight() {
        return snapshot.health() != null ? 66 : 56;
    }

    private String format(double blocks) {
        AscensionConfig cfg = AscensionConfig.get();
        return switch (cfg.measurementUnit) {
            case BLOCKS -> round(blocks) + " blocks";
            case METERS -> round(blocks) + " m";
            case BOTH -> round(blocks) + " blocks (" + round(blocks) + " m)";
        };
    }

    private String formatSigned(double value) {
        String sign = value >= 0 ? "+" : "";
        return sign + format(value);
    }

    private double round(double v) {
        int p = AscensionConfig.get().decimalPrecision;
        double f = Math.pow(10, p);
        return Math.round(v * f) / f;
    }
}
