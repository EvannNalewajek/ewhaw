package com.minecraft.mod.ewhaw.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MegaPickaxeItem extends PickaxeItem {
    // Ce flag empêche la récursion infinie (StackOverflow)
    private static final ThreadLocal<Boolean> MINING_ZONE = ThreadLocal.withInitial(() -> false);

    public MegaPickaxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public int getEnchantmentValue() {
        return 14;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        // Si on est déjà en train de miner une zone, on arrête la récursion ici
        if (MINING_ZONE.get()) {
            return super.mineBlock(stack, level, state, pos, entity);
        }

        if (!level.isClientSide && entity instanceof Player player) {
            try {
                // On active le verrou
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

                        // Correction : On gère manuellement la destruction pour inclure les enchantements
                        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            // 1. Faire tomber les ressources en tenant compte des enchantements (Fortune/Silk Touch)
                            net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(targetPos);
                            net.minecraft.world.level.block.Block.dropResources(targetState, level, targetPos, blockEntity, player, stack);
                            
                            // 2. Retirer le bloc
                            level.removeBlock(targetPos, false);
                            
                            // 3. Appliquer l'usure à l'outil
                            stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                        }
                    }
                }
            } finally {
                // On libère toujours le verrou, même en cas d'erreur
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
