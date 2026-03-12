package com.minecraft.mod.ewhaw.registry;

import com.minecraft.mod.ewhaw.EverythingWeHaveAlwaysWanted;

import com.minecraft.mod.ewhaw.item.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.world.level.material.Fluids;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(EverythingWeHaveAlwaysWanted.MODID);

    public static final DeferredItem<Item> MEGA_BUCKET =
            ITEMS.register("mega_bucket",
                    () -> new MegaBucketItem(() -> Fluids.EMPTY, new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> MEGA_WATER_BUCKET =
            ITEMS.register("mega_water_bucket",
                    () -> new MegaBucketItem(() -> Fluids.WATER, new Item.Properties().craftRemainder(MEGA_BUCKET.get()).stacksTo(1)));

    public static final DeferredItem<Item> MEGA_LAVA_BUCKET =
            ITEMS.register("mega_lava_bucket",
                    () -> new MegaBucketItem(() -> Fluids.LAVA, new Item.Properties().craftRemainder(MEGA_BUCKET.get()).stacksTo(1)));

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
                            new Item.Properties().attributes(MegaPickaxeItem.createAttributes(ModToolTiers.MEGA_IRON, 1.0F, -2.8F))
                    ));

    public static final DeferredItem<Item> MEGA_GOLD_PICKAXE =
            ITEMS.register("mega_gold_pickaxe",
                    () -> new MegaGoldPickaxeItem(
                            ModToolTiers.MEGA_GOLD,
                            new Item.Properties().attributes(MegaGoldPickaxeItem.createAttributes(ModToolTiers.MEGA_GOLD, 1.0F, -2.8F))
                    ));

    public static final DeferredItem<Item> MEGA_DIAMOND_PICKAXE =
            ITEMS.register("mega_diamond_pickaxe",
                    () -> new MegaPickaxeItem(
                            ModToolTiers.MEGA_DIAMOND,
                            new Item.Properties().attributes(MegaPickaxeItem.createAttributes(ModToolTiers.MEGA_DIAMOND, 1.0F, -2.8F))
                    ));

    public static final DeferredItem<Item> MEGA_NETHERITE_PICKAXE =
            ITEMS.register("mega_netherite_pickaxe",
                    () -> new MegaPickaxeItem(
                            ModToolTiers.MEGA_NETHERITE,
                            new Item.Properties().fireResistant().attributes(MegaPickaxeItem.createAttributes(ModToolTiers.MEGA_NETHERITE, 1.0F, -2.8F))
                    ));

    public static final DeferredItem<Item> MEGA_IRON_SHOVEL =
            ITEMS.register("mega_iron_shovel",
                    () -> new MegaShovelItem(
                            ModToolTiers.MEGA_IRON,
                            new Item.Properties().attributes(MegaShovelItem.createAttributes(ModToolTiers.MEGA_IRON, 1.5F, -3.0F))
                    ));

    public static final DeferredItem<Item> MEGA_GOLDEN_SHOVEL =
            ITEMS.register("mega_golden_shovel",
                    () -> new MegaGoldenShovelItem(
                            ModToolTiers.MEGA_GOLD,
                            new Item.Properties().attributes(MegaGoldenShovelItem.createAttributes(ModToolTiers.MEGA_GOLD, 1.5F, -3.0F))
                    ));

    public static final DeferredItem<Item> MEGA_DIAMOND_SHOVEL =
            ITEMS.register("mega_diamond_shovel",
                    () -> new MegaShovelItem(
                            ModToolTiers.MEGA_DIAMOND,
                            new Item.Properties().attributes(MegaShovelItem.createAttributes(ModToolTiers.MEGA_DIAMOND, 1.5F, -3.0F))
                    ));

    public static final DeferredItem<Item> MEGA_NETHERITE_SHOVEL =
            ITEMS.register("mega_netherite_shovel",
                    () -> new MegaShovelItem(
                            ModToolTiers.MEGA_NETHERITE,
                            new Item.Properties().fireResistant().attributes(MegaShovelItem.createAttributes(ModToolTiers.MEGA_NETHERITE, 1.5F, -3.0F))
                    ));

    public static final DeferredItem<Item> MEGA_IRON_HOE =
            ITEMS.register("mega_iron_hoe",
                    () -> new MegaHoeItem(
                            ModToolTiers.MEGA_IRON,
                            new Item.Properties().attributes(MegaHoeItem.createAttributes(ModToolTiers.MEGA_IRON, -2.0F, -1.0F))
                    ));

    public static final DeferredItem<Item> MEGA_GOLDEN_HOE =
            ITEMS.register("mega_golden_hoe",
                    () -> new MegaGoldenHoeItem(
                            ModToolTiers.MEGA_GOLD,
                            new Item.Properties().attributes(MegaGoldenHoeItem.createAttributes(ModToolTiers.MEGA_GOLD, -2.0F, -1.0F))
                    ));

    public static final DeferredItem<Item> MEGA_DIAMOND_HOE =
            ITEMS.register("mega_diamond_hoe",
                    () -> new MegaHoeItem(
                            ModToolTiers.MEGA_DIAMOND,
                            new Item.Properties().attributes(MegaHoeItem.createAttributes(ModToolTiers.MEGA_DIAMOND, -2.0F, -1.0F))
                    ));

    public static final DeferredItem<Item> MEGA_NETHERITE_HOE =
            ITEMS.register("mega_netherite_hoe",
                    () -> new MegaHoeItem(
                            ModToolTiers.MEGA_NETHERITE,
                            new Item.Properties().fireResistant().attributes(MegaHoeItem.createAttributes(ModToolTiers.MEGA_NETHERITE, -2.0F, -1.0F))
                    ));

    public static final DeferredItem<Item> MEGA_MAGNET =
            ITEMS.register("mega_magnet",
                    () -> new MegaMagnetItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<BlockItem> MORTAR_ITEM =
            ITEMS.registerSimpleBlockItem("mortar", ModBlocks.MORTAR);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}