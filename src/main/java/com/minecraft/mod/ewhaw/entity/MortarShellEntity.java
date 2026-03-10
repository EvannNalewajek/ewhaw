package com.minecraft.mod.ewhaw.entity;

import com.minecraft.mod.ewhaw.registry.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MortarShellEntity extends ThrowableProjectile implements ItemSupplier {

    public MortarShellEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public MortarShellEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.MORTAR_SHELL.get(), x, y, z, level);
    }

    @Override
    public ItemStack getItem() {
        // On lui donne l'apparence visuelle d'un boulet de feu
        return new ItemStack(Items.FIRE_CHARGE);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        // Pas de données synchronisées spéciales pour l'instant
    }

    @Override
    public void tick() {
        super.tick();
        
        // Ajoute des particules de fumée derrière le boulet pendant qu'il vole
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            // Crée une explosion de puissance 3 (un peu moins qu'un TNT) qui ne détruit pas les blocs
            this.level().explode(
                    this,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    3.0F,
                    Level.ExplosionInteraction.NONE
            );
            
            // Ajoute un effet visuel beaucoup plus grand et explicite pour la zone de dégâts
            if (this.level() instanceof ServerLevel serverLevel) {
                // Un grand nuage de flammes qui s'étend sur environ 3 blocs de rayon
                serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.5, this.getZ(), 100, 2.5D, 0.5D, 2.5D, 0.2D);
                // Un énorme nuage de fumée sombre épaisse
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 100, 2.5D, 1.5D, 2.5D, 0.05D);
                // Quelques particules de lave qui sautent pour le style "mortier"
                serverLevel.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 0.5, this.getZ(), 30, 1.5D, 0.5D, 1.5D, 0.5D);
            }

            this.discard(); // Détruit le boulet après l'explosion
        }
    }
}
