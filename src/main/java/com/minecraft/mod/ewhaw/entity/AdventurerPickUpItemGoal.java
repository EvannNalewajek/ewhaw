package com.minecraft.mod.ewhaw.entity;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import java.util.EnumSet;
import java.util.List;
import java.util.Comparator;

public class AdventurerPickUpItemGoal extends Goal {
    private final AdventurerEntity adventurer;
    private ItemEntity targetItem;
    private int scanTick = 0;

    public AdventurerPickUpItemGoal(AdventurerEntity adventurer) {
        this.adventurer = adventurer;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.scanTick-- > 0) return false;
        this.scanTick = 5;

        if (this.adventurer.isTame() && this.adventurer.getOwner() != null) {
            double distToOwner = this.adventurer.distanceToSqr(this.adventurer.getOwner());
            if (distToOwner > 256.0D) return false;
        }

        if (this.adventurer.isOrderedToSit() || this.adventurer.getTarget() != null) return false;

        return findNearestItem();
    }

    private boolean findNearestItem() {
        List<ItemEntity> list = this.adventurer.level().getEntitiesOfClass(ItemEntity.class, 
            this.adventurer.getBoundingBox().inflate(10.0D, 5.0D, 10.0D), 
            item -> !item.hasPickUpDelay() && this.adventurer.canHoldItem(item.getItem())
        );

        if (list.isEmpty()) return false;

        this.targetItem = list.stream()
            .min(Comparator.comparingDouble(this.adventurer::distanceToSqr))
            .orElse(null);

        return this.targetItem != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetItem == null || !this.targetItem.isAlive() || this.adventurer.isOrderedToSit() || this.adventurer.getTarget() != null) {
            return false;
        }

        if (this.adventurer.isTame() && this.adventurer.getOwner() != null) {
            if (this.adventurer.distanceToSqr(this.adventurer.getOwner()) > 400.0D) return false;
        }

        return this.adventurer.distanceToSqr(this.targetItem) < 100.0D;
    }

    @Override
    public void tick() {
        if (this.targetItem != null) {
            // Vitesse réduite à 1.2D pour un mouvement plus fluide
            this.adventurer.getNavigation().moveTo(this.targetItem, 1.2D);

            if (this.adventurer.distanceToSqr(this.targetItem) < 5.0D || this.adventurer.getBoundingBox().inflate(1.0D).intersects(this.targetItem.getBoundingBox())) {
                this.adventurer.pickUpItem(this.targetItem);
                if (!findNearestItem()) {
                    this.stop();
                }
            }
        }
    }

    @Override
    public void stop() {
        this.targetItem = null;
        this.adventurer.getNavigation().stop();
    }
}
