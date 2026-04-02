package com.minecraft.mod.ewhaw.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class MegaAxeItem extends AxeItem {
    // Ce flag empêche la récursion infinie
    protected static final ThreadLocal<Boolean> FELLING_ZONE = ThreadLocal.withInitial(() -> false);

    public MegaAxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        // Si on est déjà en train d'abattre l'arbre, on arrête la récursion ici
        if (FELLING_ZONE.get()) {
            return true;
        }

        if (!level.isClientSide && entity instanceof Player player && state.is(BlockTags.LOGS)) {
            try {
                // On active le verrou
                FELLING_ZONE.set(true);
                
                // stats[0] = nombre de blocs, stats[1] = maxY atteint
                int[] stats = new int[]{0, pos.getY()};
                
                fellTree(stack, level, state, pos, player, stats);
                
                // On applique les dégâts à la fin
                applyCappedDamage(stack, player, stats[0]);
                
                return true; // On indique que le cassage est entièrement géré
            } finally {
                // On libère toujours le verrou
                FELLING_ZONE.set(false);
            }
        }

        return super.mineBlock(stack, level, state, pos, entity);
    }

    protected void fellTree(ItemStack stack, Level level, BlockState originState, BlockPos originPos, Player player, int[] stats) {
        int maxBlocks = getFellingLimit();
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        
        queue.add(originPos);
        visited.add(originPos);
        
        // On casse et compte le bloc initial
        breakExtraBlock(level, originPos, originState, player, stack);
        stats[0] = 1;

        while (!queue.isEmpty() && stats[0] < maxBlocks) {
            BlockPos current = queue.poll();
            
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        
                        BlockPos targetPos = current.offset(x, y, z);
                        if (visited.contains(targetPos)) continue;
                        visited.add(targetPos);

                        BlockState targetState = level.getBlockState(targetPos);
                        if (isValidLog(targetState)) {
                            queue.add(targetPos);
                            breakExtraBlock(level, targetPos, targetState, player, stack);
                            stats[0]++;
                            
                            // Mise à jour du maxY pour le nettoyage des feuilles
                            if (targetPos.getY() > stats[1]) {
                                stats[1] = targetPos.getY();
                            }
                            
                            if (stats[0] >= maxBlocks) break;
                        }
                    }
                    if (stats[0] >= maxBlocks) break;
                }
                if (stats[0] >= maxBlocks) break;
            }
        }
    }

    protected boolean isValidLog(BlockState state) {
        return state.is(BlockTags.LOGS);
    }

    protected void breakExtraBlock(Level level, BlockPos pos, BlockState state, Player player, ItemStack stack) {
        if (level instanceof net.minecraft.server.level.ServerLevel) {
            net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(pos);
            net.minecraft.world.level.block.Block.dropResources(state, level, pos, blockEntity, player, stack);
            level.removeBlock(pos, false);
        }
    }

    protected void applyCappedDamage(ItemStack stack, Player player, int damage) {
        if (damage <= 0) return;
        stack.hurtAndBreak(damage, player, EquipmentSlot.MAINHAND);
    }

    protected int getFellingLimit() {
        return 64;
    }
}
