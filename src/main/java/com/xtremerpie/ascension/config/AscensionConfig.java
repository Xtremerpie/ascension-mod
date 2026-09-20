package com.xtremerpie.ascension.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xtremerpie.ascension.core.AscensionConstants;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Plain-data config object, serialized to
 * {@code config/ascension.json} via Gson (bundled with Minecraft/Fabric —
 * no extra dependency needed). Loaded once via {@link #load(Path)} at mod
 * init; accessed thereafter via the {@link #get()} singleton.
 *
 * Every field here maps directly to a checkbox/slider in ConfigScreen —
 * spec section 22/29 requires no manual file editing for normal use, so
 * this class must stay simple, flat, directly-bindable data.
 */
public final class AscensionConfig {

    private static AscensionConfig instance;
    private static Path configPath;

    // ---- HUD ----
    public boolean hudEnabled = true;
    public boolean godUiEnabled = true;
    public boolean distanceModuleEnabled = true;
    public boolean targetModuleEnabled = true;
    public boolean movementModuleEnabled = true;
    public boolean physicsModuleEnabled = true;
    public boolean projectileModuleEnabled = true;
    public boolean worldModuleEnabled = true;
    public boolean achievementModuleEnabled = true;
    public boolean progressionModuleEnabled = true;
    public boolean notificationModuleEnabled = true;

    // ---- Units ----
    public MeasurementUnit measurementUnit = MeasurementUnit.BLOCKS;
    public int decimalPrecision = 1;

    // ---- Performance ----
    public int hudUpdateIntervalTicks = AscensionConstants.DEFAULT_HUD_UPDATE_INTERVAL_TICKS;
    public int worldScanIntervalTicks = AscensionConstants.DEFAULT_WORLD_SCAN_INTERVAL_TICKS;
    public int trajectoryUpdateIntervalTicks = AscensionConstants.DEFAULT_TRAJECTORY_UPDATE_INTERVAL_TICKS;
    public int entityScanRangeBlocks = AscensionConstants.DEFAULT_ENTITY_SCAN_RANGE_BLOCKS;

    // ---- Trajectory rendering ----
    public boolean trajectoryVisualizationEnabled = true;
    public int trajectoryMaxSteps = 100;
    public int trajectorySampleEveryNTicks = 2;

    // ---- Notifications ----
    public boolean notificationsEnabled = true;
    public int notificationDurationTicks = 100; // 5s

    // ---- Debug ----
    public boolean debugMode = false;

    // ---- Keybinds (translation-key-free raw GLFW-friendly names; actual
    // KeyBinding registration lives in AscensionClient and reads these as
    // defaults only — Minecraft's own controls menu is the real source of
    // truth after the player changes them there) ----
    public String keybindGodUi = "key.keyboard.g";
    public String keybindToggleHud = "key.keyboard.h";
    public String keybindOpenConfig = "key.keyboard.o";

    public enum MeasurementUnit {
        BLOCKS, METERS, BOTH
    }

    public static AscensionConfig get() {
        if (instance == null) {
            instance = new AscensionConfig();
        }
        return instance;
    }

    public static void load(Path path) {
        configPath = path;
        Gson gson = gson();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                AscensionConfig loaded = gson.fromJson(reader, AscensionConfig.class);
                instance = loaded != null ? loaded : new AscensionConfig();
                return;
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                // Malformed or unreadable config: fall back to defaults rather
                // than crash the mod on startup (spec section 36).
                instance = new AscensionConfig();
                return;
            }
        }
        instance = new AscensionConfig();
        save();
    }

    public static void save() {
        if (configPath == null || instance == null) return;
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                gson().toJson(instance, writer);
            }
        } catch (IOException e) {
            // Persistence failure shouldn't crash gameplay; settings just
            // won't survive restart until the underlying issue is fixed.
        }
    }

    private static Gson gson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    // Per-module update interval lookup used by HudManager, so a single
    // "HUD update frequency" setting can still be overridden per module in
    // the future without changing HudManager's call sites.
    private static final Map<String, Integer> perModuleOverride = new HashMap<>();

    public int updateIntervalTicksFor(String moduleId) {
        if ("world".equals(moduleId)) return worldScanIntervalTicks;
        if ("projectile".equals(moduleId)) return trajectoryUpdateIntervalTicks;
        return perModuleOverride.getOrDefault(moduleId, hudUpdateIntervalTicks);
    }
}
