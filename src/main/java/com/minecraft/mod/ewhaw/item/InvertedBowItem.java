package com.minecraft.mod.ewhaw.item;

import com.minecraft.mod.ewhaw.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InvertedBowItem extends BowItem {
    public InvertedBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player player) {
            ItemStack itemstack = player.getProjectile(stack);
            if (!itemstack.isEmpty()) {
                int i = this.getUseDuration(stack, entityLiving) - timeLeft;
                float f = getPowerForTime(i);
                if (!((double)f < 0.1D)) {
                    if (level instanceof ServerLevel serverLevel) {
                        // Logique 1.21.1 : On "draw" les munitions puis on "shoot"
                        List<ItemStack> list = draw(stack, itemstack, player);
                        if (!list.isEmpty()) {
                            // Vitesse réduite : f * 3.0F est la vitesse normale, on multiplie par 0.25
                            float velocity = f * 3.0F * 0.25F;
                            this.shoot(serverLevel, player, player.getUsedItemHand(), stack, list, velocity, 1.0F, f == 1.0F, null);
                        }
                    }
                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float spread, float yaw, @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + yaw, 0.0F, velocity, spread);
        shooter.level().addFreshEntity(projectile);
        
        // On joue notre son personnalisé
        shooter.level().playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), 
                ModSounds.TWANG.get(), SoundSource.PLAYERS, 1.0F, 1.0F / (shooter.level().getRandom().nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);
    }
}
