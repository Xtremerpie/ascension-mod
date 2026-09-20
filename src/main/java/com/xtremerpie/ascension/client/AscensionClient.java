package com.xtremerpie.ascension.client;

import com.xtremerpie.ascension.achievements.AchievementRegistry;
import com.xtremerpie.ascension.config.AscensionConfig;
import com.xtremerpie.ascension.hud.HudManager;
import com.xtremerpie.ascension.network.NetworkPackets;
import com.xtremerpie.ascension.network.PlayerStatusDto;
import com.xtremerpie.ascension.notification.Notification;
import com.xtremerpie.ascension.notification.NotificationManager;
import com.xtremerpie.ascension.structures.BlueprintRegistry;
import com.xtremerpie.ascension.ui.AscensionScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.projectile.PersistentProjectileEntity;

/**
 * Client-only entrypoint. Owns all rendering, keybindings and client-side
 * ticking — nothing here is safe to call from a dedicated server, which is
 * exactly why it's a separate entrypoint/class from {@link com.xtremerpie.ascension.AscensionMod}
 * (spec section 7).
 */
public final class AscensionClient implements ClientModInitializer {

    private final HudManager hudManager = new HudManager();
    private final TrajectoryRenderer trajectoryRenderer = new TrajectoryRenderer();

    // Client-side copies of the same data-driven registries AscensionMod
    // loads server-side. These read JSON bundled in the mod jar itself
    // (shipped to both sides), not anything server-supplied, so no
    // network round trip is needed just to know what achievements/
    // blueprints exist — only per-player PROGRESS needs syncing (see
    // ClientAscensionState / PlayerStatusDto).
    private final AchievementRegistry clientAchievementRegistry = new AchievementRegistry();
    private final BlueprintRegistry clientBlueprintRegistry = new BlueprintRegistry();

    private KeyBinding godUiKey;
    private KeyBinding toggleHudKey;
    private KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {
        clientAchievementRegistry.loadAll(AscensionClient.class.getClassLoader());
        clientBlueprintRegistry.loadAll(AscensionClient.class.getClassLoader());

        godUiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.ascension.god_ui", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_G, "category.ascension"));
        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.ascension.toggle_hud", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_H, "category.ascension"));
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.ascension.open_config", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_O, "category.ascension"));

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        HudRenderCallback.EVENT.register((drawContext, tickCounter) ->
                hudManager.render(drawContext, MinecraftClient.getInstance()));

        WorldRenderEvents.AFTER_TRANSLUCENT.register(this::onWorldRender);

        ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.AchievementCompletePayload.ID, (payload, context) ->
                context.client().execute(() -> NotificationManager.client().push(
                        Notification.Category.ACHIEVEMENT, payload.title(), payload.description(), null)));

        ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.LevelUpPayload.ID, (payload, context) ->
                context.client().execute(() -> NotificationManager.client().push(
                        Notification.Category.PROGRESSION, "Level " + payload.newLevel(),
                        payload.milestoneUnlocked() == null || payload.milestoneUnlocked().isEmpty()
                                ? "" : "Unlocked: " + payload.milestoneUnlocked(),
                        null)));

        ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.PlayerStatusPayload.ID, (payload, context) ->
                context.client().execute(() -> ClientAscensionState.update(PlayerStatusDto.fromJson(payload.statusJson()))));
    }

    private void onClientTick(MinecraftClient client) {
        if (client.player == null) return;

        hudManager.tick(client);

        if (godUiKey.wasPressed() && AscensionConfig.get().godUiEnabled) {
            hudManager.state().toggleGodMode();
        }
        if (toggleHudKey.wasPressed()) {
            hudManager.state().toggleHudVisible();
        }
        if (openConfigKey.wasPressed() && client.currentScreen == null) {
            client.setScreen(new AscensionScreen(clientAchievementRegistry, clientBlueprintRegistry));
        }
    }

    private void onWorldRender(WorldRenderEvents.Context context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || !AscensionConfig.get().trajectoryVisualizationEnabled) return;

        // Same bounded, owner-filtered search ProjectileHudModule uses —
        // reused here rather than duplicated logic would be cleaner as a
        // shared helper, noted as a small follow-up refactor.
        var box = client.player.getBoundingBox().expand(AscensionConfig.get().entityScanRangeBlocks);
        PersistentProjectileEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (var entity : client.world.getEntitiesByClass(PersistentProjectileEntity.class, box, e -> true)) {
            if (entity.getOwner() != client.player) continue;
            double d = entity.getPos().distanceTo(client.player.getPos());
            if (d < best) {
                best = d;
                nearest = entity;
            }
        }

        if (nearest != null) {
            trajectoryRenderer.render(context, nearest);
        }
    }
}

