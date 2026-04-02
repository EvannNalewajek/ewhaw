package com.minecraft.mod.ewhaw.registry;

import com.minecraft.mod.ewhaw.EverythingWeHaveAlwaysWanted;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EverythingWeHaveAlwaysWanted.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EWHAW_TAB =
            CREATIVE_MODE_TABS.register("ewhaw_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.everythingwehavealwayswanted.ewhaw_tab"))
                    .icon(() -> new ItemStack(ModItems.MEGA_IRON_PICKAXE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.MEGA_IRON_BLOCK_ITEM.get());
                        output.accept(ModItems.MEGA_GOLD_BLOCK_ITEM.get());
                        output.accept(ModItems.MEGA_DIAMOND_BLOCK_ITEM.get());
                        output.accept(ModItems.MEGA_NETHERITE_BLOCK_ITEM.get());

                        output.accept(ModItems.MEGA_IRON_PICKAXE.get());
                        output.accept(ModItems.MEGA_GOLD_PICKAXE.get());
                        output.accept(ModItems.MEGA_DIAMOND_PICKAXE.get());
                        output.accept(ModItems.MEGA_NETHERITE_PICKAXE.get());

                        output.accept(ModItems.MEGA_IRON_SHOVEL.get());
                        output.accept(ModItems.MEGA_GOLDEN_SHOVEL.get());
                        output.accept(ModItems.MEGA_DIAMOND_SHOVEL.get());
                        output.accept(ModItems.MEGA_NETHERITE_SHOVEL.get());

                        output.accept(ModItems.MEGA_IRON_HOE.get());
                        output.accept(ModItems.MEGA_GOLDEN_HOE.get());
                        output.accept(ModItems.MEGA_DIAMOND_HOE.get());
                        output.accept(ModItems.MEGA_NETHERITE_HOE.get());

                        output.accept(ModItems.MEGA_IRON_AXE.get());
                        output.accept(ModItems.MEGA_GOLDEN_AXE.get());
                        output.accept(ModItems.MEGA_DIAMOND_AXE.get());
                        output.accept(ModItems.MEGA_NETHERITE_AXE.get());

                        output.accept(ModItems.MEGA_MAGNET.get());
                        output.accept(ModItems.MEGA_BUCKET.get());
                        output.accept(ModItems.MEGA_WATER_BUCKET.get());
                        output.accept(ModItems.MEGA_LAVA_BUCKET.get());
                        output.accept(ModItems.MORTAR_ITEM.get());
                        output.accept(ModItems.ADVENTURER_SPAWN_EGG.get());
                        output.accept(ModItems.HUMAN_SPAWN_EGG.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}