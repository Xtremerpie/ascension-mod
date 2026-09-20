package com.xtremerpie.ascension.ui;

import com.xtremerpie.ascension.config.AscensionConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;

/**
 * Real, functional in-game configuration screen (spec 22/29) — every
 * checkbox here directly reads/writes {@link AscensionConfig} and the
 * change is saved to disk on close. Deliberately covers the HUD-module
 * toggles first (the settings a player reaches for most); performance and
 * keybind sub-pages are natural follow-ups using the same pattern, noted
 * as SIMPLIFIED in IMPLEMENTATION_STATUS.md rather than faked here.
 */
public final class ConfigScreen extends Screen {

    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Text.literal("Ascension Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        AscensionConfig cfg = AscensionConfig.get();
        int x = this.width / 2 - 100;
        int y = this.height / 2 - 90;
        int spacing = 22;

        addCheckbox(x, y, "HUD Enabled", cfg.hudEnabled, v -> cfg.hudEnabled = v);
        addCheckbox(x, y + spacing, "God UI Enabled", cfg.godUiEnabled, v -> cfg.godUiEnabled = v);
        addCheckbox(x, y + spacing * 2, "Distance Module", cfg.distanceModuleEnabled, v -> cfg.distanceModuleEnabled = v);
        addCheckbox(x, y + spacing * 3, "Target Module", cfg.targetModuleEnabled, v -> cfg.targetModuleEnabled = v);
        addCheckbox(x, y + spacing * 4, "Movement Module", cfg.movementModuleEnabled, v -> cfg.movementModuleEnabled = v);
        addCheckbox(x, y + spacing * 5, "Physics Module", cfg.physicsModuleEnabled, v -> cfg.physicsModuleEnabled = v);
        addCheckbox(x, y + spacing * 6, "Projectile Module", cfg.projectileModuleEnabled, v -> cfg.projectileModuleEnabled = v);
        addCheckbox(x, y + spacing * 7, "World Module", cfg.worldModuleEnabled, v -> cfg.worldModuleEnabled = v);
        addCheckbox(x, y + spacing * 8, "Notifications", cfg.notificationsEnabled, v -> cfg.notificationsEnabled = v);

        this.addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(Text.literal("Done"), b -> this.close())
                .dimensions(this.width / 2 - 50, y + spacing * 9 + 10, 100, 20).build());
    }

    private void addCheckbox(int x, int y, String label, boolean initial, java.util.function.Consumer<Boolean> onChange) {
        this.addDrawableChild(CheckboxWidget.builder(Text.literal(label), this.textRenderer)
                .pos(x, y)
                .checked(initial)
                .callback((checkbox, checked) -> onChange.accept(checked))
                .build());
    }

    @Override
    public void close() {
        AscensionConfig.save();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
