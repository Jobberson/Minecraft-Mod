package com.snog.temporalengineering.common.registry;

import com.snog.temporalengineering.common.blockentity.TemporalProcessorBlockEntity;
import com.snog.temporalengineering.common.blockentity.TemporalFieldGeneratorBlockEntity;
import com.snog.temporalengineering.TemporalEngineering;

import net.minecraft.world.level.block.entity.BlockEntityType;

import com.snog.temporalengineering.common.blockentity.TemporalProcessorBlockEntity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
        public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, TemporalEngineering.MODID);

        public static final RegistryObject<BlockEntityType<TemporalProcessorBlockEntity>> TEMPORAL_PROCESSOR =
                BLOCK_ENTITIES.register("temporal_processor",
                            () -> BlockEntityType.Builder.of(TemporalProcessorBlockEntity::new,
                                    ModBlocks.TEMPORAL_PROCESSOR.get()).build(null));

        public static final RegistryObject<BlockEntityType<TemporalFieldGeneratorBlockEntity>> TEMPORAL_FIELD_GENERATOR =
                BLOCK_ENTITIES.register("temporal_field_generator",
                        () -> BlockEntityType.Builder.of(TemporalFieldGeneratorBlockEntity::new,
                                ModBlocks.TEMPORAL_FIELD_GENERATOR.get()).build(null));


    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
