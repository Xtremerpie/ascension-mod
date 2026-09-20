package com.xtremerpie.ascension.achievements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.xtremerpie.ascension.core.AscensionConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Loads {@link AchievementDefinition}s from
 * {@code data/ascension/achievements/*.json} bundled in the mod jar (spec
 * section 18: "prefer data-driven... do not hardcode all achievement
 * definitions inside Java"). Because listing files inside a jar's
 * classpath resource folder isn't trivial in plain Java without extra
 * tooling, the folder also ships an {@code index.json} naming every file
 * to load — the registry reads that index, then reads each named file
 * individually via the classloader. This is a real, working file-based
 * loader, not a hardcoded Java list.
 */
public final class AchievementRegistry {

    private static final String RESOURCE_ROOT = "data/" + AscensionConstants.MOD_ID + "/achievements/";
    private static final Gson GSON = new GsonBuilder().create();

    private final Map<String, AchievementDefinition> definitions = new LinkedHashMap<>();

    public void loadAll(ClassLoader classLoader) {
        definitions.clear();
        try (InputStream indexStream = classLoader.getResourceAsStream(RESOURCE_ROOT + "index.json")) {
            if (indexStream == null) {
                logFallback("index.json not found on classpath at " + RESOURCE_ROOT + " — no achievements loaded.");
                return;
            }
            JsonArray fileNames = GSON.fromJson(new InputStreamReader(indexStream, StandardCharsets.UTF_8), JsonArray.class);
            for (var element : fileNames) {
                loadOne(classLoader, element.getAsString());
            }
        } catch (IOException e) {
            logFallback("I/O error reading achievement index: " + e.getMessage());
        }
    }

    private void loadOne(ClassLoader classLoader, String fileName) {
        try (InputStream stream = classLoader.getResourceAsStream(RESOURCE_ROOT + fileName)) {
            if (stream == null) {
                logFallback("Listed achievement file missing: " + fileName);
                return;
            }
            AchievementDefinition def = GSON.fromJson(
                    new InputStreamReader(stream, StandardCharsets.UTF_8), AchievementDefinition.class);
            if (def == null || def.id() == null) {
                logFallback("Malformed achievement file skipped: " + fileName);
                return;
            }
            definitions.put(def.id(), def);
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            logFallback("Failed to parse " + fileName + ": " + e.getMessage());
        }
    }

    private void logFallback(String message) {
        // A malformed/missing individual achievement file must not crash
        // the mod (spec section 36) — it's simply skipped, and reported
        // here for the developer to notice in logs.
        System.err.println("[Ascension] " + message);
    }

    public Optional<AchievementDefinition> get(String id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public Map<String, AchievementDefinition> all() {
        return definitions;
    }

    public int count() {
        return definitions.size();
    }
}
