package com.xtremerpie.ascension.hud.modules;

import com.xtremerpie.ascension.config.AscensionConfig;
import com.xtremerpie.ascension.hud.HudContext;
import com.xtremerpie.ascension.hud.HudModule;
import com.xtremerpie.ascension.hud.HudRenderer;
import com.xtremerpie.ascension.notification.Notification;
import com.xtremerpie.ascension.notification.NotificationManager;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

/** Renders "ASCENSION COMPLETE" style achievement popups (spec section 21). */
public final class AchievementHudModule implements HudModule {

    private final HudRenderer renderer;
    private List<Notification> active = List.of();

    public AchievementHudModule(HudRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public String id() {
        return "achievement";
    }

    @Override
    public boolean isEnabled(HudContext context) {
        return AscensionConfig.get().achievementModuleEnabled;
    }

    @Override
    public boolean shouldDisplay(HudContext context) {
        return !active.isEmpty();
    }

    @Override
    public void update(HudContext context) {
        active = NotificationManager.client().visibleOf(Notification.Category.ACHIEVEMENT);
    }

    @Override
    public void render(DrawContext ctx, HudContext context, int screenWidth, int screenHeight, int anchorY) {
        var font = context.client().textRenderer;
        int width = 180;
        int height = 44;
        int x = (screenWidth - width) / 2;
        int y = 40;

        for (Notification n : active) {
            renderer.drawPanel(ctx, x, y, width, height);
            renderer.drawHeading(ctx, font, "Ascension Complete", x, y);
            ctx.drawText(font, n.title(), x + renderer.padding(), y + 16, HudRenderer.TEXT_ACCENT, false);
            ctx.drawText(font, n.subtitle(), x + renderer.padding(), y + 28, HudRenderer.TEXT_PRIMARY, false);
            y += height + 4;
        }
    }

    @Override
    public int preferredHeight() {
        return 0; // self-positions top-center, independent of the left stack
    }
}
