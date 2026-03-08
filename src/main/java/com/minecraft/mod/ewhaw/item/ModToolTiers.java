package com.minecraft.mod.ewhaw.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public class ModToolTiers {

    public static final Tier MEGA_IRON = new SimpleTier(
            BlockTags.INCORRECT_FOR_IRON_TOOL,
            500,
            6.0F,
            2.0F,
            14,
            () -> Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT)
    );

    public static final Tier MEGA_GOLD = new SimpleTier(
            BlockTags.INCORRECT_FOR_GOLD_TOOL,
            250,
            12.0F,
            0.0F,
            22,
            () -> Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT)
    );

    public static final Tier MEGA_DIAMOND = new SimpleTier(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            3122,
            8.0F,
            3.0F,
            10,
            () -> Ingredient.of(net.minecraft.world.item.Items.DIAMOND)
    );

    public static final Tier MEGA_NETHERITE = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            4062,
            9.0F,
            4.0F,
            15,
            () -> Ingredient.of(net.minecraft.world.item.Items.NETHERITE_INGOT)
    );
}