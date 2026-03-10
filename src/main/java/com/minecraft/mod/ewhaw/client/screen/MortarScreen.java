package com.minecraft.mod.ewhaw.client.screen;

import com.minecraft.mod.ewhaw.EverythingWeHaveAlwaysWanted;
import com.minecraft.mod.ewhaw.menu.MortarMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MortarScreen extends AbstractContainerScreen<MortarMenu> {
    // On utilise une texture personnalisée pour avoir exactement 3 cases
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(EverythingWeHaveAlwaysWanted.MODID, "textures/gui/mortar.png");

    public MortarScreen(MortarMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166; // Taille standard d'une interface avec l'inventaire du joueur
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // En laissant cette méthode vide, on empêche Minecraft de dessiner le texte par défaut "Inventory" et "Mortar"
        // Si tu voulais dessiner ton propre texte, ce serait ici ! Par exemple :
        // guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }
}