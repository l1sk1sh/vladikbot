package com.multiheaded.disbot.settings;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Oliver Johnson
 */
public class Constants {
    static final String SETTINGS_JSON = "settings.json";

    public static final Integer DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000;

    public static Map<String, String> FORMAT_EXTENSION;

    static {
        FORMAT_EXTENSION = new HashMap<>();
        FORMAT_EXTENSION.put("HtmlDark", ".html");
        FORMAT_EXTENSION.put("PlainText", ".txt");
    }

    public final static String PLAY_EMOJI = "\u25B6"; // ▶
    public final static String PAUSE_EMOJI = "\u23F8"; // ⏸
    public final static String STOP_EMOJI = "\u23F9"; // ⏹
    public final static String PROGRESS_EMOJI = "\uD83D\uDD18"; //🔘
    public final static String HEADPHONES_EMOJI = "\uD83C\uDFA7"; // 🎧
    public final static String VOLUME_OFF_EMOJI = "\uD83D\uDD07"; //🔇
    public final static String VOLUME_30_EMOJI = "\uD83D\uDD08"; //🔈
    public final static String VOLUME_70_EMOJI = "\uD83D\uDD09"; //🔉
    public final static String VOLUME_100_EMOJI = "\uD83D\uDD0A"; //🔊

}
