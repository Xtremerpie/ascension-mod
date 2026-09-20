package com.xtremerpie.ascension.rewards;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.xtremerpie.ascension.core.AscensionConstants;
import com.xtremerpie.ascension.persistence.PlayerDataManager;
import com.xtremerpie.ascension.progression.AscensionManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads reward definitions from {@code data/ascension/rewards/*.json} (an
 * index-file scheme identical to {@link com.xtremerpie.ascension.achievements.AchievementRegistry},
 * see that class for why) and turns each into a concrete {@link Reward}.
 *
 * Each JSON file has one flattened schema with a "type" discriminator
 * (item / experience / progression / structure / enchantment) and only
 * the fields relevant to that type populated — this avoids needing a
 * custom Gson polymorphic TypeAdapter for a handful of reward kinds.
 */
public final class RewardManager {

    private static final String RESOURCE_ROOT = "data/" + AscensionConstants.MOD_ID + "/rewards/";
    private static final Gson GSON = new GsonBuilder().create();

    private record RewardConfig(String id, String type, String itemId, Integer count,
                                 Integer xpAmount, Double ascensionXpAmount, String blueprintId,
                                 Integer durationTicks, Integer amplifier) {
    }

    private final Map<String, Reward> rewards = new LinkedHashMap<>();
    private final PlayerDataManager playerDataManager;
    private final AscensionManager ascensionManager;

    public RewardManager(PlayerDataManager playerDataManager, AscensionManager ascensionManager) {
        this.playerDataManager = playerDataManager;
        this.ascensionManager = ascensionManager;
    }

    public void loadAll(ClassLoader classLoader) {
        rewards.clear();
        try (InputStream indexStream = classLoader.getResourceAsStream(RESOURCE_ROOT + "index.json")) {
            if (indexStream == null) {
                System.err.println("[Ascension] Reward index.json not found — no rewards loaded.");
                return;
            }
            JsonArray fileNames = GSON.fromJson(new InputStreamReader(indexStream, StandardCharsets.UTF_8), JsonArray.class);
            for (var element : fileNames) {
                loadOne(classLoader, element.getAsString());
            }
        } catch (IOException e) {
            System.err.println("[Ascension] Failed reading reward index: " + e.getMessage());
        }
    }

    private void loadOne(ClassLoader classLoader, String fileName) {
        try (InputStream stream = classLoader.getResourceAsStream(RESOURCE_ROOT + fileName)) {
            if (stream == null) return;
            RewardConfig cfg = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), RewardConfig.class);
            if (cfg == null || cfg.id() == null || cfg.type() == null) {
                System.err.println("[Ascension] Malformed reward file skipped: " + fileName);
                return;
            }
            Reward reward = build(cfg);
            if (reward != null) {
                rewards.put(reward.id(), reward);
            }
        } catch (IOException e) {
            System.err.println("[Ascension] Failed to parse reward " + fileName + ": " + e.getMessage());
        }
    }

    private Reward build(RewardConfig cfg) {
        return switch (cfg.type()) {
            case "item" -> new ItemReward(cfg.id(), Identifier.of(cfg.itemId()), cfg.count() != null ? cfg.count() : 1);
            case "experience" -> new ExperienceReward(cfg.id(), cfg.xpAmount() != null ? cfg.xpAmount() : 0);
            case "progression" -> new ProgressionReward(cfg.id(), cfg.ascensionXpAmount() != null ? cfg.ascensionXpAmount() : 0, ascensionManager);
            case "structure" -> new StructureReward(cfg.id(), cfg.blueprintId(), playerDataManager);
            case "enchantment" -> new EnchantmentReward(cfg.id(),
                    cfg.durationTicks() != null ? cfg.durationTicks() : 6000,
                    cfg.amplifier() != null ? cfg.amplifier() : 0);
            default -> {
                System.err.println("[Ascension] Unknown reward type: " + cfg.type());
                yield null;
            }
        };
    }

    /** Applies every reward id in the list, skipping (and logging) any that fail rather than aborting the batch. */
    public void grantAll(ServerPlayerEntity player, List<String> rewardIds) {
        if (rewardIds == null) return;
        for (String id : rewardIds) {
            Reward reward = rewards.get(id);
            if (reward == null) {
                System.err.println("[Ascension] Unknown reward id referenced: " + id);
                continue;
            }
            RewardResult result = reward.apply(player);
            if (!result.success()) {
                System.err.println("[Ascension] Reward " + id + " failed: " + result.summary());
            }
        }
    }

    public int count() {
        return rewards.size();
    }
}
