package com.minecraft.mod.ewhaw.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MegaShovelItem extends ShovelItem {

    public MegaShovelItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public int getEnchantmentValue() {
        return 14;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            float pitch = player.getXRot();
            Direction face = player.getDirection();

            for (int a = -2; a <= 2; a++) {
                for (int b = -2; b <= 2; b++) {
                    BlockPos targetPos;

                    if (pitch > 40 || pitch < -40) {
                        targetPos = pos.offset(a, 0, b);
                    } else if (face == Direction.NORTH || face == Direction.SOUTH) {
                        targetPos = pos.offset(a, b, 0);
                    } else {
                        targetPos = pos.offset(0, b, a);
                    }

                    if (targetPos.equals(pos)) {
                        continue;
                    }

                    BlockState targetState = level.getBlockState(targetPos);

                    if (shouldBreakExtraBlock(level, stack, state, targetState, targetPos)) {
                        continue;
                    }

                    boolean destroyed = level.destroyBlock(targetPos, true, player);
                    if (destroyed) {
                        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                    }
                }
            }
        }

        return super.mineBlock(stack, level, state, pos, entity);
    }

    boolean shouldBreakExtraBlock(
            Level level,
            ItemStack stack,
            BlockState originState,
            BlockState targetState,
            BlockPos targetPos
    ) {
        if (targetState.isAir()) {
            return true;
        }

        // blocs incassables
        if (targetState.getDestroySpeed(level, targetPos) < 0.0F) {
            return true;
        }

        // évite de casser des blocs "hors outil"
        if (!stack.getItem().isCorrectToolForDrops(stack, targetState)) {
            return true;
        }

        // évite les blocs que la pelle casse lentement / anormalement
        if (this.getDestroySpeed(stack, targetState) <= 1.0F) {
            return true;
        }

        // garde seulement le même type de bloc que celui frappé
        return !targetState.is(originState.getBlock());
    }
}
