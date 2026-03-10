package com.minecraft.mod.ewhaw.registry;

import com.minecraft.mod.ewhaw.EverythingWeHaveAlwaysWanted;
import com.minecraft.mod.ewhaw.menu.MortarMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, EverythingWeHaveAlwaysWanted.MODID);

    public static final Supplier<MenuType<MortarMenu>> MORTAR_MENU =
            MENUS.register("mortar_menu", () -> IMenuTypeExtension.create(MortarMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}