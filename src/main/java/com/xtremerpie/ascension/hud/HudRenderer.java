package com.xtremerpie.ascension.hud;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Shared low-level drawing helpers implementing ASCENSION's visual
 * language (spec section 5): dark translucent panels, thin borders, small
 * glow accent, readable Minecraft-default text. Every HUD module draws
 * through here rather than hand-rolling its own panel style, so the whole
 * HUD stays visually consistent without copying another mod's exact UI.
 */
public final class HudRenderer {

    // ARGB colors. Panel is ~70% opaque near-black; border is a soft cyan
    // accent distinct from vanilla's usual white/gray/red HUD elements.
    public static final int PANEL_BACKGROUND = 0xB2101418;
    public static final int PANEL_BORDER = 0x8033D9C0;
    public static final int TEXT_PRIMARY = 0xFFE6F7F4;
    public static final int TEXT_SECONDARY = 0xFF9FB8B2;
    public static final int TEXT_ACCENT = 0xFF33D9C0;
    public static final int TEXT_WARNING = 0xFFE0A33D;

    private static final int PADDING = 6;

    public void drawPanel(DrawContext ctx, int x, int y, int width, int height) {
        ctx.fill(x, y, x + width, y + height, PANEL_BACKGROUND);
        // thin border: four 1px fills, cheap and avoids extra line-drawing state
        ctx.fill(x, y, x + width, y + 1, PANEL_BORDER);
        ctx.fill(x, y + height - 1, x + width, y + height, PANEL_BORDER);
        ctx.fill(x, y, x + 1, y + height, PANEL_BORDER);
        ctx.fill(x + width - 1, y, x + width, y + height, PANEL_BORDER);
    }

    public void drawHeading(DrawContext ctx, TextRenderer font, String text, int x, int y) {
        ctx.drawText(font, Text.literal(text.toUpperCase()), x + PADDING, y + PADDING, TEXT_ACCENT, true);
    }

    public void drawLine(DrawContext ctx, TextRenderer font, String label, String value, int x, int y) {
        ctx.drawText(font, Text.literal(label), x + PADDING, y, TEXT_SECONDARY, false);
        int labelWidth = font.getWidth(label);
        ctx.drawText(font, Text.literal(value), x + PADDING + labelWidth + 6, y, TEXT_PRIMARY, false);
    }

    public int padding() {
        return PADDING;
    }
}
