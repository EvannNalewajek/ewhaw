package com.minecraft.mod.ewhaw.item;

import com.minecraft.mod.ewhaw.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.function.Supplier;

public class MegaBucketItem extends BucketItem {
    private final Fluid content;

    public MegaBucketItem(Supplier<? extends Fluid> fluidSupplier, Properties properties) {
        super(fluidSupplier.get(), properties);
        this.content = fluidSupplier.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, this.content == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        
        if (blockhitresult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemstack);
        } else if (blockhitresult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemstack);
        } else {
            BlockPos pos = blockhitresult.getBlockPos();
            Direction direction = blockhitresult.getDirection();
            BlockPos targetPos = pos.relative(direction);

            if (level.mayInteract(player, pos) && player.mayUseItemAt(targetPos, direction, itemstack)) {
                if (this.content == Fluids.EMPTY) {
                    // LOGIQUE : RAMASSER 3x3
                    Fluid pickedFluid = Fluids.EMPTY;
                    boolean pickedUpAny = false;

                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            BlockPos p = pos.offset(x, 0, z);
                            BlockState state = level.getBlockState(p);
                            if (state.getBlock() instanceof BucketPickup bucketPickup) {
                                Fluid foundFluid = state.getFluidState().getType();
                                if (foundFluid != Fluids.EMPTY) {
                                    if (pickedFluid == Fluids.EMPTY) {
                                        pickedFluid = foundFluid;
                                    }
                                    if (foundFluid == pickedFluid) {
                                        bucketPickup.pickupBlock(player, level, p, state);
                                        pickedUpAny = true;
                                    }
                                }
                            }
                        }
                    }

                    if (pickedUpAny) {
                        player.awardStat(Stats.ITEM_USED.get(this));
                        ItemStack filledBucket = ItemStack.EMPTY;
                        if (pickedFluid == Fluids.WATER) filledBucket = new ItemStack(ModItems.MEGA_WATER_BUCKET.get());
                        else if (pickedFluid == Fluids.LAVA) filledBucket = new ItemStack(ModItems.MEGA_LAVA_BUCKET.get());
                        
                        ItemStack result = ItemUtils.createFilledResult(itemstack, player, filledBucket);
                        return InteractionResultHolder.sidedSuccess(result, level.isClientSide());
                    }
                } else {
                    // LOGIQUE : POSER 3x3
                    BlockState currentState = level.getBlockState(pos);
                    BlockPos centerPos = currentState.canBeReplaced(this.content) ? pos : targetPos;
                    
                    boolean placedAny = false;
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            BlockPos p = centerPos.offset(x, 0, z);
                            if (this.emptyContents(player, level, p, blockhitresult)) {
                                this.checkExtraContent(player, level, itemstack, p);
                                placedAny = true;
                            }
                        }
                    }

                    if (placedAny) {
                        player.awardStat(Stats.ITEM_USED.get(this));
                        return InteractionResultHolder.sidedSuccess(getMegaEmptySuccessItem(itemstack, player), level.isClientSide());
                    }
                }
            }
            return InteractionResultHolder.fail(itemstack);
        }
    }

    private ItemStack getMegaEmptySuccessItem(ItemStack stack, Player player) {
        return !player.hasInfiniteMaterials() ? new ItemStack(ModItems.MEGA_BUCKET.get()) : stack;
    }
}
