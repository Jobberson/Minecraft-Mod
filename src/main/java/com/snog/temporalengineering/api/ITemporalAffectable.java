package com.snog.temporalengineering.api;

/**
 * Implement this on BlockEntities that can natively
 * respond to temporal acceleration.
 *
 * Multiplier should scale internal work, NOT tick rate.
 */
public interface ITemporalAffectable {

    /**
     * Apply a time multiplier for a duration.
     *
     * @param multiplier value >= 1.0f
     * @param durationTicks how long the effect lasts
     */
    void applyTimeMultiplier(float multiplier, int durationTicks);
}
