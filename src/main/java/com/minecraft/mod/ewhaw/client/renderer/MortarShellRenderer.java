package com.minecraft.mod.ewhaw.client.renderer;

import com.minecraft.mod.ewhaw.entity.MortarShellEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public class MortarShellRenderer extends ThrownItemRenderer<MortarShellEntity> {
    public MortarShellRenderer(EntityRendererProvider.Context context) {
        // Rends l'entité visuellement comme un bloc de fer par exemple, ou une charge de feu (Fire Charge)
        super(context, 1.0f, true);
    }
}