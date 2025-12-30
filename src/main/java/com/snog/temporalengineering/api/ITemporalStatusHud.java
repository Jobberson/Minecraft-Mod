package com.snog.temporalengineering.api;

/**
 * Optional interface for BlockEntities that want to display temporal status
 * (requested vs effective multiplier, duration, and source).
 */
public interface ITemporalStatusHud
{
    void notifyTemporalEffect(float requested, float effective, int durationTicks, String source);
}
