package com.minecraft.mod.ewhaw.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MegaGoldPickaxeItem extends MegaPickaxeItem {

    public MegaGoldPickaxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public int getEnchantmentValue() {
        return 22;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {

            mineHugeArea(stack, level, state, pos, player);

            stack.setDamageValue(stack.getDamageValue() + 248);

            return super.mineBlock(stack, level, state, pos, entity);
        }

        return super.mineBlock(stack, level, state, pos, entity);
    }

    private void mineHugeArea(ItemStack stack, Level level, BlockState originState, BlockPos originPos, Player player) {
        float pitch = player.getXRot();
        Direction face = player.getDirection();

        for (int depth = 0; depth < 9; depth++) {
            for (int a = -4; a <= 4; a++) {
                for (int b = -4; b <= 4; b++) {
                    BlockPos targetPos;
                    if (pitch > 40) {
                        // regarder vers le bas
                        targetPos = originPos.offset(a, -depth, b);
                    } else if (pitch < -40) {
                        // regarder vers le haut
                        targetPos = originPos.offset(a, depth, b);
                    } else if (face == Direction.NORTH) {
                        targetPos = originPos.offset(a, b, -depth);
                    } else if (face == Direction.SOUTH) {
                        targetPos = originPos.offset(a, b, depth);
                    } else if (face == Direction.EAST) {
                        targetPos = originPos.offset(depth, b, a);
                    } else {
                        targetPos = originPos.offset(-depth, b, a);
                    }

                    BlockState targetState = level.getBlockState(targetPos);

                    if (shouldBreakExtraBlock(level, stack, originState, targetState, targetPos)) {
                        continue;
                    }

                    level.destroyBlock(targetPos, true, player);
                }
            }
        }
    }
}