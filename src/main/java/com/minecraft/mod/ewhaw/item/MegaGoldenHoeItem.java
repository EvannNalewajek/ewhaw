package com.minecraft.mod.ewhaw.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MegaGoldenHoeItem extends MegaHoeItem {
    public MegaGoldenHoeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        // Si on clique sur une plante qui est à son stade de croissance maximum
        if (state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(state)) {
            if (!level.isClientSide) {
                harvestAndReplant3x3(context, (ServerLevel) level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Sinon, on utilise le comportement normal de la Mega Hoe (labourer 3x3)
        return super.useOn(context);
    }

    private void harvestAndReplant3x3(UseOnContext context, ServerLevel level, BlockPos centerPos) {
        Player player = context.getPlayer();
        ItemStack hoe = context.getItemInHand();
        boolean performedAction = false;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos targetPos = centerPos.offset(x, 0, z);
                BlockState state = level.getBlockState(targetPos);

                if (state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(state)) {
                    // 1. Récupérer les loots de la plante
                    List<ItemStack> drops = Block.getDrops(state, level, targetPos, level.getBlockEntity(targetPos), player, hoe);
                    
                    // 2. Trouver l'item qui sert de graine pour cette plante
                    // getCloneItemStack renvoie généralement la graine/item de plantation (patate, graine de blé, etc.)
                    Item seedItem = cropBlock.getCloneItemStack(level, targetPos, state).getItem();

                    boolean seedConsumed = false;
                    for (ItemStack drop : drops) {
                        if (drop.is(seedItem)) {
                            drop.shrink(1); // On "consomme" une graine du butin
                            seedConsumed = true;
                            break;
                        }
                    }

                    // 3. Si on a pu consommer une graine, on replante
                    if (seedConsumed) {
                        level.setBlock(targetPos, cropBlock.getStateForAge(0), 3);
                        // Faire apparaître les autres loots au sol
                        for (ItemStack drop : drops) {
                            if (!drop.isEmpty()) {
                                Block.popResource(level, targetPos, drop);
                            }
                        }
                        
                        if (player != null) {
                            hoe.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
                        }
                        performedAction = true;
                    }
                }
            }
        }

        if (performedAction) {
            level.playSound(null, centerPos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.playSound(null, centerPos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}
