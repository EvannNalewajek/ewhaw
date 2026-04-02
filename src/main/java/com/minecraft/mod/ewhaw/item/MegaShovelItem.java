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
    private static final ThreadLocal<Boolean> MINING_ZONE = ThreadLocal.withInitial(() -> false);

    public MegaShovelItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public int getEnchantmentValue() {
        return 14;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (MINING_ZONE.get()) {
            return super.mineBlock(stack, level, state, pos, entity);
        }

        if (!level.isClientSide && entity instanceof Player player) {
            try {
                MINING_ZONE.set(true);
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

                        if (level instanceof net.minecraft.server.level.ServerLevel) {
                            net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(targetPos);
                            net.minecraft.world.level.block.Block.dropResources(targetState, level, targetPos, blockEntity, player, stack);
                            level.removeBlock(targetPos, false);
                            stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                        }
                    }
                }
            } finally {
                MINING_ZONE.set(false);
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

        if (targetState.getDestroySpeed(level, targetPos) < 0.0F) {
            return true;
        }

        if (!stack.getItem().isCorrectToolForDrops(stack, targetState)) {
            return true;
        }

        if (this.getDestroySpeed(stack, targetState) <= 1.0F) {
            return true;
        }

        return !targetState.is(originState.getBlock());
    }
}
