package com.snog.temporalengineering.common.registry;

import com.snog.temporalengineering.TemporalEngineering;
import com.snog.temporalengineering.common.menu.TemporalProcessorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.common.extensions.IForgeMenuType;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, TemporalEngineering.MODID);

    public static final RegistryObject<MenuType<TemporalProcessorMenu>> TEMPORAL_PROCESSOR_MENU =
            MENUS.register("temporal_processor",
                () -> IForgeMenuType.create(TemporalProcessorMenu::new)
            );

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
