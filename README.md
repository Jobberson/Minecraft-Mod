# Temporal Engineering

**Temporal Engineering** is a modular tech mod about **heat**, **coolant**, **Exotic Matter**, and **time manipulation**.

## Non-Goals
- No global tick-rate changes
- No modification of vanilla random ticks
- No permanent world-wide time effects
- No machine that "does everything"

## Core Concepts
- Localized time acceleration/slow via fields and adapters
- Config-driven balancing: caps, per-tick delta clamps
- Costs: energy drain and temporal stability decay (config-gated)

## Key Server Configs
- `adapters.enabled` — master toggle for third-party adapters
- `non_native_max_multiplier` — cap for non-native machines (those without ITemporalAffectable)
- `limits.allow_multi_cycle_in_single_tick` — allow resolving multiple operations per tick
- `limits.global_max_delta_per_tick` — cap on per-tick progress delta
- `limits.per_machine_max_multiplier` — list of per-machine multiplier caps
- `temporal_costs.enabled` — enable energy & stability costs tied to multiplier
- `temporal_costs.energy_drain_base_per_tick` / `energy_drain_scale`
- `temporal_costs.stability_decay_base_per_tick` / `stability_decay_scale`
- `ui.pulse_enabled` — enable UI pulse when multiplier spikes
- `fx.generator_particles_enabled` / `fx.generator_sound_enabled`

## Adapter API
- Implement `TemporalAdapter` to describe how time applies to a foreign BlockEntity.
- Register adapters in common setup, then the registry locks:

  ```java
  TemporalAdapterRegistry.register(TargetBE.class, new SomeAdapter(), 10, "SomeMod-Adapter");

  TemporalAdapterRegistry.lock();

  ```
## Safety Rules
- One tick should never finish multiple operations unless explicitly allowed by config.
- All multipliers and deltas are clamped via the centralized `TemporalTime` utility.
- Adapters are safe to fail; the registry never crashes the server.

## Credits
Design & code by Pedro Schenegoski. 

This README is a quick-start; consult the full design doc for deeper context.