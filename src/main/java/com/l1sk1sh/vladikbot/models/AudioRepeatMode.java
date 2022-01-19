package com.l1sk1sh.vladikbot.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * @author Michaili K
 */
@Getter
@RequiredArgsConstructor
public enum AudioRepeatMode {
    OFF(null, "Off"),
    ALL("\uD83D\uDD01", "All"), // 🔁
    SINGLE("\uD83D\uDD02", "Single"); // 🔂

    private final String emoji;
    private final String userFriendlyName;
}
