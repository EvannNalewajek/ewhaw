package com.minecraft.mod.ewhaw.entity;

import com.minecraft.mod.ewhaw.EverythingWeHaveAlwaysWanted;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.DifficultyInstance;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.core.component.DataComponents;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class SqwackEntity extends AbstractHumanEntity implements RangedAttackMob, NeutralMob {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(EverythingWeHaveAlwaysWanted.MODID, "textures/entity/sqwack.png");
    private static final ResourceKey<LootTable> BARTER_LOOT = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(EverythingWeHaveAlwaysWanted.MODID, "gameplay/sqwack_bartering"));
    
    private static final EntityDataAccessor<Boolean> DATA_IS_ADMIRING = SynchedEntityData.defineId(SqwackEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(SqwackEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_HAS_INVERTED_BOW = SynchedEntityData.defineId(SqwackEntity.class, EntityDataSerializers.BOOLEAN);
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    
    private int barterTimer = 0;
    private int timeUntilNextBurst = 200 + (int)(Math.random() * 400);
    private int cawsRemainingInBurst = 0;
    private int timeUntilNextCaw = 0;

    @Nullable
    private UUID persistentAngerTarget;
    @Nullable
    private UUID lastItemThrower;

    public SqwackEntity(EntityType<? extends AbstractHumanEntity> entityType, Level level) {
        super(entityType, level);
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_IS_ADMIRING, false);
        builder.define(DATA_REMAINING_ANGER_TIME, 0);
        builder.define(DATA_HAS_INVERTED_BOW, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("HasInvertedBow", this.entityData.get(DATA_HAS_INVERTED_BOW));
        this.addPersistentAngerSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_HAS_INVERTED_BOW, tag.getBoolean("HasInvertedBow"));
        this.readPersistentAngerSaveData(this.level(), tag);
        // Force la pose visuelle à partir de l'état logique chargé par super
        this.setInSittingPose(this.isOrderedToSit());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        
        // Priorité 2 : Attaque à l'arc (gère aussi le recul pour garder ses distances)
        this.goalSelector.addGoal(2, new RangedBowAttackGoal<>(this, 1.0D, 20, 15.0F));
        
        // Priorité 3 : Attaque de mêlée (seulement si l'arc est rangé)
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            public boolean canUse() {
                return super.canUse() && SqwackEntity.this.getMainHandItem().is(Items.IRON_SWORD);
            }
            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && SqwackEntity.this.getMainHandItem().is(Items.IRON_SWORD);
            }
        });

        this.goalSelector.addGoal(4, new SqwackSearchPreciousItemGoal(this));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.25D, 6.0F, 2.0F));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (this.isTame() && this.isOwnedBy(player)) {
            if (isPreciousItem(itemstack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) itemstack.shrink(1);
                this.heal(4.0F);
                this.level().broadcastEntityEvent(this, (byte) 7);
                return InteractionResult.SUCCESS;
            }
            
            if (!this.isPreciousItem(itemstack) && !itemstack.is(Items.WRITABLE_BOOK)) {
                boolean sitting = !this.isOrderedToSit();
                this.setOrderedToSit(sitting);
                this.setInSittingPose(sitting); 
                this.navigation.stop();
                this.setTarget(null);
                return InteractionResult.SUCCESS;
            }
        }
        
        return super.mobInteract(player, hand);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setCustomName(Component.literal("Sqwack"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        this.updateSwingTime();
        
        if (!this.level().isClientSide) {
            // SYSTEME DE SONS EN RAFALES (BURSTS)
            if (!this.isSilent()) {
                if (this.cawsRemainingInBurst > 0) {
                    if (--this.timeUntilNextCaw <= 0) {
                        float pitch = 0.85F + this.random.nextFloat() * 0.25F;
                        float volume = 0.9F + this.random.nextFloat() * 0.1F;
                        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), 
                            com.minecraft.mod.ewhaw.registry.ModSounds.CROA.get(), 
                            net.minecraft.sounds.SoundSource.NEUTRAL, volume, pitch);
                        
                        this.cawsRemainingInBurst--;
                        // Délai Intra-Burst : Fixé à 20 ticks (1 seconde)
                        this.timeUntilNextCaw = 20;
                    }
                } else {
                    if (--this.timeUntilNextBurst <= 0) {
                        this.cawsRemainingInBurst = 2 + this.random.nextInt(3);
                        this.timeUntilNextCaw = 0;
                        // Reset Inter-Burst Delay : 200 à 600 ticks (10-30s)
                        this.timeUntilNextBurst = 200 + this.random.nextInt(401);
                    }
                }
            }

            boolean isSitting = this.isOrderedToSit();
            this.setInSittingPose(isSitting);

            if (isSitting) {
                this.setPose(net.minecraft.world.entity.Pose.SITTING);
                this.getNavigation().stop();
                net.minecraft.world.phys.Vec3 delta = this.getDeltaMovement();
                this.setDeltaMovement(0, delta.y, 0);
                this.setSprinting(false);
            } else {
                if (this.getPose() == net.minecraft.world.entity.Pose.SITTING) {
                    this.setPose(net.minecraft.world.entity.Pose.STANDING);
                }
                
                // Intelligence de rattrapage
                if (this.isTame() && this.getOwner() instanceof Player player) {
                    double distSq = this.distanceToSqr(player);
                    if (distSq > 64.0D) { this.setSprinting(true); } 
                    else if (distSq < 16.0D) { this.setSprinting(false); }
                }
            }

            // Intelligence de combat raffinée
            if (this.isTame() && this.isAlive() && !isSitting) {
                LivingEntity target = this.getTarget();
                net.minecraft.world.item.Item preferredBow = this.entityData.get(DATA_HAS_INVERTED_BOW) ? 
                    com.minecraft.mod.ewhaw.registry.ModItems.INVERTED_BOW.get() : Items.BOW;

                if (target != null && target.isAlive()) {
                    double distSq = this.distanceToSqr(target);
                    if (distSq < 9.0D) {
                        if (!this.getMainHandItem().is(Items.IRON_SWORD)) {
                            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                        }
                    } else if (distSq > 25.0D || !this.getMainHandItem().is(Items.IRON_SWORD)) {
                        if (!this.getMainHandItem().is(preferredBow)) {
                            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(preferredBow));
                        }
                    }
                } else {
                    if (!this.getMainHandItem().is(preferredBow)) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(preferredBow));
                    }
                }
            }

            if (!this.level().isDay() && this.isAlive() && !this.isTame()) {
                this.flee();
            }

            if (this.isAlive()) {
                ItemStack itemStack = this.getOffhandItem();
                if (isPreciousItem(itemStack)) {
                    this.barterTimer++;
                    this.setAdmiring(true);
                    this.getNavigation().stop();
                    if (this.barterTimer > 100) {
                        this.finishBartering(itemStack);
                    }
                } else {
                    this.barterTimer = 0;
                    this.setAdmiring(false);
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && amount >= this.getHealth() && !source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)) {
            this.flee();
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        this.swing(InteractionHand.MAIN_HAND);
        return super.doHurtTarget(target);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            this.swinging = true;
            this.swingTime = 0;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        this.swing(InteractionHand.MAIN_HAND);
        ItemStack weapon = this.getMainHandItem();
        boolean isInverted = weapon.is(com.minecraft.mod.ewhaw.registry.ModItems.INVERTED_BOW.get());
        
        ItemStack ammo = new ItemStack(Items.ARROW);
        AbstractArrow abstractarrow = ProjectileUtil.getMobArrow(this, ammo, velocity, weapon);
        
        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.3333333333333333D) - abstractarrow.getY();
        double d2 = target.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);

        if (isInverted) {
            abstractarrow.shoot(d0, d1 + d3 * 0.2D, d2, 0.4F, 10.0F); 
            this.playSound(com.minecraft.mod.ewhaw.registry.ModSounds.TWANG.get(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        } else {
            abstractarrow.shoot(d0, d1 + d3 * 0.2D, d2, 1.6F, (float)(14 - this.level().getDifficulty().getId() * 4));
            this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        }
        
        this.level().addFreshEntity(abstractarrow);
    }

    private void flee() {
        if (this.level() instanceof ServerLevel serverLevel) {
            this.setTame(false, true);
            this.setOwnerUUID(null);
            
            for(int i = 0; i < 20; ++i) {
                double d0 = this.random.nextGaussian() * 0.02D;
                double d1 = this.random.nextGaussian() * 0.02D;
                double d2 = this.random.nextGaussian() * 0.02D;
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getRandomX(1.0D), this.getRandomY(), this.getRandomZ(1.0D), 1, d0, d1, d2, 0.1D);
            }
            this.playSound(SoundEvents.FIREWORK_ROCKET_BLAST, 1.0F, 1.0F);
            this.discard();
        }
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide) {
            this.flee();
        }
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource damageSource) {
        return com.minecraft.mod.ewhaw.registry.ModSounds.CROA_HURT.get();
    }

    public boolean isAdmiring() { return this.entityData.get(DATA_IS_ADMIRING); }
    public void setAdmiring(boolean admiring) { this.entityData.set(DATA_IS_ADMIRING, admiring); }

    private void finishBartering(ItemStack stack) {
        this.playSound(com.minecraft.mod.ewhaw.registry.ModSounds.CROA_AGREE.get(), 1.0F, 1.0F);
        
        if (!this.isTame() && isLoveBook(stack) && this.lastItemThrower != null) {
            Player player = this.level().getPlayerByUUID(this.lastItemThrower);
            if (player != null) {
                this.tame(player);
                this.entityData.set(DATA_HAS_INVERTED_BOW, this.random.nextInt(8) == 0);
                this.level().broadcastEntityEvent(this, (byte) 7);
                stack.shrink(1);
                this.barterTimer = 0;
                this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                return;
            }
        }

        // 5% de chance de donner son livre quand même
        if (this.random.nextFloat() < 0.05F) {
            ItemEntity bookEntity = this.spawnAtLocation(createSqwackBook());
            if (bookEntity != null) {
                bookEntity.setThrower(this);
                bookEntity.setPickUpDelay(40);
            }
        }

        stack.shrink(1);
        if (this.level() instanceof ServerLevel serverLevel) {
            LootTable loottable = serverLevel.getServer().reloadableRegistries().getLootTable(BARTER_LOOT);
            List<ItemStack> list = loottable.getRandomItems(new LootParams.Builder(serverLevel).create(LootContextParamSets.EMPTY));
            for (ItemStack itemstack : list) {
                ItemEntity droppedItem = this.spawnAtLocation(itemstack);
                if (droppedItem != null) {
                    droppedItem.setThrower(this); // Sqwack est le donneur
                    droppedItem.setPickUpDelay(40); // 2 secondes de délai pour le joueur
                }
            }
        }
        this.barterTimer = 0;
        this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
    }

    private ItemStack createSqwackBook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        String[] titles = {"Me Write", "Glints!", "Kaw Kaw", "???", "Important", "Shiny Book", "Sqwack", "Look!!", "The Nest", "Before", "Empty", "Voice"};
        String title = titles[this.random.nextInt(titles.length)];
        
        List<net.minecraft.server.network.Filterable<Component>> pages = new java.util.ArrayList<>();
        int pageCount = 1 + this.random.nextInt(3);
        
        for (int i = 0; i < pageCount; i++) {
            pages.add(net.minecraft.server.network.Filterable.passThrough(Component.literal(generateSqwackNonsense())));
        }

        WrittenBookContent content = new WrittenBookContent(net.minecraft.server.network.Filterable.passThrough(title), "Sqwack", 0, pages, true);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, content);
        
        return book;
    }

    private String generateSqwackNonsense() {
        String[] words = {
            "Kaw", "Kraaa", "Shiny", "Glint", "Sparkle", "Friend", "Food?", "Worm", "Pretty", "Mine", "Gold", "Sqwack", 
            "Me", "You", "Tree", "Big", "Sky", "Egg", "Family", "Lost", "Void", "Shadow", "Forgotten", "Memory", 
            "Ancient", "Silence", "Echo", "Whisper", "Gone", "Hidden", "Moon", "Sun", "Star", "Key", "Cage", "Wings", 
            "Cold", "Dark", "Wait", "Long", "End", "Remember", "Home", "Together", "Wind", "Grass", "Hungry"
        };
        String[] fillers = {"...", "!!!", "???", "??!", ", ,", " - ", "... kaw?", "---", "..."};
        
        StringBuilder sb = new StringBuilder();
        int length = 10 + this.random.nextInt(20);
        
        for (int i = 0; i < length; i++) {
            if (this.random.nextFloat() < 0.75F) {
                sb.append(words[this.random.nextInt(words.length)]);
            } else {
                sb.append(fillers[this.random.nextInt(fillers.length)]);
            }
            
            if (this.random.nextFloat() < 0.3F) {
                sb.append(this.random.nextBoolean() ? ". " : "! ");
            } else {
                sb.append(" ");
            }
        }
        
        return sb.toString().trim();
    }

    private boolean isLoveBook(ItemStack stack) {
        if (!stack.is(Items.WRITABLE_BOOK)) return false;
        WritableBookContent content = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
        if (content != null) {
            for (net.minecraft.server.network.Filterable<String> page : content.pages()) {
                if (page.raw().toLowerCase().contains("i love you")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack itemstack = itemEntity.getItem();
        // N'accepte l'objet que s'il n'appartient pas à Sqwack
        if (isPreciousItem(itemstack) && this.getOffhandItem().isEmpty() && itemEntity.getOwner() != this) {
            this.onItemPickup(itemEntity);
            this.lastItemThrower = itemEntity.getOwner() != null ? itemEntity.getOwner().getUUID() : null;
            this.setItemSlot(EquipmentSlot.OFFHAND, itemstack.split(1));
            this.take(itemEntity, 1);
            if (itemstack.isEmpty()) {
                itemEntity.discard();
            }
        }
    }

    public boolean isPreciousItem(ItemStack stack) {
        return stack.is(Items.GOLD_INGOT) || stack.is(Items.EMERALD) || stack.is(Items.DIAMOND) || 
               stack.is(Items.AMETHYST_SHARD) || stack.is(Items.WRITABLE_BOOK);
    }

    @Override public boolean checkTotemDeathProtection(DamageSource source) { return false; }
    @Override public ResourceLocation getTextureLocation() { return TEXTURE; }
    @Override public boolean isFood(ItemStack stack) { return false; }

    @Override public int getRemainingPersistentAngerTime() { return this.entityData.get(DATA_REMAINING_ANGER_TIME); }
    @Override public void setRemainingPersistentAngerTime(int time) { this.entityData.set(DATA_REMAINING_ANGER_TIME, time); }
    @Override public @Nullable UUID getPersistentAngerTarget() { return this.persistentAngerTarget; }
    @Override public void setPersistentAngerTarget(@Nullable UUID target) { this.persistentAngerTarget = target; }
    @Override public void startPersistentAngerTimer() { this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random)); }

    static class SqwackSearchPreciousItemGoal extends Goal {
        private final SqwackEntity sqwack;
        public SqwackSearchPreciousItemGoal(SqwackEntity sqwack) { this.sqwack = sqwack; }
        @Override
        public boolean canUse() {
            if (!this.sqwack.getOffhandItem().isEmpty()) return false;
            if (this.sqwack.isOrderedToSit()) return false;
            // On ignore les objets jetés par Sqwack lui-même
            List<ItemEntity> list = this.sqwack.level().getEntitiesOfClass(ItemEntity.class, this.sqwack.getBoundingBox().inflate(8.0D, 4.0D, 8.0D), 
                item -> !item.hasPickUpDelay() && item.getOwner() != this.sqwack && this.sqwack.isPreciousItem(item.getItem()));
            return !list.isEmpty();
        }
        @Override
        public void tick() {
            List<ItemEntity> list = this.sqwack.level().getEntitiesOfClass(ItemEntity.class, this.sqwack.getBoundingBox().inflate(8.0D, 4.0D, 8.0D), 
                item -> !item.hasPickUpDelay() && item.getOwner() != this.sqwack && this.sqwack.isPreciousItem(item.getItem()));
            if (!list.isEmpty()) {
                ItemEntity target = list.stream().min(Comparator.comparingDouble(this.sqwack::distanceToSqr)).orElse(null);
                if (target != null) {
                    this.sqwack.setSprinting(true);
                    this.sqwack.getNavigation().moveTo(target, 1.5D);
                }
            }
        }
        @Override public void stop() { this.sqwack.setSprinting(false); super.stop(); }
    }
}
