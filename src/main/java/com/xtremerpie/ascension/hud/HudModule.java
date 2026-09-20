package com.xtremerpie.ascension.hud;

import net.minecraft.client.gui.DrawContext;

/**
 * A single independently-toggleable HUD element (distance, target,
 * movement, physics, projectile, world, entity, achievement, progression,
 * notification — see the {@code modules} package). Spec section 4/9:
 * "each module should be independently enabled/disabled" and no single
 * god-class HUD.
 */
public interface HudModule {

    /** Stable id used by config to enable/disable this module, e.g. "distance". */
    String id();

    /** Whether the player has this module turned on in config at all. */
    boolean isEnabled(HudContext context);

    /**
     * Contextual gate: even if enabled, should this module render right
     * now? (e.g. DistanceHudModule only returns true while a valid target
     * exists). This is what keeps the HUD from permanently covering the
     * screen — spec section 3.
     */
    boolean shouldDisplay(HudContext context);

    /**
     * Recompute this module's data. Called at the module's own configured
     * interval (see AscensionConfig), not necessarily every frame —
     * performance requirement from spec section 12.
     */
    void update(HudContext context);

    /** Draw. Only called when {@link #isEnabled} and {@link #shouldDisplay} are both true. */
    void render(DrawContext drawContext, HudContext context, int screenWidth, int screenHeight, int anchorY);

    /** Approximate vertical space this module needs, for simple vertical stacking. */
    int preferredHeight();
}
