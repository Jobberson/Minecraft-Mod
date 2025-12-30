package com.snog.temporalengineering.api;

import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Adapter that applies temporal acceleration
 * to foreign BlockEntities.
 */
@FunctionalInterface
public interface TemporalAdapter {

    /**
     * @param be target block entity
     * @param multiplier time multiplier
     * @param durationTicks duration
     * @return true if applied successfully
     */
    boolean apply(BlockEntity be, float multiplier, int durationTicks);
}
