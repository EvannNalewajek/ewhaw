package com.minecraft.mod.ewhaw;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import com.minecraft.mod.ewhaw.registry.ModEntityTypes;
import com.minecraft.mod.ewhaw.registry.ModMenuTypes;
import com.minecraft.mod.ewhaw.client.renderer.AdventurerRenderer;
import com.minecraft.mod.ewhaw.client.renderer.HumanRenderer;
import com.minecraft.mod.ewhaw.client.renderer.MortarShellRenderer;
import com.minecraft.mod.ewhaw.client.screen.AdventurerScreen;
import com.minecraft.mod.ewhaw.client.screen.MortarScreen;

@Mod(value = EverythingWeHaveAlwaysWanted.MODID, dist = Dist.CLIENT)
public class EverythingWeHaveAlwaysWantedClient {
    
    public EverythingWeHaveAlwaysWantedClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::registerScreens);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        EverythingWeHaveAlwaysWanted.LOGGER.info("HELLO FROM CLIENT SETUP");
        EverythingWeHaveAlwaysWanted.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

        event.enqueueWork(() -> {
            net.minecraft.client.renderer.item.ItemProperties.register(com.minecraft.mod.ewhaw.registry.ModItems.INVERTED_BOW.get(), 
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("pull"), (stack, level, entity, seed) -> {
                if (entity == null) {
                    return 0.0F;
                } else {
                    return entity.getUseItem() != stack ? 0.0F : (float)(stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0F;
                }
            });
            net.minecraft.client.renderer.item.ItemProperties.register(com.minecraft.mod.ewhaw.registry.ModItems.INVERTED_BOW.get(), 
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("pulling"), (stack, level, entity, seed) -> {
                return entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F;
            });
        });
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.MORTAR_SHELL.get(), MortarShellRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.HUMAN.get(), HumanRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ADVENTURER.get(), AdventurerRenderer::new);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.MORTAR_MENU.get(), MortarScreen::new);
        event.register(ModMenuTypes.ADVENTURER_MENU.get(), AdventurerScreen::new);
    }
}
