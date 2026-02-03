package com.snog.temporalengineering.client;

import com.snog.temporalengineering.TemporalEngineering;
import com.snog.temporalengineering.client.render.TemporalFieldGeneratorRenderer;
import com.snog.temporalengineering.client.screen.TemporalFieldGeneratorScreen;
import com.snog.temporalengineering.client.screen.TemporalProcessorScreen;
import com.snog.temporalengineering.common.registry.ModBlockEntities;
import com.snog.temporalengineering.common.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TemporalEngineering.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientSetup
{
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            MenuScreens.register(
                ModMenuTypes.TEMPORAL_PROCESSOR_MENU.get(),
                TemporalProcessorScreen::new
            );

            MenuScreens.register(
                ModMenuTypes.TEMPORAL_FIELD_GENERATOR_MENU.get(),
                TemporalFieldGeneratorScreen::new
            );

            BlockEntityRenderers.register(
                ModBlockEntities.TEMPORAL_FIELD_GENERATOR.get(),
                TemporalFieldGeneratorRenderer::new
            );
        });
    }
}