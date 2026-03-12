package com.minecraft.mod.ewhaw.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jetbrains.annotations.NotNull;

public class MegaHoeItem extends HoeItem {
    public MegaHoeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        
        if (context.getClickedFace() == Direction.DOWN) {
            return InteractionResult.PASS;
        }

        boolean success = false;
        // On parcourt une zone de 3x3 centrée sur le bloc cliqué
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos targetPos = pos.offset(x, 0, z);
                
                // On vérifie si le bloc peut être transformé par une houe
                BlockState tilledState = level.getBlockState(targetPos).getToolModifiedState(context, ItemAbilities.HOE_TILL, false);
                
                if (tilledState != null) {
                    BlockPos abovePos = targetPos.above();
                    BlockState aboveState = level.getBlockState(abovePos);

                    // Si le bloc au-dessus n'est ni de l'air, ni un bloc remplaçable (herbe/fleur)
                    // Alors on ne peut pas labourer ici.
                    if (!aboveState.isAir() && !aboveState.is(BlockTags.REPLACEABLE)) {
                        continue;
                    }

                    if (!level.isClientSide) {
                        // Si c'est de la végétation remplaçable, on la nettoie
                        if (aboveState.is(BlockTags.REPLACEABLE)) {
                            level.destroyBlock(abovePos, true, player);
                        }

                        level.setBlock(targetPos, tilledState, 11);
                        if (player != null) {
                            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
                        }
                    }
                    success = true;
                }
            }
        }

        if (success) {
            level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }
}
