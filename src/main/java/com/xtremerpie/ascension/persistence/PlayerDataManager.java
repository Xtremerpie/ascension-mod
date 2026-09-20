package com.xtremerpie.ascension.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persists {@link PlayerData} as one JSON file per player under
 * {@code <world save>/ascension/playerdata/<uuid>.json}.
 *
 * DESIGN NOTE (documented simplification, not a shortcut taken lightly):
 * Minecraft/Fabric's normal recommendation for this kind of data is the
 * engine's PersistentState API. That API's exact current shape (it moved
 * to a Codec-based registration in recent versions) could not be verified
 * against the actual 1.21.11 source in this environment (no network
 * access to the mapped game jar — see IMPLEMENTATION_STATUS.md). Rather
 * than guess at a signature and risk shipping code that doesn't compile
 * against the real API, this uses plain, stable, long-standing APIs
 * (MinecraftServer#getSavePath, java.nio.file, Gson) to achieve the same
 * outcome: player data survives world reload, Minecraft restart, and
 * server restart. If you confirm the current PersistentState API locally,
 * migrating to it is a drop-in replacement for this class only — nothing
 * else in the mod depends on the storage mechanism.
 */
public final class PlayerDataManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private Path storageDir;

    public void init(MinecraftServer server) {
        storageDir = server.getSavePath(WorldSavePath.ROOT).resolve("ascension").resolve("playerdata");
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            System.err.println("[Ascension] Could not create player data directory: " + e.getMessage());
        }
    }

    public PlayerData get(ServerPlayerEntity player) {
        return cache.computeIfAbsent(player.getUuid(), id -> loadOrCreate(id));
    }

    private PlayerData loadOrCreate(UUID id) {
        Path file = fileFor(id);
        if (storageDir != null && Files.exists(file)) {
            try {
                String json = Files.readString(file, StandardCharsets.UTF_8);
                PlayerData loaded = GSON.fromJson(json, PlayerData.class);
                if (loaded != null) {
                    loaded.playerId = id;
                    return loaded;
                }
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                System.err.println("[Ascension] Failed to load player data for " + id + ": " + e.getMessage()
                        + " — starting fresh rather than crashing.");
            }
        }
        return new PlayerData(id);
    }

    public void save(PlayerData data) {
        if (storageDir == null) return;
        Path file = fileFor(data.playerId);
        try {
            Files.writeString(file, GSON.toJson(data), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[Ascension] Failed to save player data for " + data.playerId + ": " + e.getMessage());
        }
    }

    public void saveAndUnload(ServerPlayerEntity player) {
        PlayerData data = cache.remove(player.getUuid());
        if (data != null) {
            save(data);
        }
    }

    public void saveAll() {
        cache.values().forEach(this::save);
    }

    private Path fileFor(UUID id) {
        return storageDir.resolve(id + ".json");
    }
}
