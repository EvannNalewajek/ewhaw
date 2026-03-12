package com.minecraft.mod.ewhaw.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MegaGoldPickaxeItem extends MegaPickaxeItem {
    private static final ThreadLocal<Boolean> MINING_ZONE = ThreadLocal.withInitial(() -> false);

    public MegaGoldPickaxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public int getEnchantmentValue() {
        return 22;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (MINING_ZONE.get()) {
            return super.mineBlock(stack, level, state, pos, entity);
        }

        if (!level.isClientSide && entity instanceof Player player) {
            try {
                MINING_ZONE.set(true);
                mineHugeArea(stack, level, state, pos, player);
                stack.hurtAndBreak(50, player, EquipmentSlot.MAINHAND);
            } finally {
                MINING_ZONE.set(false);
            }
        }

        return super.mineBlock(stack, level, state, pos, entity);
    }

    private void mineHugeArea(ItemStack stack, Level level, BlockState originState, BlockPos originPos, Player player) {
        float pitch = player.getXRot();
        Direction face = player.getDirection();

        for (int depth = 0; depth < 5; depth++) {
            for (int a = -2; a <= 2; a++) {
                for (int b = -2; b <= 2; b++) {
                    BlockPos targetPos;
                    if (pitch > 40) {
                        targetPos = originPos.offset(a, -depth, b);
                    } else if (pitch < -40) {
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

                    if (targetPos.equals(originPos)) continue;

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
