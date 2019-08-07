package com.multiheaded.vladikbot.settings;

import net.dv8tion.jda.core.Permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Oliver Johnson
 */
public class Constants {
    public final static List<Permission> RECOMMENDED_PERMS = new ArrayList<>();

    static {
        RECOMMENDED_PERMS.add(Permission.MESSAGE_READ);
        RECOMMENDED_PERMS.add(Permission.MESSAGE_WRITE);
        RECOMMENDED_PERMS.add(Permission.MESSAGE_HISTORY);
        RECOMMENDED_PERMS.add(Permission.MESSAGE_ADD_REACTION);
        RECOMMENDED_PERMS.add(Permission.MESSAGE_EMBED_LINKS);
        RECOMMENDED_PERMS.add(Permission.MESSAGE_ATTACH_FILES);
        RECOMMENDED_PERMS.add(Permission.MESSAGE_MANAGE);
        RECOMMENDED_PERMS.add(Permission.MESSAGE_EXT_EMOJI);
        RECOMMENDED_PERMS.add(Permission.MANAGE_CHANNEL);
        RECOMMENDED_PERMS.add(Permission.VOICE_CONNECT);
        RECOMMENDED_PERMS.add(Permission.VOICE_SPEAK);
        RECOMMENDED_PERMS.add(Permission.NICKNAME_CHANGE);
    }

    static final String BOT_SETTINGS_JSON = "settings_bot.json";
    static final String GUILD_SETTINGS_JSON = "settings_guild.json";
    public static final String STATUSES_JSON = "rotation.json";

    public static final Integer DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000;
    public static final Integer EIGHT_MEGABYTES_IN_BYTES = 8388608;
    public static final Integer STATUSES_ROTATION_FREQUENCY_IN_SECONDS = 30 * 60;

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
    public static final String[] SUPPORTED_MEDIA_FORMATS = {".jpg", ".png", ".mp4"};
    public static final String JSON_EXTENSION = ".json";
    public static final String HTML_EXTENSION = ".html";
    public static final String TXT_EXTENSION = ".txt";

    public static final String ACTION_PLAYING = "playing";
    public static final String ACTION_WATCHING = "watching";
    public static final String ACTION_LISTENING = "listening";
    public static final String ACTION_STREAMING = "streaming";

    public static final String YT_SEARCH_PREFIX = "ytsearch:";
    public static final String SC_SEARCH_PREFIX = "scsearch:";

    public static final String BACKUP_HTML_DARK = "HtmlDark";
    public static final String BACKUP_PLAIN_TEXT = "PlainText";
    public static final Map<String, String> FORMAT_EXTENSION;
    static {
        FORMAT_EXTENSION = new HashMap<>();
        FORMAT_EXTENSION.put(BACKUP_HTML_DARK, HTML_EXTENSION);
        FORMAT_EXTENSION.put(BACKUP_PLAIN_TEXT, TXT_EXTENSION);
    }

    public final static String PLAY_EMOJI = "\u25B6";               // ▶
    public final static String PAUSE_EMOJI = "\u23F8";              // ⏸
    public final static String STOP_EMOJI = "\u23F9";               // ⏹
    public final static String REPEAT_EMOJI = "\uD83D\uDD01";       // 🔁
    public final static String PROGRESS_EMOJI = "\uD83D\uDD18";     // 🔘
    public final static String HEADPHONES_EMOJI = "\uD83C\uDFA7";   // 🎧
    public final static String LOAD_EMOJI = "\uD83D\uDCE5";         // 📥
    public final static String CANCEL_EMOJI = "\uD83D\uDEAB";       // 🚫
    public final static String VOLUME_OFF_EMOJI = "\uD83D\uDD07";   // 🔇
    public final static String VOLUME_30_EMOJI = "\uD83D\uDD08";    // 🔈
    public final static String VOLUME_70_EMOJI = "\uD83D\uDD09";    // 🔉
    public final static String VOLUME_100_EMOJI = "\uD83D\uDD0A";   // 🔊

}
