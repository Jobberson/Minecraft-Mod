package com.snog.temporalengineering.common.registry;

import com.snog.temporalengineering.TemporalEngineering;
import com.snog.temporalengineering.common.registry.ModCreativeTabs;
import com.snog.temporalengineering.common.registry.ModBlocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

        public static final DeferredRegister<Item> ITEMS
            = DeferredRegister.create(ForgeRegistries.ITEMS, TemporalEngineering.MODID);

        public static final RegistryObject<Item> VOLATILE_EXOTIC_MATTER
            = ITEMS.register("volatile_exotic_matter",
                    () -> new Item(new Item.Properties().tab(ModCreativeTabs.TEMPORAL_TAB)));

        public static final RegistryObject<Item> TEMPORAL_PROCESSOR_ITEM
            = ITEMS.register("temporal_processor",
                    () -> new BlockItem(ModBlocks.TEMPORAL_PROCESSOR.get(),
                                new Item.Properties().tab(ModCreativeTabs.TEMPORAL_TAB)));

        public static final RegistryObject<Item> TEMPORAL_FIELD_GENERATOR_ITEM
                = ITEMS.register("temporal_field_generator",
                        () -> new BlockItem(ModBlocks.TEMPORAL_FIELD_GENERATOR.get(),
                                new Item.Properties().tab(ModCreativeTabs.TEMPORAL_TAB)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
