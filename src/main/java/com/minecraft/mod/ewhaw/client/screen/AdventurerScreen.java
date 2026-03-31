package com.minecraft.mod.ewhaw.client.screen;

import com.minecraft.mod.ewhaw.EverythingWeHaveAlwaysWanted;
import com.minecraft.mod.ewhaw.menu.AdventurerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AdventurerScreen extends AbstractContainerScreen<AdventurerMenu> {
    // Utilisation de TA texture personnalisée
    private static final ResourceLocation ADVENTURER_INVENTORY_LOCATION = 
        ResourceLocation.fromNamespaceAndPath(EverythingWeHaveAlwaysWanted.MODID, "textures/gui/container/adventurer_inventory.png");

    public AdventurerScreen(AdventurerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // On laisse vide pour ne pas afficher de texte automatique par-dessus ta texture
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        
        // On dessine TA texture
        guiGraphics.blit(ADVENTURER_INVENTORY_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
        
        // Rendu de l'Aventurier en 3D
        // Ajuste les coordonnées (i + 51, j + 75) si ton personnage n'est pas bien aligné sur ton dessin
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, i + 26, j + 8, i + 75, j + 78, 30, 0.0625F, (float)mouseX, (float)mouseY, this.menu.getAdventurer());
    }
}
