package com.minecraft.mod.ewhaw.client.renderer;

import com.minecraft.mod.ewhaw.entity.AdventurerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AdventurerRenderer extends MobRenderer<AdventurerEntity, PlayerModel<AdventurerEntity>> {
    private final PlayerModel<AdventurerEntity> wideModel;
    private final PlayerModel<AdventurerEntity> slimModel;

    public AdventurerRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        this.wideModel = this.model;
        this.slimModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);

        this.addLayer(new HumanoidArmorLayer<>(this, 
            new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), 
            new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), 
            context.getModelManager()));
        
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(AdventurerEntity entity) {
        return entity.getTextureLocation();
    }

    @Override
    public void render(AdventurerEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.model = entity.isSlim() ? this.slimModel : this.wideModel;
        this.model.crouching = entity.isCrouching();
        this.model.attackTime = entity.getAttackAnim(partialTicks);

        this.model.rightArmPose = getArmPose(entity, HumanoidArm.RIGHT);
        this.model.leftArmPose = getArmPose(entity, HumanoidArm.LEFT);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private HumanoidModel.ArmPose getArmPose(AdventurerEntity entity, HumanoidArm arm) {
        ItemStack stack = (entity.getMainArm() == arm) ? entity.getMainHandItem() : entity.getOffhandItem();
        if (stack.isEmpty()) return HumanoidModel.ArmPose.EMPTY;
        if (stack.is(Items.BOW) && entity.isAggressive()) return HumanoidModel.ArmPose.BOW_AND_ARROW;
        return HumanoidModel.ArmPose.ITEM;
    }
}
