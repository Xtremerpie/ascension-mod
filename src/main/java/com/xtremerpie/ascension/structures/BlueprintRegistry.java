package com.xtremerpie.ascension.structures;

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

public final class BlueprintRegistry {

    private static final String RESOURCE_ROOT = "data/" + AscensionConstants.MOD_ID + "/structures/";
    private static final Gson GSON = new GsonBuilder().create();

    private final Map<String, Blueprint> blueprints = new LinkedHashMap<>();

    public void loadAll(ClassLoader classLoader) {
        blueprints.clear();
        try (InputStream indexStream = classLoader.getResourceAsStream(RESOURCE_ROOT + "index.json")) {
            if (indexStream == null) {
                System.err.println("[Ascension] Blueprint index.json not found — no blueprints loaded.");
                return;
            }
            JsonArray fileNames = GSON.fromJson(new InputStreamReader(indexStream, StandardCharsets.UTF_8), JsonArray.class);
            for (var element : fileNames) {
                loadOne(classLoader, element.getAsString());
            }
        } catch (IOException e) {
            System.err.println("[Ascension] Failed reading blueprint index: " + e.getMessage());
        }
    }

    private void loadOne(ClassLoader classLoader, String fileName) {
        try (InputStream stream = classLoader.getResourceAsStream(RESOURCE_ROOT + fileName)) {
            if (stream == null) return;
            Blueprint bp = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), Blueprint.class);
            if (bp == null || bp.id() == null) {
                System.err.println("[Ascension] Malformed blueprint file skipped: " + fileName);
                return;
            }
            blueprints.put(bp.id(), bp);
        } catch (IOException e) {
            System.err.println("[Ascension] Failed to parse blueprint " + fileName + ": " + e.getMessage());
        }
    }

    public Optional<Blueprint> get(String id) {
        return Optional.ofNullable(blueprints.get(id));
    }

    public Map<String, Blueprint> all() {
        return blueprints;
    }
}
