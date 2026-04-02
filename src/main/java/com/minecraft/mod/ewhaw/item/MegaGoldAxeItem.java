package com.minecraft.mod.ewhaw.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MegaGoldAxeItem extends MegaAxeItem {
    public MegaGoldAxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    protected int getFellingLimit() {
        return 256;
    }

    @Override
    protected void fellTree(ItemStack stack, Level level, BlockState originState, BlockPos originPos, Player player, int[] stats) {
        // Abattage normal (logs) via la classe mère
        super.fellTree(stack, level, originState, originPos, player, stats);
        
        // Bonus Or : Nettoyage des feuilles autour de la zone initiale jusqu'au sommet trouvé
        if (!level.isClientSide) {
            stats[0] += cleanLeaves(level, originPos, stats[1], player, stack);
        }
    }

    private int cleanLeaves(Level level, BlockPos base, int maxYReached, Player player, ItemStack stack) {
        int radius = 8;
        int count = 0;
        
        // On balaie de la base jusqu'au sommet de l'arbre + 5 blocs de marge
        // On utilise les coordonnées absolues pour être sûr de tout couvrir
        BlockPos min = new BlockPos(base.getX() - radius, base.getY(), base.getZ() - radius);
        BlockPos max = new BlockPos(base.getX() + radius, maxYReached + 5, base.getZ() + radius);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.LEAVES)) {
                // On utilise breakExtraBlock pour faire tomber les loots sans blesser l'outil immédiatement
                breakExtraBlock(level, pos.immutable(), state, player, stack);
                count++;
            }
        }
        return count;
    }

    @Override
    protected void applyCappedDamage(ItemStack stack, Player player, int damage) {
        if (damage <= 0) return;
        
        // Logique "Fermeté" : Seulement si l'outil est NEUF (damageValue == 0)
        // et que les dégâts totaux vont le briser (damage >= maxDamage)
        if (stack.getDamageValue() == 0 && damage >= stack.getMaxDamage()) {
            damage = stack.getMaxDamage() - 1; // On le laisse à 1 PV
        }
        
        super.applyCappedDamage(stack, player, damage);
    }
}
