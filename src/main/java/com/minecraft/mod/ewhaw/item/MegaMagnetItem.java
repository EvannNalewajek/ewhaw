package com.minecraft.mod.ewhaw.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Items;
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
            boolean inMainHand = player.getMainHandItem() == stack;
            boolean inOffHand = player.getOffhandItem() == stack;

            if (inMainHand || inOffHand) {
                // Main hand: Repel
                // Off-hand: Attract
                boolean attract = inOffHand;
                
                double radius = 10.0D;
                AABB scanArea = player.getBoundingBox().inflate(radius, radius, radius);

                // Items & XP Orbs (Logic déjà présente)
                List<Entity> attractables = level.getEntities(player, scanArea, e -> 
                    e instanceof ItemEntity || e instanceof ExperienceOrb || e instanceof AbstractMinecart
                );
                
                for (Entity e : attractables) {
                    if (e instanceof ItemEntity item && item.hasPickUpDelay()) continue;
                    moveEntityTowardsPlayer(e, player, attract);
                }

                // Metallic Mobs (LivingEntities)
                List<LivingEntity> livingEntities = level.getEntitiesOfClass(LivingEntity.class, scanArea);
                for (LivingEntity living : livingEntities) {
                    if (living != player && isMetallic(living)) {
                        moveEntityTowardsPlayer(living, player, attract);
                    }
                }

                // Falling Anvils
                List<FallingBlockEntity> fallingBlocks = level.getEntitiesOfClass(FallingBlockEntity.class, scanArea);
                for (FallingBlockEntity fallingBlock : fallingBlocks) {
                    if (fallingBlock.getBlockState().is(BlockTags.ANVIL)) {
                        moveEntityTowardsPlayer(fallingBlock, player, attract);
                    }
                }
            }
        }
    }

    private boolean isMetallic(LivingEntity entity) {
        if (entity instanceof IronGolem) return true;
        for (ItemStack armor : entity.getArmorSlots()) {
            if (isMetallicItem(armor)) return true;
        }
        return false;
    }

    private boolean isMetallicItem(ItemStack stack) {
        return stack.is(Items.IRON_HELMET) || stack.is(Items.IRON_CHESTPLATE) || stack.is(Items.IRON_LEGGINGS) || stack.is(Items.IRON_BOOTS) ||
               stack.is(Items.CHAINMAIL_HELMET) || stack.is(Items.CHAINMAIL_CHESTPLATE) || stack.is(Items.CHAINMAIL_LEGGINGS) || stack.is(Items.CHAINMAIL_BOOTS) ||
               stack.is(Items.IRON_HORSE_ARMOR) || stack.is(Items.IRON_INGOT) || stack.is(Items.IRON_BLOCK) || stack.is(Items.RAW_IRON);
    }

    private void moveEntityTowardsPlayer(Entity targetEntity, Player player, boolean attract) {
        Vec3 targetPos = targetEntity.position();
        Vec3 playerPos = player.position().add(0, player.getEyeHeight() / 2.0, 0);

        Vec3 motion = playerPos.subtract(targetPos);
        double distance = motion.length();
        
        if (distance > 0.5 && distance < 10.0) {
            double strength = attract ? 0.15 : -0.2; 
            Vec3 direction = motion.normalize().scale(strength);
            targetEntity.setDeltaMovement(targetEntity.getDeltaMovement().add(direction));
            targetEntity.hasImpulse = true;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.add(Component.translatable("item.everythingwehavealwayswanted.mega_magnet.desc.off_hand").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.everythingwehavealwayswanted.mega_magnet.desc.main_hand").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.everythingwehavealwayswanted.mega_magnet.desc.metallic").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, tooltip, tooltipFlag);
    }
}
