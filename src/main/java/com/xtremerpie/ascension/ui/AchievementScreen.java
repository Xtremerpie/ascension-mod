package com.xtremerpie.ascension.ui;

import com.xtremerpie.ascension.achievements.AchievementDefinition;
import com.xtremerpie.ascension.achievements.AchievementRegistry;
import com.xtremerpie.ascension.client.ClientAscensionState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Real achievement browser (spec 27/31): category, title, description,
 * progress fraction, completion state, all read from the actual
 * {@link AchievementRegistry} (data-driven, same file the server loads)
 * and the synced {@link ClientAscensionState} progress snapshot — not
 * fabricated placeholder entries.
 */
public final class AchievementScreen extends Screen {

    private static final int ROW_HEIGHT = 34;

    private final Screen parent;
    private final List<AchievementDefinition> sorted;
    private int scrollOffset = 0;

    public AchievementScreen(AchievementRegistry registry, Screen parent) {
        super(Text.literal("Achievements"));
        this.parent = parent;
        this.sorted = registry.all().values().stream()
                .sorted((a, b) -> a.category().compareTo(b.category()))
                .toList();
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> this.close())
                .dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        var dto = ClientAscensionState.current();
        int listTop = 30;
        int listBottom = this.height - 40;
        int x = this.width / 2 - 140;
        int width = 280;

        ctx.drawText(this.textRenderer, Text.literal(dto.completedAchievements.size() + " / " + sorted.size() + " completed"),
                this.width / 2 - 60, 10, 0xFF33D9C0, true);

        ctx.enableScissor(x, listTop, x + width, listBottom);
        int y = listTop - scrollOffset;

        for (AchievementDefinition def : sorted) {
            if (y + ROW_HEIGHT >= listTop && y <= listBottom) {
                boolean completed = dto.completedAchievements.contains(def.id());
                double progress = dto.achievementProgress.getOrDefault(def.id(), 0.0);
                double fraction = def.condition().progressFraction(progress);

                int color = completed ? 0xFF33D9C0 : 0xFFAAAAAA;
                ctx.drawText(this.textRenderer, Text.literal((completed ? "[DONE] " : "[ ] ") + def.title()
                        + "  (" + def.category() + ")"), x, y, color, false);
                ctx.drawText(this.textRenderer, Text.literal(def.description()), x, y + 10, 0xFF888888, false);
                if (!completed) {
                    ctx.drawText(this.textRenderer, Text.literal(String.format("Progress: %.0f%%", fraction * 100)),
                            x, y + 20, 0xFF666666, false);
                }
            }
            y += ROW_HEIGHT;
        }
        ctx.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, sorted.size() * ROW_HEIGHT - (this.height - 70));
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - verticalAmount * ROW_HEIGHT));
        return true;
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
