package com.snog.temporalengineering;

import net.minecraftforge.common.ForgeConfigSpec;

public class TemporalConfig {
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

    static {
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

        b.pop();
        SPEC = b.build();
    }
}
