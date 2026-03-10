package com.minecraft.mod.ewhaw.block.entity;

import com.minecraft.mod.ewhaw.block.MortarBlock;
import com.minecraft.mod.ewhaw.entity.MortarShellEntity;
import com.minecraft.mod.ewhaw.menu.MortarMenu;
import com.minecraft.mod.ewhaw.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MortarBlockEntity extends BlockEntity implements MenuProvider {
    private int cooldown = 0;

    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.is(Items.COAL) || stack.is(Items.CHARCOAL); // Charbon ou Charbon de bois
                case 1 -> stack.is(Items.GUNPOWDER); // Poudre à canon
                case 2 -> stack.is(Items.FLINT_AND_STEEL); // Briquet
                default -> false;
            };
        }
    };

    public MortarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MORTAR_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("inventory", itemHandler.serializeNBT(registries));
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.everythingwehavealwayswanted.mortar");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MortarMenu(containerId, playerInventory, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MortarBlockEntity entity) {
        if (entity.cooldown > 0) {
            entity.cooldown--;
            return;
        }

        if (level.getGameTime() % 10 == 0) {
            // Vérifie si on a les munitions requises
            ItemStack coalStack = entity.itemHandler.getStackInSlot(0);
            ItemStack gunpowderStack = entity.itemHandler.getStackInSlot(1);
            ItemStack flintStack = entity.itemHandler.getStackInSlot(2);

            boolean hasAmmo = (!coalStack.isEmpty() && (coalStack.is(Items.COAL) || coalStack.is(Items.CHARCOAL)))
                    && !gunpowderStack.isEmpty() && gunpowderStack.is(Items.GUNPOWDER)
                    && !flintStack.isEmpty() && flintStack.is(Items.FLINT_AND_STEEL);

            if (!hasAmmo) {
                return; // Ne fait rien s'il n'y a pas de munitions
            }

            double radius = 15.0D;
            AABB scanArea = new AABB(pos).inflate(radius);
            
            List<Monster> monsters = level.getEntitiesOfClass(Monster.class, scanArea);
            
            if (!monsters.isEmpty()) {
                Monster target = monsters.get(0);
                
                // Consomme les munitions
                coalStack.shrink(1);
                gunpowderStack.shrink(1);
                
                // Use durability of flint and steel
                flintStack.setDamageValue(flintStack.getDamageValue() + 1);
                if (flintStack.getDamageValue() >= flintStack.getMaxDamage()) {
                    entity.itemHandler.setStackInSlot(2, ItemStack.EMPTY);
                    level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                
                // Rotation du bloc vers la cible
                faceTarget(level, pos, state, target);

                // Tirer le boulet
                shootAtTarget(level, pos, target);
                
                entity.cooldown = 60; // 3 secondes
            }
        }
    }

    private static void faceTarget(Level level, BlockPos pos, BlockState state, Monster target) {
        double dX = target.getX() - pos.getX();
        double dZ = target.getZ() - pos.getZ();
        
        Direction newDirection;
        if (Math.abs(dX) > Math.abs(dZ)) {
            newDirection = dX > 0 ? Direction.EAST : Direction.WEST;
        } else {
            newDirection = dZ > 0 ? Direction.SOUTH : Direction.NORTH;
        }

        if (state.getValue(MortarBlock.FACING) != newDirection) {
            level.setBlock(pos, state.setValue(MortarBlock.FACING, newDirection), 3);
        }
    }

    private static void shootAtTarget(Level level, BlockPos pos, Monster target) {
        MortarShellEntity shell = new MortarShellEntity(level, pos.getX() + 0.5, pos.getY() + 2.5, pos.getZ() + 0.5);
        
        double dX = target.getX() - shell.getX();
        double dY = target.getY(0.3333333333333333D) - shell.getY();
        double dZ = target.getZ() - shell.getZ();
        
        double horizontalDistance = Math.sqrt(dX * dX + dZ * dZ);
        
        // La gravité de Minecraft pour les projectiles (ThrowableProjectile) est constante: 0.03 par tick
        // Formule balistique:
        // Pour un temps de vol désiré T:
        // V_y = (dY + 0.5 * g * T^2) / T
        // V_x = dX / T
        // V_z = dZ / T
        
        double g = 0.05D; // Gravité de l'entité (ajusté pour coller au comportement de Minecraft)
        
        // On force un temps de vol assez long (en ticks) pour avoir un bel arc en cloche.
        // Plus la cible est loin, plus ça prend de temps, mais au minimum 20 ticks (1 seconde).
        double timeToTarget = Math.max(20.0D, horizontalDistance * 3.0D); 
        
        double vX = dX / timeToTarget;
        double vY = (dY + 0.5D * g * timeToTarget * timeToTarget) / timeToTarget;
        double vZ = dZ / timeToTarget;

        // La méthode shoot prend une direction (un vecteur normalisé) et une vitesse scalaire globale
        double velocity = Math.sqrt(vX * vX + vY * vY + vZ * vZ);
        
        // On donne directement le vecteur cible à shoot
        shell.shoot(vX, vY, vZ, (float)velocity, 0.0F);

        level.addFreshEntity(shell);
        
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
        
        // Effet visuel du tir (fumée et explosion à la sortie du canon)
        if (level instanceof ServerLevel serverLevel) {
            double muzzleX = pos.getX() + 0.5;
            double muzzleY = pos.getY() + 2.5;
            double muzzleZ = pos.getZ() + 0.5;
            
            // Explosion de poudre
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, muzzleX, muzzleY, muzzleZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            // Grosse fumée de poudre noire qui s'échappe vers le haut
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, muzzleX, muzzleY, muzzleZ, 20, 0.2D, 0.2D, 0.2D, 0.1D);
        }
    }
}