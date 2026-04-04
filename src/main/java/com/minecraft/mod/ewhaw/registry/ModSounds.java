package com.minecraft.mod.ewhaw.registry;

import com.minecraft.mod.ewhaw.EverythingWeHaveAlwaysWanted;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, EverythingWeHaveAlwaysWanted.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> TWANG =
            SOUND_EVENTS.register("twang", 
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(EverythingWeHaveAlwaysWanted.MODID, "twang")));

    public static final DeferredHolder<SoundEvent, SoundEvent> CROA =
            SOUND_EVENTS.register("croa", 
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(EverythingWeHaveAlwaysWanted.MODID, "croa")));

    public static final DeferredHolder<SoundEvent, SoundEvent> CROA_HURT =
            SOUND_EVENTS.register("croa_hurt", 
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(EverythingWeHaveAlwaysWanted.MODID, "croa_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> CROA_AGREE =
            SOUND_EVENTS.register("croa_agree", 
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(EverythingWeHaveAlwaysWanted.MODID, "croa_agree")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
