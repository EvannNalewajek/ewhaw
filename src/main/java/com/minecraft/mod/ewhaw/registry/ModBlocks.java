package com.minecraft.mod.ewhaw.registry;

import com.minecraft.mod.ewhaw.EverythingWeHaveAlwaysWanted;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(EverythingWeHaveAlwaysWanted.MODID);

    public static final DeferredBlock<Block> MEGA_IRON_BLOCK =
            BLOCKS.register("mega_iron_block",
                    () -> new Block(
                            BlockBehaviour.Properties.of()
                                    .strength(10.0f, 12.0f)
                                    .sound(SoundType.METAL)
                                    .requiresCorrectToolForDrops()
                    ));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}