
package com.snog.temporalengineering.api;

import com.snog.temporalengineering.TemporalEngineering;
import com.snog.temporalengineering.common.temporal.TemporalTime;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for temporal adapters.
 * Safe by design: no crashes allowed.
 * Adds: priorities, conflict detection, and registry locking.
 */
public final class TemporalAdapterRegistry
{
    private static final Map<Class<?>, List<AdapterEntry>> ADAPTERS = new ConcurrentHashMap<>();
    private static volatile boolean LOCKED = false;

    private TemporalAdapterRegistry()
    {
    }

    public static final class AdapterEntry
    {
        public final TemporalAdapter adapter;
        public final int priority; // higher wins
        public final String id;

        public AdapterEntry(TemporalAdapter adapter, int priority, String id)
        {
            this.adapter = adapter;
            this.priority = priority;
            this.id = (id == null || id.isBlank()) ? adapter.getClass().getName() : id;
        }
    }

    /** Lock registry against late registrations. Call after mod setup. */
    public static void lock()
    {
        LOCKED = true;
    }

    /** Register an adapter with default priority = 0. */
    public static <T extends BlockEntity> void register(Class<T> blockEntityClass, TemporalAdapter adapter)
    {
        register(blockEntityClass, adapter, 0, null);
    }

    /** Register an adapter with priority and ID (for clearer logs). */
    public static <T extends BlockEntity> void register(Class<T> blockEntityClass, TemporalAdapter adapter, int priority, String id)
    {
        if (LOCKED)
        {
            TemporalEngineering.LOGGER.warn("Adapter registration rejected after lock: {}", blockEntityClass.getName());
            return;
        }

        ADAPTERS.computeIfAbsent(blockEntityClass, k -> new ArrayList<>())
                .add(new AdapterEntry(adapter, priority, id));
    }

    // -------------------- Application --------------------

    /**
     * Back-compat: tryApply with generic 'Adapter' source.
     */
    public static boolean tryApply(BlockEntity be, float multiplier, int durationTicks)
    {
        return tryApplyWithSource(be, multiplier, durationTicks, "Adapter");
    }

    /**
     * Preferred: apply with an explicit source (e.g., "Field").
     * This allows BEs implementing ITemporalStatusHud to show richer UI feedback.
     */
    public static boolean tryApplyWithSource(BlockEntity be, float multiplier, int durationTicks, String source)
    {
        if (be == null) return false;

        // Centralized cap enforcement
        float effective = TemporalTime.computeEffectiveMultiplier(be, multiplier);

        // 1) Native support
        if (be instanceof ITemporalAffectable affectable)
        {
            affectable.applyTimeMultiplier(effective, durationTicks);

            // Optional HUD feedback
            if (be instanceof ITemporalStatusHud hud)
            {
                hud.notifyTemporalEffect(multiplier, effective, durationTicks, source);
            }
            return true;
        }

        // 2) Adapter lookup with priorities + conflict detection
        Class<?> beClass = be.getClass();
        AdapterEntry winner = null;
        List<AdapterEntry> matches = new ArrayList<>();

        for (Map.Entry<Class<?>, List<AdapterEntry>> entry : ADAPTERS.entrySet())
        {
            if (entry.getKey().isAssignableFrom(beClass))
            {
                matches.addAll(entry.getValue());
            }
        }

        if (matches.isEmpty())
        {
            return false;
        }

        // Select highest priority adapter; warn if conflicts (same highest priority)
        matches.sort(Comparator.comparingInt(e -> -e.priority));
        winner = matches.get(0);

        if (matches.size() > 1 && matches.get(1).priority == winner.priority)
        {
            TemporalEngineering.LOGGER.warn(
                "TemporalAdapter conflict on {}: multiple adapters share highest priority={} (winner={} but others present).",
                beClass.getName(), winner.priority, winner.id
            );
        }

        try
        {
            boolean ok = winner.adapter.apply(be, effective, durationTicks);

            // Optional HUD feedback (non-native path)
            if (ok && be instanceof ITemporalStatusHud hud)
            {
                hud.notifyTemporalEffect(multiplier, effective, durationTicks, "Adapter");
            }
            return ok;
        }
        catch (Throwable t)
        {
            // SAFETY RULE: adapters are allowed to fail silently
            return false;
        }
    }
}
