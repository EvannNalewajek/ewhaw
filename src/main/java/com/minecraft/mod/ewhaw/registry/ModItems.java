package com.minecraft.mod.ewhaw.registry;

import com.minecraft.mod.ewhaw.EverythingWeHaveAlwaysWanted;

import com.minecraft.mod.ewhaw.item.MegaIronPickaxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(EverythingWeHaveAlwaysWanted.MODID);

    public static final DeferredItem<BlockItem> MEGA_IRON_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("mega_iron_block", ModBlocks.MEGA_IRON_BLOCK);

    public static final DeferredItem<Item> MEGA_IRON_PICKAXE =
            ITEMS.register("mega_iron_pickaxe",
                    () -> new MegaIronPickaxeItem(
                            Tiers.IRON,
                            new Item.Properties()
                                    .durability(500)
                    ));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}