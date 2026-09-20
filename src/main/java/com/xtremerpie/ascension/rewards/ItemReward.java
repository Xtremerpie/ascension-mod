package com.xtremerpie.ascension.rewards;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Grants a vanilla item via the player's real inventory API
 * ({@code giveItemStack}), which respects normal stacking/overflow rules
 * (overflow drops at the player's feet, same as vanilla loot) rather than
 * bypassing them (spec section 16).
 */
public final class ItemReward implements Reward {

    private final String id;
    private final Identifier itemId;
    private final int count;

    public ItemReward(String id, Identifier itemId, int count) {
        this.id = id;
        this.itemId = itemId;
        this.count = count;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public RewardResult apply(ServerPlayerEntity player) {
        Item item = Registries.ITEM.get(itemId);
        if (item == null) {
            return RewardResult.failed("Unknown item id: " + itemId);
        }
        ItemStack stack = new ItemStack(item, count);
        player.giveItemStack(stack);
        return RewardResult.ok("+" + count + " " + itemId.getPath());
    }
}
