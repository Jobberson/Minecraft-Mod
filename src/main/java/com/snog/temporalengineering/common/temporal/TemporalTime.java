package com.snog.temporalengineering.common.temporal;

import com.snog.temporalengineering.common.config.TemporalConfig;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

/**
 * Central authority for all temporal math:
 * - Computes effective multiplier with global + per-machine caps
 * - Clamps per-tick deltas
 * - Applies emergency clamps for invalid values (NaN/Inf)
 *
 * Keep this class stateless. Read config live to allow in-game tuning.
 */
public final class TemporalTime
{
    private TemporalTime()
    {
    }

    /**
     * Compute an effective multiplier after applying caps and safety checks.
     * Native ITemporalAffectable machines are allowed higher caps than non-native,
     * but both are still subject to per-machine and global rules.
     */
    public static float computeEffectiveMultiplier(BlockEntity be, float requestedMultiplier)
    {
        float m = requestedMultiplier;

        // Emergency clamp: invalid or non-positive requests fall back to 1x
        if (!Float.isFinite(m) || m <= 0f)
        {
            return 1.0f;
        }

        // Per-machine cap (optional map); if none, returns Float.POSITIVE_INFINITY
        float perMachineCap = readPerMachineCap(be);
        if (Float.isFinite(perMachineCap))
        {
            m = Math.min(m, perMachineCap);
        }

        // Non-native global cap (applies when target does NOT implement ITemporalAffectable)
        boolean nativeAffectable = be instanceof com.snog.temporalengineering.api.ITemporalAffectable;
        if (!nativeAffectable)
        {
            double cap = TemporalConfig.NON_NATIVE_MAX_MULTIPLIER.get();
            m = Math.min(m, (float) cap);
        }

        // Final emergency clamp
        if (!Float.isFinite(m) || m <= 0f)
        {
            m = 1.0f;
        }

        return m;
    }

    /**
     * Clamp a computed per-tick delta to a machine-specific or global maximum.
     * Use for progress/work deltas to prevent skipping recipe logic.
     */
    public static float clampDeltaPerTick(BlockEntity be, float computedDelta)
    {
        if (!Float.isFinite(computedDelta) || computedDelta <= 0f)
        {
            return 0f;
        }

        float maxDelta = readMaxDeltaFor(be);
        if (Float.isFinite(maxDelta))
        {
            return Math.min(computedDelta, maxDelta);
        }

        return computedDelta;
    }

    /**
     * Reads a per-machine max multiplier from config list entries.
     * Format: "fully.qualified.ClassName = value"
     * Example default: "com.snog.temporalengineering.common.blockentity.TemporalProcessorBlockEntity = 6.0"
     *
     * Returns Float.POSITIVE_INFINITY when no entry matches (meaning "no extra cap here").
     */
    private static float readPerMachineCap(BlockEntity be)
    {
        try
        {
            String target = be.getClass().getName();
            for (String entry : TemporalConfig.PER_MACHINE_MAX_MULTIPLIER.get())
            {
                String line = entry.trim();
                if (line.isEmpty()) continue;

                // Accept "Class=val" or "Class : val" formats
                String[] parts = line.split("[=:]");
                if (parts.length < 2) continue;

                String className = parts[0].trim();
                String valueStr  = parts[1].trim();

                if (className.equals(target))
                {
                    float val = Float.parseFloat(valueStr);
                    if (Float.isFinite(val) && val > 0f)
                    {
                        return val;
                    }
                }
            }
        }
        catch (Throwable ignored)
        {
        }
        return Float.POSITIVE_INFINITY;
    }

    /**
     * Reads the maximum allowed per-tick delta for the given machine.
     * Currently uses a single global fallback; you can extend this later
     * with a per-machine list similar to PER_MACHINE_MAX_MULTIPLIER.
     */
    private static float readMaxDeltaFor(BlockEntity be)
    {
        double globalMax = TemporalConfig.GLOBAL_MAX_DELTA_PER_TICK.get();
        if (globalMax <= 0.0 || !Double.isFinite(globalMax))
        {
            return Float.POSITIVE_INFINITY; // no global cap
        }
        return (float) globalMax;
    }
}
