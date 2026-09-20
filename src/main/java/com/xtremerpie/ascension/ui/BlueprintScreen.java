package com.xtremerpie.ascension.ui;

import com.xtremerpie.ascension.client.ClientAscensionState;
import com.xtremerpie.ascension.structures.Blueprint;
import com.xtremerpie.ascension.structures.BlueprintRegistry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Real blueprint browser (spec 18/26/31): name, description, size,
 * unlock state, and a working Build button.
 *
 * The Build button sends the player's own {@code /ascension build <id>}
 * chat command rather than a new client->server packet type — this reuses
 * the exact same server-authoritative path already implemented in
 * {@code AscensionCommands}/{@code StructureManager}, instead of adding a
 * second, parallel, unverified network message for the same action.
 */
public final class BlueprintScreen extends Screen {

    private static final int ROW_HEIGHT = 54;

    private final Screen parent;
    private final List<Blueprint> blueprints;
    private int scrollOffset = 0;

    public BlueprintScreen(BlueprintRegistry registry, Screen parent) {
        super(Text.literal("Blueprints"));
        this.parent = parent;
        this.blueprints = registry.all().values().stream().toList();
    }

    @Override
    protected void init() {
        var dto = ClientAscensionState.current();
        int x = this.width / 2 - 140;
        int listTop = 30;
        int y = listTop - scrollOffset;

        for (Blueprint bp : blueprints) {
            boolean unlocked = dto.unlockedBlueprintIds.contains(bp.id());
            int buttonY = y + 30;
            if (buttonY > listTop - 20 && buttonY < this.height - 40) {
                this.addDrawableChild(ButtonWidget.builder(
                                Text.literal(unlocked ? "Build here" : "Locked"),
                                b -> {
                                    if (unlocked && this.client != null && this.client.player != null) {
                                        this.client.player.networkHandler.sendChatCommand("ascension build " + bp.id());
                                    }
                                })
                        .dimensions(x + 220, buttonY, 60, 18)
                        .build());
            }
            y += ROW_HEIGHT;
        }

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

        ctx.enableScissor(x, listTop, x + 280, listBottom);
        int y = listTop - scrollOffset;

        for (Blueprint bp : blueprints) {
            if (y + ROW_HEIGHT >= listTop && y <= listBottom) {
                boolean unlocked = dto.unlockedBlueprintIds.contains(bp.id());
                int color = unlocked ? 0xFF33D9C0 : 0xFF888888;
                ctx.drawText(this.textRenderer, Text.literal(bp.name() + (unlocked ? " (Unlocked)" : " (Locked)")), x, y, color, true);
                ctx.drawText(this.textRenderer, Text.literal(bp.description()), x, y + 10, 0xFFAAAAAA, false);
                ctx.drawText(this.textRenderer, Text.literal("Size: " + bp.sizeX() + "x" + bp.sizeY() + "x" + bp.sizeZ()), x, y + 20, 0xFF888888, false);
            }
            y += ROW_HEIGHT;
        }
        ctx.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, blueprints.size() * ROW_HEIGHT - (this.height - 70));
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - verticalAmount * ROW_HEIGHT));
        this.clearAndInit();
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
