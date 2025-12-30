package com.snog.temporalengineering.common.registry;

import com.snog.temporalengineering.TemporalEngineering;
import com.snog.temporalengineering.common.block.TemporalFieldGeneratorBlock;
import com.snog.temporalengineering.common.block.TemporalProcessorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS 
        = DeferredRegister.create(ForgeRegistries.BLOCKS, TemporalEngineering.MODID);

    public static final RegistryObject<Block> TEMPORAL_PROCESSOR = BLOCKS.register("temporal_processor",
        () -> new TemporalProcessorBlock(Block.Properties.of(Material.METAL)
            .strength(5.0f, 10.0f)
            .sound(SoundType.METAL))
    );

    public static final RegistryObject<Block> TEMPORAL_FIELD_GENERATOR = BLOCKS.register("temporal_field_generator",
    () -> new TemporalFieldGeneratorBlock(Block.Properties.of(Material.METAL)
        .strength(4.0f)
        .sound(SoundType.METAL))
);

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
