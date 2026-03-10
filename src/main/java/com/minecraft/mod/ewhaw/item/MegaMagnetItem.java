package com.minecraft.mod.ewhaw.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MegaMagnetItem extends Item {

    public MegaMagnetItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player) {
            // Actif s'il est tenu dans la main principale (sélectionné) ou la main secondaire
            if (isSelected || player.getOffhandItem() == stack) {
                double radius = 10.0D;
                AABB boundingBox = player.getBoundingBox().inflate(radius, radius, radius);

                // Attirer les objets au sol
                List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, boundingBox);
                for (ItemEntity itemEntity : items) {
                    if (!itemEntity.hasPickUpDelay()) {
                        pullEntityTowardsPlayer(itemEntity, player);
                    }
                }

                // Attirer les orbes d'expérience
                List<ExperienceOrb> xpOrbs = level.getEntitiesOfClass(ExperienceOrb.class, boundingBox);
                for (ExperienceOrb orb : xpOrbs) {
                    pullEntityTowardsPlayer(orb, player);
                }
            }
        }
    }

    private void pullEntityTowardsPlayer(Entity targetEntity, Player player) {
        Vec3 targetPos = targetEntity.position();
        Vec3 playerPos = player.position().add(0, player.getEyeHeight() / 2.0, 0); // Vise vers le centre du joueur

        Vec3 motion = playerPos.subtract(targetPos);
        double distance = motion.length();
        
        if (distance > 1.0) {
            Vec3 direction = motion.normalize().scale(0.1);
            targetEntity.setDeltaMovement(targetEntity.getDeltaMovement().add(direction));
            targetEntity.hasImpulse = true; // Indique que l'entité a changé de trajectoire
        }
    }
}
