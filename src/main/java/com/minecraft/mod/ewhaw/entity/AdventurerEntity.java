package com.minecraft.mod.ewhaw.entity;

import com.minecraft.mod.ewhaw.EverythingWeHaveAlwaysWanted;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.NeutralMob;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import java.util.UUID;

public class AdventurerEntity extends AbstractHumanEntity implements ContainerListener, RangedAttackMob, NeutralMob {
    private static final EntityDataAccessor<Integer> DATA_SKIN_ID = SynchedEntityData.defineId(AdventurerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_SLIM = SynchedEntityData.defineId(AdventurerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(AdventurerEntity.class, EntityDataSerializers.INT);
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    @Nullable
    private UUID persistentAngerTarget;

    private final SimpleContainer inventory = new SimpleContainer(9);
    
    private final RangedBowAttackGoal<AdventurerEntity> bowGoal = new RangedBowAttackGoal<>(this, 1.0D, 20, 15.0F);
    private final MeleeAttackGoal meleeGoal = new MeleeAttackGoal(this, 1.2D, false);

    public AdventurerEntity(EntityType<? extends AbstractHumanEntity> entityType, Level level) {
        super(entityType, level);
        this.inventory.addListener(this);
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SKIN_ID, 0);
        builder.define(DATA_IS_SLIM, false);
        builder.define(DATA_REMAINING_ANGER_TIME, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkinId", this.getSkinId());
        tag.putBoolean("IsSlim", this.isSlim());
        tag.put("Inventory", this.inventory.createTag(this.registryAccess()));
        this.addPersistentAngerSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setSkinId(tag.getInt("SkinId"));
        this.setSlim(tag.getBoolean("IsSlim"));
        if (tag.contains("Inventory", 10)) {
            this.inventory.fromTag(tag.getList("Inventory", 10), this.registryAccess());
        }
        this.readPersistentAngerSaveData(this.level(), tag);
        this.reassessAttackGoals();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    public int getSkinId() { return this.entityData.get(DATA_SKIN_ID); }
    public void setSkinId(int id) { this.entityData.set(DATA_SKIN_ID, id); }
    public boolean isSlim() { return this.entityData.get(DATA_IS_SLIM); }
    public void setSlim(boolean slim) { this.entityData.set(DATA_IS_SLIM, slim); }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource random = level.getRandom();
        this.setSlim(random.nextBoolean());
        this.setSkinId(random.nextInt(2));
        this.populateDefaultEquipmentSlots(random, difficulty);
        this.reassessAttackGoals();
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new AdventurerPickUpItemGoal(this));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.25D, 6.0F, 2.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, true));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Monster.class, false));
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof Monster) return true;
        return super.wantsToAttack(target, owner);
    }

    public void reassessAttackGoals() {
        if (this.level() != null && !this.level().isClientSide) {
            this.goalSelector.removeGoal(this.meleeGoal);
            this.goalSelector.removeGoal(this.bowGoal);
            if (this.getMainHandItem().is(Items.BOW)) {
                this.goalSelector.addGoal(2, this.bowGoal);
            } else {
                this.goalSelector.addGoal(2, this.meleeGoal);
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.swing(InteractionHand.MAIN_HAND, true);
        return super.doHurtTarget(target);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        this.swing(InteractionHand.MAIN_HAND, true);
        ItemStack ammoStack = new ItemStack(Items.ARROW);
        ItemStack weapon = this.getMainHandItem();
        if (!weapon.is(Items.BOW)) {
            weapon = new ItemStack(Items.BOW);
        }
        AbstractArrow abstractarrow = ProjectileUtil.getMobArrow(this, ammoStack, velocity, weapon);
        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.33D) - abstractarrow.getY();
        double d2 = target.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        abstractarrow.shoot(d0, d1 + d3 * 0.2D, d2, 1.6F, (float)(14 - this.level().getDifficulty().getId() * 4));
        this.playSound(net.minecraft.sounds.SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(abstractarrow);
    }

    @Override
    public ItemStack getProjectile(ItemStack weapon) {
        return weapon.getItem() instanceof ProjectileWeaponItem ? new ItemStack(Items.ARROW) : super.getProjectile(weapon);
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        if (!this.level().isClientSide && slot == EquipmentSlot.MAINHAND) this.reassessAttackGoals();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            // Effet de résistance gratuit avec un bouclier
            if (this.tickCount % 20 == 0 && (this.getOffhandItem().is(Items.SHIELD) || this.getMainHandItem().is(Items.SHIELD))) {
                this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 40, 0, false, false, false));
            }
            
            if (this.isTame() && this.getOwner() instanceof Player player) {
                double distanceSq = this.distanceToSqr(player);
                if (distanceSq > 64.0D && this.getNavigation().isInProgress()) this.setSprinting(true);
                else if (distanceSq < 16.0D) this.setSprinting(false);
                if (!this.isOrderedToSit() && distanceSq > 576.0D) this.teleportToOwner(player);
            }
        }
    }

    private void teleportToOwner(Player owner) {
        BlockPos pos = owner.blockPosition();
        for (int i = 0; i < 10; ++i) {
            int x = this.random.nextInt(3) - 1; int y = this.random.nextInt(3) - 1; int z = this.random.nextInt(3) - 1;
            if (this.maybeTeleportTo(pos.getX() + x, pos.getY() + y, pos.getZ() + z)) return;
        }
    }

    private boolean maybeTeleportTo(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!this.level().noCollision(this, this.getBoundingBox().move(pos.getCenter().subtract(this.position()))) || !this.level().getBlockState(pos.below()).isFaceSturdy(this.level(), pos.below(), net.minecraft.core.Direction.UP)) return false;
        this.moveTo((double)x + 0.5D, (double)y, (double)z + 0.5D, this.getYRot(), this.getXRot());
        this.navigation.stop();
        return true;
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack itemstack = itemEntity.getItem();
        if (this.canHoldItem(itemstack)) {
            this.onItemPickup(itemEntity);
            this.take(itemEntity, itemstack.getCount());
            ItemStack remainder = this.inventory.addItem(itemstack);
            if (remainder.isEmpty()) itemEntity.discard();
            else itemstack.setCount(remainder.getCount());
        }
    }

    public boolean canHoldItem(ItemStack stack) { return this.inventory.canAddItem(stack); }
    public SimpleContainer getAdventurerInventory() { return this.inventory; }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (this.isTame() && this.isOwnedBy(player)) {
            // Shift + Clic Droit : Assis / Debout
            if (player.isShiftKeyDown()) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.jumping = false;
                this.navigation.stop();
                this.setTarget(null);
                
                if (!this.level().isClientSide) {
                    String message = this.isOrderedToSit() ? "Adventurer is now waiting." : "Adventurer is now following.";
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal(message), true);
                }
                return InteractionResult.SUCCESS;
            }
            // Clic Droit simple : Inventaire (sauf si or pour soin)
            if (!itemstack.is(Items.GOLD_INGOT)) {
                if (!this.level().isClientSide) {
                    player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                        (id, inv, p) -> new com.minecraft.mod.ewhaw.menu.AdventurerMenu(id, inv, this),
                        net.minecraft.network.chat.Component.literal("")
                    ), buf -> buf.writeInt(this.getId()));
                }
                return InteractionResult.SUCCESS;
            }
            if (itemstack.is(Items.GOLD_INGOT) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) itemstack.shrink(1);
                this.heal(5.0F);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.SUCCESS;
        } else if (itemstack.is(Items.GOLD_INGOT)) {
            if (!player.getAbilities().instabuild) itemstack.shrink(1);
            if (this.random.nextInt(3) == 0) {
                this.tame(player);
                this.setOrderedToSit(false); // On force Debout après l'apprivoisement
                this.level().broadcastEntityEvent(this, (byte) 7);
            } else this.level().broadcastEntityEvent(this, (byte) 6);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override public void containerChanged(Container container) {}

    @Override
    public ResourceLocation getTextureLocation() {
        String folder = this.isSlim() ? "female" : "male";
        return ResourceLocation.fromNamespaceAndPath(EverythingWeHaveAlwaysWanted.MODID, "textures/entity/adventurer/" + folder + "_" + this.getSkinId() + ".png");
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        Item weapon;
        float weaponChance = random.nextFloat();
        if (weaponChance < 0.25F) weapon = Items.BOW;
        else {
            int tier = random.nextInt(3); boolean isSword = random.nextBoolean();
            if (isSword) weapon = switch (tier) { case 1 -> Items.STONE_SWORD; case 2 -> Items.IRON_SWORD; default -> Items.WOODEN_SWORD; };
            else weapon = switch (tier) { case 1 -> Items.STONE_AXE; case 2 -> Items.IRON_AXE; default -> Items.WOODEN_AXE; };
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
        float shieldChance = (weapon == Items.BOW) ? 0.05F : 0.25F;
        if (random.nextFloat() < shieldChance) this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        populateArmorSlot(random, EquipmentSlot.HEAD); populateArmorSlot(random, EquipmentSlot.CHEST); populateArmorSlot(random, EquipmentSlot.LEGS); populateArmorSlot(random, EquipmentSlot.FEET);
    }

    private void populateArmorSlot(RandomSource random, EquipmentSlot slot) {
        if (random.nextFloat() < 0.5F) {
            int type = random.nextInt(3); Item armorPiece = null;
            if (slot == EquipmentSlot.HEAD) armorPiece = switch (type) { case 1 -> Items.CHAINMAIL_HELMET; case 2 -> Items.IRON_HELMET; default -> Items.LEATHER_HELMET; };
            else if (slot == EquipmentSlot.CHEST) armorPiece = switch (type) { case 1 -> Items.CHAINMAIL_CHESTPLATE; case 2 -> Items.IRON_CHESTPLATE; default -> Items.LEATHER_CHESTPLATE; };
            else if (slot == EquipmentSlot.LEGS) armorPiece = switch (type) { case 1 -> Items.CHAINMAIL_LEGGINGS; case 2 -> Items.IRON_LEGGINGS; default -> Items.LEATHER_LEGGINGS; };
            else if (slot == EquipmentSlot.FEET) armorPiece = switch (type) { case 1 -> Items.CHAINMAIL_BOOTS; case 2 -> Items.IRON_BOOTS; default -> Items.LEATHER_BOOTS; };
            if (armorPiece != null) this.setItemSlot(slot, new ItemStack(armorPiece));
        }
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, time);
    }

    @Override
    public @Nullable UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID target) {
        this.persistentAngerTarget = target;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }
}
