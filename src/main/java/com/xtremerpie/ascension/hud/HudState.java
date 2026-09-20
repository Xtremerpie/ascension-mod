package com.xtremerpie.ascension.hud;

/**
 * Runtime (not persisted-to-disk — that's AscensionConfig) HUD toggle
 * state: whether the HUD is showing at all, and whether ASCENSION/GOD mode
 * is currently active. Backed by simple volatile fields since it's only
 * ever touched from the client thread (keybind handlers + render calls,
 * both client-thread), but marked volatile defensively in case a future
 * screen/network callback touches it off-thread.
 */
public final class HudState {

    private volatile boolean hudVisible = true;
    private volatile boolean godModeActive = false;

    public boolean isHudVisible() {
        return hudVisible;
    }

    public void toggleHudVisible() {
        hudVisible = !hudVisible;
    }

    public boolean isGodModeActive() {
        return godModeActive;
    }

    public void toggleGodMode() {
        godModeActive = !godModeActive;
    }
}
