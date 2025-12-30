package com.snog.temporalengineering.common.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTabs {

    public static final CreativeModeTab TEMPORAL_TAB = new CreativeModeTab("temporalengineering") {
        @Override
        public ItemStack makeIcon() {
            // Use get() safely — at runtime registry will be ready when the tab icon is drawn
            return new ItemStack(ModItems.VOLATILE_EXOTIC_MATTER.get());
        }
    };
}
