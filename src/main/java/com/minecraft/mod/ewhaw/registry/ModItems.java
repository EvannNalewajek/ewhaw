package com.minecraft.mod.ewhaw.registry;

import com.minecraft.mod.ewhaw.EverythingWeHaveAlwaysWanted;

import com.minecraft.mod.ewhaw.item.MegaGoldPickaxeItem;
import com.minecraft.mod.ewhaw.item.MegaPickaxeItem;
import com.minecraft.mod.ewhaw.item.ModToolTiers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(EverythingWeHaveAlwaysWanted.MODID);

    public static final DeferredItem<BlockItem> MEGA_IRON_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("mega_iron_block", ModBlocks.MEGA_IRON_BLOCK);

    public static final DeferredItem<BlockItem> MEGA_GOLD_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("mega_gold_block", ModBlocks.MEGA_GOLD_BLOCK);

    public static final DeferredItem<BlockItem> MEGA_DIAMOND_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("mega_diamond_block", ModBlocks.MEGA_DIAMOND_BLOCK);

    public static final DeferredItem<BlockItem> MEGA_NETHERITE_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("mega_netherite_block", ModBlocks.MEGA_NETHERITE_BLOCK);

    public static final DeferredItem<Item> MEGA_IRON_PICKAXE =
            ITEMS.register("mega_iron_pickaxe",
                    () -> new MegaPickaxeItem(
                            ModToolTiers.MEGA_IRON,
                            new Item.Properties()
                    ));

    public static final DeferredItem<Item> MEGA_GOLD_PICKAXE =
            ITEMS.register("mega_gold_pickaxe",
                    () -> new MegaGoldPickaxeItem(
                            ModToolTiers.MEGA_GOLD,
                            new Item.Properties()
                    ));

    public static final DeferredItem<Item> MEGA_DIAMOND_PICKAXE =
            ITEMS.register("mega_diamond_pickaxe",
                    () -> new MegaPickaxeItem(
                            ModToolTiers.MEGA_DIAMOND,
                            new Item.Properties()
                    ));

    public static final DeferredItem<Item> MEGA_NETHERITE_PICKAXE =
            ITEMS.register("mega_netherite_pickaxe",
                    () -> new MegaPickaxeItem(
                            ModToolTiers.MEGA_NETHERITE,
                            new Item.Properties()
                    ));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}