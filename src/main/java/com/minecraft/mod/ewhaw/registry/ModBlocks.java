package com.minecraft.mod.ewhaw.registry;

import com.minecraft.mod.ewhaw.EverythingWeHaveAlwaysWanted;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.minecraft.mod.ewhaw.block.MortarBlock;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(EverythingWeHaveAlwaysWanted.MODID);

    public static final DeferredBlock<Block> MORTAR =
            BLOCKS.register("mortar",
                    () -> new MortarBlock(
                            BlockBehaviour.Properties.of()
                                    .strength(5.0f, 6.0f)
                                    .sound(SoundType.METAL)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion()
                    ));

    public static final DeferredBlock<Block> MEGA_IRON_BLOCK =
            BLOCKS.register("mega_iron_block",
                    () -> new Block(
                            BlockBehaviour.Properties.of()
                                    .strength(10.0f, 12.0f)
                                    .sound(SoundType.METAL)
                                    .requiresCorrectToolForDrops()
                    ));
    public static final DeferredBlock<Block> MEGA_GOLD_BLOCK =
            BLOCKS.register("mega_gold_block",
                    () -> new Block(
                            BlockBehaviour.Properties.of()
                                    .strength(10.0f, 12.0f)
                                    .sound(SoundType.METAL)
                                    .requiresCorrectToolForDrops()
                    ));

    public static final DeferredBlock<Block> MEGA_DIAMOND_BLOCK =
            BLOCKS.register("mega_diamond_block",
                    () -> new Block(
                            BlockBehaviour.Properties.of()
                                    .strength(12.0f, 15.0f)
                                    .sound(SoundType.METAL)
                                    .requiresCorrectToolForDrops()
                    ));

    public static final DeferredBlock<Block> MEGA_NETHERITE_BLOCK =
            BLOCKS.register("mega_netherite_block",
                    () -> new Block(
                            BlockBehaviour.Properties.of()
                                    .strength(50.0f, 1200.0f)
                                    .sound(SoundType.METAL)
                                    .requiresCorrectToolForDrops()
                    ));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}