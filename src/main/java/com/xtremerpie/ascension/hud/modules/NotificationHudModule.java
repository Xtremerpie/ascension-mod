package com.xtremerpie.ascension.hud.modules;

import com.xtremerpie.ascension.config.AscensionConfig;
import com.xtremerpie.ascension.hud.HudContext;
import com.xtremerpie.ascension.hud.HudModule;
import com.xtremerpie.ascension.hud.HudRenderer;
import com.xtremerpie.ascension.notification.Notification;
import com.xtremerpie.ascension.notification.NotificationManager;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

/**
 * Renders general/structure/blueprint notifications, stacked top-right.
 * Achievement and progression notifications get their own modules
 * ({@link AchievementHudModule}, {@link ProgressionHudModule}) so each can
 * be toggled off independently per spec section 4.
 */
public final class NotificationHudModule implements HudModule {

    private final HudRenderer renderer;
    private List<Notification> active = List.of();

    public NotificationHudModule(HudRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public String id() {
        return "notification";
    }

    @Override
    public boolean isEnabled(HudContext context) {
        return AscensionConfig.get().notificationModuleEnabled;
    }

    @Override
    public boolean shouldDisplay(HudContext context) {
        return !active.isEmpty();
    }

    @Override
    public void update(HudContext context) {
        NotificationManager.client().tick();
        active = NotificationManager.client().visibleOf(Notification.Category.GENERAL);
        active = concat(active, NotificationManager.client().visibleOf(Notification.Category.BLUEPRINT));
        active = concat(active, NotificationManager.client().visibleOf(Notification.Category.STRUCTURE));
    }

    private List<Notification> concat(List<Notification> a, List<Notification> b) {
        if (b.isEmpty()) return a;
        var combined = new java.util.ArrayList<>(a);
        combined.addAll(b);
        return combined;
    }

    @Override
    public void render(DrawContext ctx, HudContext context, int screenWidth, int screenHeight, int anchorY) {
        var font = context.client().textRenderer;
        int width = 170;
        int y = screenHeight - 16;

        for (Notification n : active) {
            int height = 30;
            y -= height + 4;
            int x = screenWidth - width - 8;
            renderer.drawPanel(ctx, x, y, width, height);
            renderer.drawHeading(ctx, font, n.title(), x, y);
            ctx.drawText(font, n.subtitle(), x + renderer.padding(), y + 16, HudRenderer.TEXT_PRIMARY, false);
        }
    }

    @Override
    public int preferredHeight() {
        return 0; // self-positions from the bottom, doesn't take part in the top-left stack
    }
}
