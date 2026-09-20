package com.xtremerpie.ascension.ui;

import com.xtremerpie.ascension.achievements.AchievementRegistry;
import com.xtremerpie.ascension.client.ClientAscensionState;
import com.xtremerpie.ascension.structures.BlueprintRegistry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Main ASCENSION menu (spec section 27's "AscensionScreen"), opened with O. */
public final class AscensionScreen extends Screen {

    private final AchievementRegistry achievementRegistry;
    private final BlueprintRegistry blueprintRegistry;

    public AscensionScreen(AchievementRegistry achievementRegistry, BlueprintRegistry blueprintRegistry) {
        super(Text.literal("Ascension"));
        this.achievementRegistry = achievementRegistry;
        this.blueprintRegistry = blueprintRegistry;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 75;
        int y = this.height / 2 - 60;
        int spacing = 24;

        var dto = ClientAscensionState.current();

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Status"), b -> this.client.setScreen(
                new StatusScreen(dto.ascensionLevel, dto.ascensionXp, dto.completedAchievements.size(),
                        achievementRegistry.count(), dto.unlockedBlueprintIds.size())))
                .dimensions(x, y, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Achievements"), b -> this.client.setScreen(
                new AchievementScreen(achievementRegistry, this)))
                .dimensions(x, y + spacing, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Blueprints"), b -> this.client.setScreen(
                new BlueprintScreen(blueprintRegistry, this)))
                .dimensions(x, y + spacing * 2, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Configuration"), b -> this.client.setScreen(
                new ConfigScreen(this)))
                .dimensions(x, y + spacing * 3, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> this.close())
                .dimensions(x, y + spacing * 4 + 10, 150, 20).build());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
