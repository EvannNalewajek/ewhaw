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
                            new Item.Properties().attributes(MegaGoldenHoeItem.createAttributes(ModToolTiers.MEGA_GOLD, 0.0F, -1.0F))
                    ));

    public static final DeferredItem<Item> MEGA_DIAMOND_HOE =
            ITEMS.register("mega_diamond_hoe",
                    () -> new MegaHoeItem(
                            ModToolTiers.MEGA_DIAMOND,
                            new Item.Properties().attributes(MegaHoeItem.createAttributes(ModToolTiers.MEGA_DIAMOND, -3.0F, 0.0F))
                    ));

    public static final DeferredItem<Item> MEGA_NETHERITE_HOE =
            ITEMS.register("mega_netherite_hoe",
                    () -> new MegaHoeItem(
                            ModToolTiers.MEGA_NETHERITE,
                            new Item.Properties().fireResistant().attributes(MegaHoeItem.createAttributes(ModToolTiers.MEGA_NETHERITE, -4.0F, 0.0F))
                    ));

    public static final DeferredItem<Item> MEGA_IRON_AXE =
            ITEMS.register("mega_iron_axe",
                    () -> new MegaAxeItem(
                            ModToolTiers.MEGA_IRON,
                            new Item.Properties().attributes(MegaAxeItem.createAttributes(ModToolTiers.MEGA_IRON, 6.0F, -3.0F))
                    ));

    public static final DeferredItem<Item> MEGA_GOLDEN_AXE =
            ITEMS.register("mega_golden_axe",
                    () -> new MegaGoldAxeItem(
                            ModToolTiers.MEGA_GOLD,
                            new Item.Properties().attributes(MegaGoldAxeItem.createAttributes(ModToolTiers.MEGA_GOLD, 6.0F, -3.0F))
                    ));

    public static final DeferredItem<Item> MEGA_DIAMOND_AXE =
            ITEMS.register("mega_diamond_axe",
                    () -> new MegaAxeItem(
                            ModToolTiers.MEGA_DIAMOND,
                            new Item.Properties().attributes(MegaAxeItem.createAttributes(ModToolTiers.MEGA_DIAMOND, 5.0F, -3.0F))
                    ));

    public static final DeferredItem<Item> MEGA_NETHERITE_AXE =
            ITEMS.register("mega_netherite_axe",
                    () -> new MegaAxeItem(
                            ModToolTiers.MEGA_NETHERITE,
                            new Item.Properties().fireResistant().attributes(MegaAxeItem.createAttributes(ModToolTiers.MEGA_NETHERITE, 5.0F, -3.0F))
                    ));

    public static final DeferredItem<Item> INVERTED_BOW =
            ITEMS.register("inverted_bow",
                    () -> new InvertedBowItem(new Item.Properties().stacksTo(1).durability(384)));

    public static final DeferredItem<Item> MEGA_MAGNET =
            ITEMS.register("mega_magnet",
                    () -> new MegaMagnetItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> ADVENTURER_SPAWN_EGG =
            ITEMS.register("adventurer_spawn_egg",
                    () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(ModEntityTypes.ADVENTURER, 0x3498db, 0xFFD700, new Item.Properties()));

    public static final DeferredItem<Item> HUMAN_SPAWN_EGG =
            ITEMS.register("human_spawn_egg",
                    () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(ModEntityTypes.HUMAN, 0x95a5a6, 0x34495e, new Item.Properties()));

    public static final DeferredItem<BlockItem> MORTAR_ITEM =
            ITEMS.registerSimpleBlockItem("mortar", ModBlocks.MORTAR);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}