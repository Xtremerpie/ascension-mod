package com.xtremerpie.ascension.ui;

import com.xtremerpie.ascension.progression.AscensionLevel;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * ASCENSION STATUS screen (spec 28/31).
 *
 * HONEST SCOPE NOTE: real player progress (level, XP, achievements,
 * blueprints, statistics) lives server-side in {@code PlayerData}. This
 * screen is client-only code and therefore cannot read that server object
 * directly in multiplayer — it needs the data pushed to it over the
 * network first. That sync packet (a "PlayerStatusSyncPayload" following
 * the same pattern as {@code NetworkPackets}) is not yet implemented, so
 * this screen currently renders with whatever values are passed into its
 * constructor rather than fabricating numbers. In singleplayer this can
 * be wired directly through the integrated server's PlayerDataManager;
 * that wiring is a short, well-scoped follow-up, listed in
 * IMPLEMENTATION_STATUS.md rather than faked here with placeholder
 * numbers.
 */
public final class StatusScreen extends Screen {

    private final int level;
    private final double xp;
    private final int achievementsCompleted;
    private final int totalAchievements;
    private final int blueprintsUnlocked;

    public StatusScreen(int level, double xp, int achievementsCompleted, int totalAchievements, int blueprintsUnlocked) {
        super(Text.literal("Ascension Status"));
        this.level = level;
        this.xp = xp;
        this.achievementsCompleted = achievementsCompleted;
        this.totalAchievements = totalAchievements;
        this.blueprintsUnlocked = blueprintsUnlocked;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> this.close())
                .dimensions(this.width / 2 - 50, this.height / 2 + 70, 100, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int x = this.width / 2 - 90;
        int y = this.height / 2 - 70;

        ctx.drawText(this.textRenderer, Text.literal("ASCENSION STATUS"), x, y, 0xFF33D9C0, true);
        y += 16;
        ctx.drawText(this.textRenderer, Text.literal("Level: " + level), x, y, 0xFFFFFFFF, false); y += 12;
        ctx.drawText(this.textRenderer, Text.literal(String.format("XP: %.0f / %.0f (%.0f%%)",
                AscensionLevel.xpIntoCurrentLevel(xp), AscensionLevel.xpNeededForNextLevel(xp),
                AscensionLevel.progressFraction(xp) * 100)), x, y, 0xFFFFFFFF, false); y += 12;
        ctx.drawText(this.textRenderer, Text.literal("Achievements: " + achievementsCompleted + " / " + totalAchievements), x, y, 0xFFFFFFFF, false); y += 12;
        ctx.drawText(this.textRenderer, Text.literal("Blueprints unlocked: " + blueprintsUnlocked), x, y, 0xFFFFFFFF, false);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
