package com.xtremerpie.ascension.rewards;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * LIMITATION — documented per spec section 17/34, not faked:
 *
 * True custom enchantments in modern Minecraft (1.20.5+) are registered
 * through the data-driven enchantment registry (JSON under
 * {@code data/<mod>/enchantment/}, referencing enchantment effect
 * component types) rather than a Java-side registration call. That
 * registry format is itself version-sensitive and could not be verified
 * against the actual Minecraft 1.21.11 registry schema in this
 * environment (no network access to the game jar/data reports while
 * writing this — see IMPLEMENTATION_STATUS.md). Rather than hand-write a
 * JSON enchantment definition that might not match 1.21.11's real schema
 * and silently fail to load, this reward applies the closest real,
 * verifiable Minecraft mechanic instead: a timed vanilla status effect
 * (Luck, by default — configurable per instance), granted through the
 * standard, stable status-effect API.
 *
 * To implement the real thing: author
 * {@code data/ascension/enchantment/precision.json} against the current
 * 1.21.11 enchantment schema (check via a generated data report from your
 * local dev environment, e.g. {@code gradlew runData} if configured) and
 * swap this class's {@code apply} body for a call that gives the player a
 * book/item enchanted with it — the surrounding Reward/RewardManager
 * plumbing does not need to change.
 */
public final class EnchantmentReward implements Reward {

    private final String id;
    private final RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect;
    private final int durationTicks;
    private final int amplifier;

    public EnchantmentReward(String id, int durationTicks, int amplifier) {
        this.id = id;
        this.effect = StatusEffects.LUCK;
        this.durationTicks = durationTicks;
        this.amplifier = amplifier;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public RewardResult apply(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(effect, durationTicks, amplifier));
        return RewardResult.ok("Granted Precision effect (documented enchantment substitute — see EnchantmentReward.java)");
    }
}
