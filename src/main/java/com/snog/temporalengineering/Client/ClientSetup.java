package com.snog.temporalengineering.client;

import com.snog.temporalengineering.TemporalEngineering;
import com.snog.temporalengineering.common.registry.ModMenuTypes;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import com.snog.temporalengineering.client.screen.TemporalProcessorScreen;

import net.minecraft.client.gui.screens.MenuScreens;

@EventBusSubscriber(modid = TemporalEngineering.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MenuScreens.register(
            ModMenuTypes.TEMPORAL_PROCESSOR_MENU.get(),
            TemporalProcessorScreen::new
        );

    }
}
