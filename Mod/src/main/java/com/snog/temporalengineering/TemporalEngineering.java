package com.snog.temporalengineering;

import com.snog.temporalengineering.common.registry.ModBlocks;
import com.snog.temporalengineering.common.registry.ModBlockEntities;
import com.snog.temporalengineering.common.registry.ModItems;
import com.snog.temporalengineering.common.registry.ModMenuTypes;
import com.snog.temporalengineering.common.config.TemporalConfig;
import com.snog.temporalengineering.common.temporal.TemporalAdapters;
import com.snog.temporalengineering.api.TemporalAdapterRegistry;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TemporalEngineering.MODID)
public class TemporalEngineering {

    public static final String MODID = "temporalengineering";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public TemporalEngineering()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // register content
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        // register config (server)
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, TemporalConfig.SPEC);

        // optional: listen for config reloads (modEventBus)
        modEventBus.addListener(this::onConfigReload);

        // common setup: register adapters, then lock registry
        modEventBus.addListener(this::onCommonSetup);

        // forge bus for runtime events
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            TemporalAdapters.register();
            TemporalAdapterRegistry.lock();
            TemporalEngineering.LOGGER.info("Temporal adapters registered and registry locked.");
        });
    }

    private void onConfigReload(final ModConfigEvent.Reloading evt) {
        if (evt.getConfig().getSpec() == TemporalConfig.SPEC) {
            // config reloaded — if you need to react at runtime, do it here.
            // currently classes read config values live each tick.
            TemporalEngineering.LOGGER.info("TemporalEngineering server config reloaded.");
        }
    }
}