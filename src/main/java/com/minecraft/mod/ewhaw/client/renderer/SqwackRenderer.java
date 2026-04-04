package com.minecraft.mod.ewhaw.client.renderer;

import com.minecraft.mod.ewhaw.entity.SqwackEntity;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SqwackRenderer extends MobRenderer<SqwackEntity, PlayerModel<SqwackEntity>> {
public SqwackRenderer(EntityRendererProvider.Context context) {
    // On crée un modèle personnalisé anonyme pour surcharger setupAnim
    super(context, new PlayerModel<SqwackEntity>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true) {
        @Override
        public void setupAnim(SqwackEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            // On utilise isInSittingPose qui est correctement synchronisé
            this.riding = entity.isInSittingPose();
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

            if (entity.isAdmiring()) {
                    this.head.xRot = 0.5F;
                    this.head.yRot = 0.0F;
                    this.hat.copyFrom(this.head);
                    this.leftArm.xRot = -1.0F; 
                    this.leftArm.yRot = 0.5F;
                    this.leftArm.zRot = 0.1F;
                    this.leftSleeve.copyFrom(this.leftArm); 
                }
            }
        }, 0.5f);
        
        this.addLayer(new HumanoidArmorLayer<>(this, 
            new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), 
            new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), 
            context.getModelManager()));
            
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public void render(SqwackEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Synchronisation visuelle avec l'état synchronisé par Minecraft
        boolean sitting = entity.isInSittingPose();
        this.model.riding = sitting;
        this.model.attackTime = entity.getAttackAnim(partialTicks);
        this.model.leftArmPose = entity.isAdmiring() ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.EMPTY;
        
        if (entity.isTame() && entity.isAggressive()) {
            ItemStack mainHand = entity.getMainHandItem();
            if (mainHand.is(Items.BOW) || mainHand.is(com.minecraft.mod.ewhaw.registry.ModItems.INVERTED_BOW.get())) {
                this.model.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
            } else {
                this.model.rightArmPose = HumanoidModel.ArmPose.ITEM;
            }
        } else {
            this.model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        }

        poseStack.pushPose();
        if (sitting) {
            poseStack.translate(0.0D, -0.6D, 0.0D);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(SqwackEntity entity) {
        return entity.getTextureLocation();
    }
}
