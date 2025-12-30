
package com.snog.temporalengineering.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.List;

public class TemporalConfig
{
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue HEAT_RATE;
    public static final ForgeConfigSpec.IntValue COOL_RATE;
    public static final ForgeConfigSpec.IntValue TANK_DRAIN_PER_SECOND;
    public static final ForgeConfigSpec.IntValue PROCESSOR_WORK_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue BASE_WORK_PER_TICK;

    public static final ForgeConfigSpec.IntValue FIELD_RADIUS;
    public static final ForgeConfigSpec.IntValue GENERATOR_CONSUME_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue GENERATOR_EFFECT_DURATION_TICKS;
    public static final ForgeConfigSpec.DoubleValue GENERATOR_SPEED_MULTIPLIER;

    public static final ForgeConfigSpec.IntValue EM_THRESHOLD;

    public static final ForgeConfigSpec.BooleanValue UI_PULSE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue FX_GENERATOR_PARTICLES_ENABLED;
    public static final ForgeConfigSpec.BooleanValue FX_GENERATOR_SOUND_ENABLED;

    public static final ForgeConfigSpec.BooleanValue TEMPORAL_COSTS_ENABLED;
    public static final ForgeConfigSpec.DoubleValue ENERGY_DRAIN_BASE_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue ENERGY_DRAIN_SCALE;
    public static final ForgeConfigSpec.DoubleValue STABILITY_DECAY_BASE_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue STABILITY_DECAY_SCALE;

    // --- Adapters section ---
    public static final ForgeConfigSpec.BooleanValue ADAPTERS_ENABLED;
    public static final ForgeConfigSpec.DoubleValue NON_NATIVE_MAX_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue PROGRESS_PATTERN_ENABLED;
    public static final ForgeConfigSpec.DoubleValue PROGRESS_PATTERN_SCALE;
    public static final ForgeConfigSpec.BooleanValue ADAPTER_VANILLA_ENABLED;
    public static final ForgeConfigSpec.BooleanValue ADAPTER_MEKANISM_ENABLED;
    public static final ForgeConfigSpec.BooleanValue ADAPTER_CREATE_ENABLED;

    // --- Limits / Temporal math ---
    public static final ForgeConfigSpec.BooleanValue ALLOW_MULTI_CYCLE_IN_SINGLE_TICK;
    public static final ForgeConfigSpec.DoubleValue GLOBAL_MAX_DELTA_PER_TICK;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> PER_MACHINE_MAX_MULTIPLIER;

    static
    {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("Temporal Engineering server configuration").push("server");

        HEAT_RATE = b.comment("Heat increase per tick when empty")
            .defineInRange("heatRate", 1, 0, 1000);

        COOL_RATE = b.comment("Heat decrease per tick when cooled")
            .defineInRange("coolRate", 2, 0, 1000);

        TANK_DRAIN_PER_SECOND = b.comment("mB drained per second while cooling")
            .defineInRange("tankDrainPerSecond", 10, 0, 10000);

        PROCESSOR_WORK_THRESHOLD = b.comment("Work required to produce one EM (higher => slower)")
            .defineInRange("processorWorkThreshold", 40, 1, 1_000_000);

        BASE_WORK_PER_TICK = b.comment("Base work gained per tick by processors")
            .defineInRange("baseWorkPerTick", 1.0, 0.0, 1000.0);

        FIELD_RADIUS = b.comment("Temporal field radius in blocks")
            .defineInRange("fieldRadius", 3, 0, 64);

        GENERATOR_CONSUME_INTERVAL_TICKS = b.comment("How often generator attempts to consume EM (ticks)")
            .defineInRange("generatorConsumeInterval", 20, 1, 60000);

        GENERATOR_EFFECT_DURATION_TICKS = b.comment("Duration of generator speed effect (ticks)")
            .defineInRange("generatorEffectDuration", 20, 1, 60000);

        GENERATOR_SPEED_MULTIPLIER = b.comment("Speed multiplier applied by generator")
            .defineInRange("generatorSpeedMultiplier", 2.0, 0.1, 100.0);

        EM_THRESHOLD = b.comment("Heat threshold where processor begins producing")
            .defineInRange("emHeatThreshold", 80, 0, 10000);

        // --- temporal costs subsection ---
        b.push("temporal_costs");
        TEMPORAL_COSTS_ENABLED = b.comment("Enable energy/stability costs tied to multiplier")
            .define("enabled", true);
        ENERGY_DRAIN_BASE_PER_TICK = b.comment("Base energy drain per tick when multiplier > 1")
            .defineInRange("energy_drain_base_per_tick", 2.0, 0.0, 1_000_000.0);
        ENERGY_DRAIN_SCALE = b.comment("Non-linear scale applied to multiplier (e.g., 2.0 for quadratic)")
            .defineInRange("energy_drain_scale", 2.0, 0.0, 10.0);
        STABILITY_DECAY_BASE_PER_TICK = b.comment("Base stability decay per tick when multiplier > 1")
            .defineInRange("stability_decay_base_per_tick", 0.5, 0.0, 1_000_000.0);
        STABILITY_DECAY_SCALE = b.comment("Scale applied to multiplier for instability growth")
            .defineInRange("stability_decay_scale", 1.5, 0.0, 10.0);
        b.pop();

        // --- effects subsection ---
        b.push("ui");
        UI_PULSE_ENABLED = b.define("pulse_enabled", true);
        b.pop();
    
        b.push("fx");
        FX_GENERATOR_PARTICLES_ENABLED = b.define("generator_particles_enabled", true);
        FX_GENERATOR_SOUND_ENABLED = b.define("generator_sound_enabled", true);
        b.pop();

        // --- adapters subsection ---
        b.push("adapters");

        ADAPTERS_ENABLED = b.comment("Global master toggle for adapters (true = adapters active)")
            .define("enabled", true);

        NON_NATIVE_MAX_MULTIPLIER = b.comment("Default cap on multiplier applied to non-native machines")
            .defineInRange("non_native_max_multiplier", 4.0, 0.1, 100.0);

        PROGRESS_PATTERN_ENABLED = b.comment("Enable the progress-pattern adapter (applies pattern-based acceleration)")
            .define("progress_pattern_enabled", true);

        PROGRESS_PATTERN_SCALE = b.comment("Scale applied by the progress-pattern adapter (pack tuning)")
            .defineInRange("progress_pattern_scale", 1.0, 0.0, 100.0);

        ADAPTER_VANILLA_ENABLED = b.comment("Enable vanilla adapter (affects vanilla furnaces/blocks if implemented)")
            .define("adapter_vanilla_enabled", true);

        ADAPTER_MEKANISM_ENABLED = b.comment("Enable Mekanism adapter")
            .define("adapter_mekanism_enabled", true);

        ADAPTER_CREATE_ENABLED = b.comment("Enable Create adapter")
            .define("adapter_create_enabled", false);

        b.pop(); // end adapters

        // --- limits subsection ---
        b.push("limits");

        ALLOW_MULTI_CYCLE_IN_SINGLE_TICK = b.comment("Permit resolving multiple production cycles in a single tick")
            .define("allow_multi_cycle_in_single_tick", false);

        GLOBAL_MAX_DELTA_PER_TICK = b.comment("Global fallback cap for per-tick progress delta (work units)")
            .defineInRange("global_max_delta_per_tick", 4.0, 0.0, 1_000_000.0);

        PER_MACHINE_MAX_MULTIPLIER = b.comment("Per-machine max multiplier list; entries 'fully.qualified.ClassName = value'")
            .defineList("per_machine_max_multiplier",
                List.of("com.snog.temporalengineering.common.blockentity.TemporalProcessorBlockEntity = 6.0"),
                o -> o instanceof String);

        b.pop(); // end limits

        b.pop(); // end server

        SPEC = b.build();
    }
}
