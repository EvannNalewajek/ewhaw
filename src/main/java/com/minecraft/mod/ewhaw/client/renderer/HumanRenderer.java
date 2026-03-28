package com.minecraft.mod.ewhaw.client.renderer;

import com.minecraft.mod.ewhaw.EverythingWeHaveAlwaysWanted;
import com.minecraft.mod.ewhaw.entity.HumanEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HumanRenderer extends MobRenderer<HumanEntity, PlayerModel<HumanEntity>> {
    // Utilisation de la texture personnalisée du mod
    private static final ResourceLocation STEVE_TEXTURE = ResourceLocation.fromNamespaceAndPath(EverythingWeHaveAlwaysWanted.MODID, "textures/entity/steve.png");

    public HumanRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(HumanEntity entity) {
        return STEVE_TEXTURE;
    }
}
