package com.xtremerpie.ascension.hud.modules;

import com.xtremerpie.ascension.config.AscensionConfig;
import com.xtremerpie.ascension.hud.HudContext;
import com.xtremerpie.ascension.hud.HudModule;
import com.xtremerpie.ascension.hud.HudRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/**
 * The GOD-UI world intelligence panel (spec section 11/5). Only visible
 * while ASCENSION/GOD mode is toggled on, and only recomputed at
 * {@code worldScanIntervalTicks} — bounded box entity search, never a
 * whole-world scan (spec 12).
 */
public final class WorldHudModule implements HudModule {

    private final HudRenderer renderer;

    private BlockPos pos = BlockPos.ORIGIN;
    private String dimension = "-";
    private String biome = "-";
    private int lightLevel = 0;
    private int players, hostiles, passives;

    public WorldHudModule(HudRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public String id() {
        return "world";
    }

    @Override
    public boolean isEnabled(HudContext context) {
        return AscensionConfig.get().worldModuleEnabled;
    }

    @Override
    public boolean shouldDisplay(HudContext context) {
        return context.godModeActive();
    }

    @Override
    public void update(HudContext context) {
        var client = context.client();
        if (client.player == null || client.world == null) return;

        pos = client.player.getBlockPos();
        dimension = client.world.getRegistryKey().getValue().toString();
        biome = client.world.getBiome(pos).getKey().map(k -> k.getValue().toString()).orElse("-");
        lightLevel = client.world.getLightLevel(pos);

        int range = AscensionConfig.get().entityScanRangeBlocks;
        Box box = client.player.getBoundingBox().expand(range);

        players = client.world.getEntitiesByClass(PlayerEntity.class, box, e -> e != client.player).size();
        hostiles = client.world.getEntitiesByClass(HostileEntity.class, box, e -> true).size();
        passives = client.world.getEntitiesByClass(PassiveEntity.class, box, e -> true).size()
                + client.world.getEntitiesByClass(PathAwareEntity.class, box, e -> !(e instanceof HostileEntity) && !(e instanceof PassiveEntity)).size();
    }

    @Override
    public void render(DrawContext ctx, HudContext context, int screenWidth, int screenHeight, int anchorY) {
        var font = context.client().textRenderer;
        int width = 200;
        int x = (screenWidth - width) / 2;
        int y0 = 8;

        renderer.drawPanel(ctx, x, y0, width, preferredHeight());
        renderer.drawHeading(ctx, font, "Ascension World Intelligence", x, y0);

        int y = y0 + 16;
        renderer.drawLine(ctx, font, "Position:", pos.getX() + ", " + pos.getY() + ", " + pos.getZ(), x, y); y += 10;
        renderer.drawLine(ctx, font, "Dimension:", dimension, x, y); y += 10;
        renderer.drawLine(ctx, font, "Biome:", biome, x, y); y += 10;
        renderer.drawLine(ctx, font, "Light:", String.valueOf(lightLevel), x, y); y += 10;
        renderer.drawLine(ctx, font, "Players:", String.valueOf(players), x, y); y += 10;
        renderer.drawLine(ctx, font, "Hostile:", String.valueOf(hostiles), x, y); y += 10;
        renderer.drawLine(ctx, font, "Passive:", String.valueOf(passives), x, y);
    }

    @Override
    public int preferredHeight() {
        return 96;
    }
}
