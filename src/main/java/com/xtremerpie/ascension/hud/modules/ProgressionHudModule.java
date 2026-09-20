package com.xtremerpie.ascension.hud.modules;

import com.xtremerpie.ascension.config.AscensionConfig;
import com.xtremerpie.ascension.hud.HudContext;
import com.xtremerpie.ascension.hud.HudModule;
import com.xtremerpie.ascension.hud.HudRenderer;
import com.xtremerpie.ascension.notification.Notification;
import com.xtremerpie.ascension.notification.NotificationManager;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

/** Renders level-up popups. */
public final class ProgressionHudModule implements HudModule {

    private final HudRenderer renderer;
    private List<Notification> active = List.of();

    public ProgressionHudModule(HudRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public String id() {
        return "progression";
    }

    @Override
    public boolean isEnabled(HudContext context) {
        return AscensionConfig.get().progressionModuleEnabled;
    }

    @Override
    public boolean shouldDisplay(HudContext context) {
        return !active.isEmpty();
    }

    @Override
    public void update(HudContext context) {
        active = NotificationManager.client().visibleOf(Notification.Category.PROGRESSION);
    }

    @Override
    public void render(DrawContext ctx, HudContext context, int screenWidth, int screenHeight, int anchorY) {
        var font = context.client().textRenderer;
        int width = 180;
        int height = 36;
        int x = (screenWidth - width) / 2;
        int y = 90;

        for (Notification n : active) {
            renderer.drawPanel(ctx, x, y, width, height);
            renderer.drawHeading(ctx, font, "Level Up", x, y);
            ctx.drawText(font, n.title(), x + renderer.padding(), y + 16, HudRenderer.TEXT_ACCENT, false);
            y += height + 4;
        }
    }

    @Override
    public int preferredHeight() {
        return 0;
    }
}
