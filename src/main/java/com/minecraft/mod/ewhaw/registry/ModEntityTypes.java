package com.minecraft.mod.ewhaw.registry;

import com.minecraft.mod.ewhaw.EverythingWeHaveAlwaysWanted;
import com.minecraft.mod.ewhaw.entity.MortarShellEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, EverythingWeHaveAlwaysWanted.MODID);

    public static final Supplier<EntityType<MortarShellEntity>> MORTAR_SHELL =
            ENTITY_TYPES.register("mortar_shell",
                    () -> EntityType.Builder.<MortarShellEntity>of(MortarShellEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F) // Taille du boulet
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("mortar_shell"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}