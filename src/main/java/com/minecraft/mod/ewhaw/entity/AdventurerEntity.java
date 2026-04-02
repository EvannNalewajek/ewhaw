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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
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
    private static final EntityDataAccessor<Boolean> DATA_IS_KO = SynchedEntityData.defineId(AdventurerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_KO_TIMER = SynchedEntityData.defineId(AdventurerEntity.class, EntityDataSerializers.INT);

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    @Nullable
    private UUID persistentAngerTarget;

    private static final ResourceLocation SNEAKING_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(EverythingWeHaveAlwaysWanted.MODID, "sneaking_speed_reduction");
    private static final AttributeModifier SNEAKING_SPEED_MODIFIER = new AttributeModifier(SNEAKING_SPEED_MODIFIER_ID, -0.4D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private final SimpleContainer inventory = new SimpleContainer(9);
    
    private final RangedBowAttackGoal<AdventurerEntity> bowGoal = new RangedBowAttackGoal<>(this, 1.0D, 20, 15.0F);
    private final RangedAttackGoal potionGoal = new RangedAttackGoal(this, 1.0D, 40, 12.0F);
    private final MeleeAttackGoal meleeGoal = new MeleeAttackGoal(this, 1.2D, false);

    private boolean isSupporting = false;

    private static final String[] MALE_NAMES = {
            "Alaric", "Caelum", "Faelan", "Hakon", "Jarek", "Kael", "Marek", "Orin",
            "Soren", "Uthger", "Valen", "Xander", "Zale", "Gareth", "Kaden", "Thorne",
            "Leif", "Tyrion", "Ossian", "Evander", "Balthazar", "Silas", "Dragan",
            "Vorne", "Rurik", "Kaelen", "Thorgal", "Bram", "Zephyr", "Elian",
            "Moros", "Thalès", "Malakor", "Solas", "Cian", "Eamon"
    };
    private static final String[] FEMALE_NAMES = {
            "Elowen", "Lyra", "Niamh", "Talia", "Iara", "Gwen", "Phaedra", "Riona",
            "Yseult", "Bryn", "Wren", "Darana", "Aria", "Kaelie", "Selene", "Mira",
            "Aerith", "Elara", "Naevia", "Ione", "Lunara", "Thalassa", "Isolde",
            "Morwenna", "Beatrix", "Rowena", "Sybil", "Elsabeth", "Freja", "Gaia",
            "Nyx", "Rhiannon", "Vesper", "Astrid", "Maeve", "Hestia", "Roxanne"
    };

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
        builder.define(DATA_IS_KO, false);
        builder.define(DATA_KO_TIMER, 0);
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
    public boolean isKO() { return this.entityData.get(DATA_IS_KO); }
    public void setKO(boolean ko) { this.entityData.set(DATA_IS_KO, ko); }
    public int getKOTimer() { return this.entityData.get(DATA_KO_TIMER); }
    public void setKOTimer(int timer) { this.entityData.set(DATA_KO_TIMER, timer); }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource random = level.getRandom();
        this.setSlim(random.nextBoolean());
        this.setSkinId(random.nextInt(2));
        this.populateDefaultEquipmentSlots(random, difficulty);
        
        // Attribution d'un nom selon le genre (modèle Slim = Femme)
        String[] nameList = this.isSlim() ? FEMALE_NAMES : MALE_NAMES;
        String randomName = nameList[random.nextInt(nameList.length)];
        this.setCustomName(net.minecraft.network.chat.Component.literal(randomName));
        this.setCustomNameVisible(true);

        this.reassessAttackGoals();
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new AdventurerSupportGoal(this));
        this.goalSelector.addGoal(3, new AdventurerKiteGoal(this)); // Nouveau : Reculer si trop proche
        this.goalSelector.addGoal(4, new AdventurerPickUpItemGoal(this));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.25D, 6.0F, 2.0F));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, true));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, (target) -> {
            if (this.isTame() && this.getOwner() != null && this.getOwner().isShiftKeyDown()) return false;
            return true;
        }));
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (owner.isShiftKeyDown()) return false;
        if (target instanceof Monster) return true;
        return super.wantsToAttack(target, owner);
    }

    public void reassessAttackGoals() {
        if (this.level() != null && !this.level().isClientSide) {
            this.goalSelector.removeGoal(this.meleeGoal);
            this.goalSelector.removeGoal(this.bowGoal);
            this.goalSelector.removeGoal(this.potionGoal);
            
            ItemStack mainHand = this.getMainHandItem();
            if (mainHand.is(Items.BOW)) {
                this.goalSelector.addGoal(4, this.bowGoal);
            } else if (mainHand.getItem() instanceof ThrowablePotionItem) {
                this.goalSelector.addGoal(4, this.potionGoal);
            } else {
                this.goalSelector.addGoal(4, this.meleeGoal);
            }
        }
    }

    @Override
    public boolean hurt(@NotNull net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (this.isKO()) return false;

        if (!this.level().isClientSide && source.getEntity() instanceof LivingEntity attacker && attacker != this.getOwner()) {
            this.setTarget(attacker); // On change de focus immédiatement sur l'agresseur
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(@NotNull net.minecraft.world.damagesource.DamageSource source) {
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD) || this.isKO()) {
            super.die(source);
            return;
        }

        // Interception Totale
        this.setHealth(1.0F);
        this.setKO(true);
        this.setKOTimer(1200);
        this.navigation.stop();
        this.setTarget(null);
        this.setPose(Pose.SLEEPING);
        
        this.playSound(net.minecraft.sounds.SoundEvents.PLAYER_HURT, 1.0F, 0.5F);
        
        if (this.getOwner() instanceof Player player) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.everythingwehavealwayswanted.adventurer.down", this.getCustomName().getString()).withStyle(net.minecraft.ChatFormatting.RED), false);
        }
    }

    @Override
    public boolean checkTotemDeathProtection(net.minecraft.world.damagesource.DamageSource source) {
        return false; 
    }

    private class AdventurerKiteGoal extends Goal {
        private final AdventurerEntity adventurer;
        private LivingEntity kiteTarget;
        private int attackCooldown;

        public AdventurerKiteGoal(AdventurerEntity adventurer) {
            this.adventurer = adventurer;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (adventurer.isOrderedToSit()) return false;

            ItemStack mainHand = adventurer.getMainHandItem();
            boolean isRanged = mainHand.is(Items.BOW) || mainHand.getItem() instanceof ThrowablePotionItem;
            if (!isRanged) return false;

            // On cherche la menace la plus proche (pas seulement le target actuel)
            LivingEntity closestThreat = adventurer.level().getNearestEntity(
                adventurer.level().getEntitiesOfClass(Monster.class, adventurer.getBoundingBox().inflate(8.0D)),
                net.minecraft.world.entity.ai.targeting.TargetingConditions.forCombat().range(8.0D),
                adventurer, adventurer.getX(), adventurer.getY(), adventurer.getZ()
            );

            if (closestThreat != null) {
                this.kiteTarget = closestThreat;
                return true;
            }

            return false;
        }

        @Override
        public void tick() {
            if (this.kiteTarget == null || !this.kiteTarget.isAlive()) return;
            
            // 1. Logique de regard et rotation
            adventurer.getLookControl().setLookAt(this.kiteTarget, 30.0F, 30.0F);
            adventurer.setYBodyRot(adventurer.getYHeadRot());

            // 2. Logique de mouvement (Recul fluide par rapport à la menace la plus proche)
            double dx = adventurer.getX() - this.kiteTarget.getX();
            double dz = adventurer.getZ() - this.kiteTarget.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            
            if (adventurer.tickCount % 5 == 0) {
                if (distance < 7.0D) {
                    double targetX = this.kiteTarget.getX() + (dx / distance) * 12.0D;
                    double targetZ = this.kiteTarget.getZ() + (dz / distance) * 12.0D;
                    adventurer.getNavigation().moveTo(targetX, adventurer.getY(), targetZ, 1.2D);
                } else {
                    adventurer.getNavigation().stop();
                }
            }
            
            // 3. Logique d'attaque (On attaque la menace la plus proche si on kite, sinon le target normal)
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            } else {
                LivingEntity attackTarget = (adventurer.getTarget() != null) ? adventurer.getTarget() : this.kiteTarget;
                adventurer.updateAlchemistWeapon(attackTarget);
                adventurer.performRangedAttack(attackTarget, 1.0F);
                this.attackCooldown = adventurer.getMainHandItem().is(Items.BOW) ? 20 : 40;
            }
        }
    }

    private class AdventurerSupportGoal extends Goal {
        private final AdventurerEntity adventurer;
        private LivingEntity supportTarget;
        private int cooldown;

        public AdventurerSupportGoal(AdventurerEntity adventurer) {
            this.adventurer = adventurer;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!adventurer.isTame()) return false;
            
            // Vérification Prioritaire : Propriétaire
            LivingEntity owner = adventurer.getOwner();
            if (owner != null && owner.isAlive() && adventurer.distanceToSqr(owner) < 144.0D) {
                if (adventurer.findBestSupportPotion(owner) != -1) {
                    this.supportTarget = owner;
                    return true;
                }
            }

            // Vérification Secondaire : Soi-même
            if (adventurer.findBestSupportPotion(adventurer) != -1) {
                this.supportTarget = adventurer;
                return true;
            }

            return false;
        }

        @Override
        public void start() {
            adventurer.isSupporting = true;
        }

        @Override
        public void stop() {
            adventurer.isSupporting = false;
            this.supportTarget = null;
            adventurer.reassessAttackGoals();
        }

        @Override
        public void tick() {
            if (this.supportTarget == null) return;

            adventurer.getLookControl().setLookAt(this.supportTarget, 30.0F, 30.0F);
            double distSq = adventurer.distanceToSqr(this.supportTarget);

            if (distSq > 9.0D) {
                adventurer.getNavigation().moveTo(this.supportTarget, 1.2D);
            } else {
                adventurer.getNavigation().stop();
                if (this.cooldown > 0) {
                    this.cooldown--;
                } else {
                    int slot = adventurer.findBestSupportPotion(this.supportTarget);
                    if (slot != -1) {
                        adventurer.swapToSlot(slot);
                        adventurer.performRangedAttack(this.supportTarget, 1.0F);
                        this.cooldown = 20; // 1 seconde entre chaque action de soutien
                    }
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
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
        ItemStack mainHand = this.getMainHandItem();

        if (mainHand.getItem() instanceof ThrowablePotionItem) {
            // Logique Alchimiste
            double d0 = target.getY(0.3333333333333333D) - 1.100000023841858D;
            double d1 = target.getX() - this.getX();
            double d2 = d0 - this.getY();
            double d3 = target.getZ() - this.getZ();
            double d4 = Math.sqrt(d1 * d1 + d3 * d3);
            
            ThrownPotion thrownpotion = new ThrownPotion(this.level(), this);
            thrownpotion.setItem(mainHand); // Utilise la potion tenue (infinie car non consommée ici)
            thrownpotion.setXRot(thrownpotion.getXRot() - -20.0F);
            thrownpotion.shoot(d1, d2 + d4 * 0.2D, d3, 0.75F, 8.0F);
            
            if (!this.isSilent()) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), net.minecraft.sounds.SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
            }

            this.level().addFreshEntity(thrownpotion);
        } else {
            // Logique Archer (existante)
            ItemStack ammoStack = new ItemStack(Items.ARROW);
            ItemStack weapon = mainHand.is(Items.BOW) ? mainHand : new ItemStack(Items.BOW);
            
            AbstractArrow abstractarrow = ProjectileUtil.getMobArrow(this, ammoStack, velocity, weapon);
            double d0 = target.getX() - this.getX();
            double d1 = target.getY(0.33D) - abstractarrow.getY();
            double d2 = target.getZ() - this.getZ();
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            abstractarrow.shoot(d0, d1 + d3 * 0.2D, d2, 1.6F, (float)(14 - this.level().getDifficulty().getId() * 4));
            this.playSound(net.minecraft.sounds.SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.level().addFreshEntity(abstractarrow);
        }
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
    public boolean isImmobile() {
        return super.isImmobile() || this.isKO();
    }

    @Override
    public boolean isEffectiveAi() {
        return super.isEffectiveAi() && !this.isKO();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.updateSwingTime();
        
        if (!this.level().isClientSide) {
            if (this.isKO()) {
                this.setPose(Pose.SLEEPING);
                this.setZza(0.0F);
                this.setYya(0.0F);
                this.setXxa(0.0F);
                
                int timer = this.getKOTimer();
                if (timer > 0) {
                    this.setKOTimer(timer - 1);
                    if (this.tickCount % 20 == 0) {
                        ((net.minecraft.server.level.ServerLevel)this.level()).sendParticles(net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR, this.getX(), this.getY() + 0.5D, this.getZ(), 1, 0.2D, 0.2D, 0.2D, 0.0D);
                    }
                } else {
                    // Mort réelle après le timer : On désactive le mode KO AVANT de mourir pour éviter la boucle
                    this.setKO(false);
                    this.setHealth(0.0F);
                    super.die(this.damageSources().generic());
                }
                return;
            }

            // Effet de résistance gratuit avec un bouclier
            if (this.tickCount % 20 == 0 && (this.getOffhandItem().is(Items.SHIELD) || this.getMainHandItem().is(Items.SHIELD))) {
                this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 40, 0, false, false, false));
            }
            
            if (this.isTame() && this.getOwner() instanceof Player player) {
                // Synchronisation du sneak
                boolean ownerSneaking = player.isShiftKeyDown();
                this.setShiftKeyDown(ownerSneaking);
                
                // Gestion des Poses (Hors KO)
                if (this.isOrderedToSit()) {
                    this.setPose(Pose.SITTING);
                } else {
                    this.setPose(ownerSneaking ? Pose.CROUCHING : Pose.STANDING);
                }

                // Intelligence Alchimiste : Switch de potion selon la cible (uniquement si pas en train de soigner)
                if (!this.isSupporting && this.tickCount % 5 == 0 && this.getTarget() != null) {
                    this.updateAlchemistWeapon(this.getTarget());
                }

                AttributeInstance moveSpeed = this.getAttribute(Attributes.MOVEMENT_SPEED);
                if (moveSpeed != null) {
                    if (ownerSneaking) {
                        if (!moveSpeed.hasModifier(SNEAKING_SPEED_MODIFIER_ID)) {
                            moveSpeed.addTransientModifier(SNEAKING_SPEED_MODIFIER);
                        }
                        
                        // Force les attaquants à lâcher la cible s'ils existent
                        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            for (Monster monster : serverLevel.getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(16.0D))) {
                                if (monster.getTarget() == this) {
                                    monster.setTarget(null);
                                }
                            }
                        }
                    } else {
                        moveSpeed.removeModifier(SNEAKING_SPEED_MODIFIER_ID);
                    }
                }

                if (ownerSneaking && this.getTarget() != null) {
                    this.setTarget(null);
                }

                double distanceSq = this.distanceToSqr(player);
                if (distanceSq > 64.0D && this.getNavigation().isInProgress()) this.setSprinting(true);
                else if (distanceSq < 16.0D) this.setSprinting(false);
                if (!this.isOrderedToSit() && distanceSq > 576.0D) this.teleportToOwner(player);
            }
        }
    }

    private void updateAlchemistWeapon(LivingEntity target) {
        // 1. Priorité aux Debuffs (Faiblesse, Lenteur, Poison)
        int bestSlot = findBestDebuffPotion(target);
        
        // 2. Si pas de debuff à appliquer, on cherche les dégâts purs
        if (bestSlot == -1) {
            boolean targetIsUndead = target.isInvertedHealAndHarm();
            for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                ItemStack stack = this.inventory.getItem(i);
                if (stack.getItem() instanceof ThrowablePotionItem) {
                    PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
                    if (contents != null) {
                        boolean hasHeal = false;
                        boolean hasHarm = false;
                        
                        for (net.minecraft.world.effect.MobEffectInstance effect : contents.getAllEffects()) {
                            if (effect.getEffect().is(MobEffects.HEAL)) hasHeal = true;
                            if (effect.getEffect().is(MobEffects.HARM)) hasHarm = true;
                        }
                        
                        if (targetIsUndead && hasHeal) {
                            bestSlot = i;
                            break;
                        } else if (!targetIsUndead && hasHarm) {
                            bestSlot = i;
                            break;
                        }
                    }
                }
            }
        }

        if (bestSlot != -1) {
            this.swapToSlot(bestSlot);
        }
    }

    private int findBestDebuffPotion(LivingEntity target) {
        // Liste des debuffs par ordre de priorité tactique
        net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>[] debuffs = new net.minecraft.core.Holder[]{
            MobEffects.WEAKNESS, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.POISON
        };
        
        for (net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> debuff : debuffs) {
            int slot = findPotionWithEffect(debuff);
            // On ne lance que si on a la potion ET que l'ennemi n'a pas déjà l'effet
            if (slot != -1 && !target.hasEffect(debuff)) {
                return slot;
            }
        }
        return -1;
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
        
        // REANIMATION (Disponible pour tous les aventuriers KO)
        if (this.isKO() && itemstack.is(Items.GOLDEN_APPLE)) {
            if (!player.getAbilities().instabuild) itemstack.shrink(1);
            
            this.setKO(false);
            this.setHealth(this.getMaxHealth() / 2.0F);
            this.setPose(Pose.STANDING);
            
            // Apprivoisement automatique si sauvetage par un joueur
            if (!this.isTame()) {
                this.tame(player);
            }
            
            this.level().broadcastEntityEvent(this, (byte) 7); // Particules de coeur
            if (!this.level().isClientSide) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.everythingwehavealwayswanted.adventurer.revived", this.getCustomName().getString()).withStyle(net.minecraft.ChatFormatting.GREEN), true);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.isKO()) return InteractionResult.PASS;

        if (this.isTame() && this.isOwnedBy(player)) {
            // Shift + Clic Droit : Assis / Debout
            if (player.isShiftKeyDown()) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.jumping = false;
                this.navigation.stop();
                this.setTarget(null);
                
                if (!this.level().isClientSide) {
                    String key = this.isOrderedToSit() ? "message.everythingwehavealwayswanted.adventurer.waiting" : "message.everythingwehavealwayswanted.adventurer.following";
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable(key), true);
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

    private void swapToSlot(int slot) {
        ItemStack targetStack = this.inventory.getItem(slot);
        ItemStack currentStack = this.getMainHandItem();
        if (!ItemStack.isSameItemSameComponents(targetStack, currentStack)) {
            this.setItemSlot(EquipmentSlot.MAINHAND, targetStack.copy());
            this.inventory.setItem(slot, currentStack.copy());
            this.reassessAttackGoals();
        }
    }

    private int findBestSupportPotion(LivingEntity subject) {
        float healthPercent = subject.getHealth() / subject.getMaxHealth();
        
        // 1. Priorité Soins si PV < 50%
        if (healthPercent <= 0.5F) {
            // D'abord Régénération si pas déjà actif
            if (!subject.hasEffect(MobEffects.REGENERATION)) {
                int slot = findPotionWithEffect(MobEffects.REGENERATION);
                if (slot != -1) return slot;
            }
            // Sinon Soin Instantané
            int slot = findPotionWithEffect(MobEffects.HEAL);
            if (slot != -1) return slot;
        }

        // 2. Priorité Buffs si manquants
        net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>[] buffs = new net.minecraft.core.Holder[]{
            MobEffects.DAMAGE_BOOST, MobEffects.MOVEMENT_SPEED, MobEffects.DAMAGE_RESISTANCE, 
            MobEffects.FIRE_RESISTANCE, MobEffects.JUMP, MobEffects.INVISIBILITY, MobEffects.NIGHT_VISION
        };
        
        for (net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> buff : buffs) {
            // Optimisation : On ne vérifie l'effet que si on a la potion correspondante
            int slot = findPotionWithEffect(buff);
            if (slot != -1 && !subject.hasEffect(buff)) {
                return slot;
            }
        }
        
        return -1;
    }

    private int findPotionWithEffect(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.getItem() instanceof ThrowablePotionItem) {
                PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
                if (contents != null) {
                    for (net.minecraft.world.effect.MobEffectInstance instance : contents.getAllEffects()) {
                        if (instance.getEffect().is(effect)) {
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }
}
