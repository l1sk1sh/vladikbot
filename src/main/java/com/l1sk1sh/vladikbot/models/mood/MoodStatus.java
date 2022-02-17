package com.l1sk1sh.vladikbot.models.mood;

public enum MoodStatus {
    /**
     * Mood Status is based on coordinates through X and Y axis
     * X axis stands for Destructive/Constructive state
     * Y axis stands for Introverted/Extraverted state
     */

    // Extraverted Destructive
    WRAITH, ANGER,
    // Extraverted Neutral
    CURIOSITY,
    // Extraverted Constructive
    PASSION, ENTHUSIASTIC,
    // Neutral Constructive
    HAPPINESS,
    // True Neutral
    NEUTRAL,
    // Neutral Destructive
    SADNESS,
    // Introverted Destructive
    WONDER, ANNOYANCE,
    // Introverted Neutral
    CAUTION,
    // Introverted Constructive
    CALM, SERENITY

}
