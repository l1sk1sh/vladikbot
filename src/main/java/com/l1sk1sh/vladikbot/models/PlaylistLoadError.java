package com.l1sk1sh.vladikbot.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Extracted class
 * @author John Grosh
 */
@Getter
@AllArgsConstructor
public class PlaylistLoadError {
    private final int number;
    private final String item;
    private final String reason;
}
