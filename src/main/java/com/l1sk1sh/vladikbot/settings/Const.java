package com.l1sk1sh.vladikbot.settings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author l1sk1sh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Const {

    public static final List<Permission> RECOMMENDED_PERMS = new ArrayList<>();
    static {
        RECOMMENDED_PERMS.add(Permission.MESSAGE_SEND);
        RECOMMENDED_PERMS.add(Permission.MESSAGE_HISTORY);
        RECOMMENDED_PERMS.add(Permission.MESSAGE_ADD_REACTION);
        RECOMMENDED_PERMS.add(Permission.MESSAGE_EMBED_LINKS);
        RECOMMENDED_PERMS.add(Permission.MESSAGE_ATTACH_FILES);
        RECOMMENDED_PERMS.add(Permission.MESSAGE_MANAGE);
        RECOMMENDED_PERMS.add(Permission.MESSAGE_EXT_EMOJI);
        RECOMMENDED_PERMS.add(Permission.VOICE_CONNECT);
        RECOMMENDED_PERMS.add(Permission.VOICE_SPEAK);
        RECOMMENDED_PERMS.add(Permission.NICKNAME_CHANGE);
    }

    public static final List<GatewayIntent> REQUIRED_INTENTS = new ArrayList<>();
    static {
        REQUIRED_INTENTS.add(GatewayIntent.GUILD_MEMBERS);
        REQUIRED_INTENTS.add(GatewayIntent.GUILD_EMOJIS_AND_STICKERS);
        REQUIRED_INTENTS.add(GatewayIntent.GUILD_VOICE_STATES);
        REQUIRED_INTENTS.add(GatewayIntent.GUILD_PRESENCES);
        REQUIRED_INTENTS.add(GatewayIntent.GUILD_MESSAGES);
        REQUIRED_INTENTS.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        REQUIRED_INTENTS.add(GatewayIntent.DIRECT_MESSAGES);
        REQUIRED_INTENTS.add(GatewayIntent.MESSAGE_CONTENT);
    }

    public static final int DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000;
    public static final int EIGHT_MEGABYTES_IN_BYTES = 8 * 1024 * 1024;
    public static final int STATUSES_ROTATION_FREQUENCY_IN_SECONDS = 30 * 60;
    public static final int NEWS_UPDATE_FREQUENCY_IN_SECONDS = 10 * 60;
    public static final int MEMES_UPDATE_FREQUENCY_IN_SECONDS = 60 * 60;
    public static final int SECONDS_IN_MINUTES = 60;

    public static final String YT_SEARCH_PREFIX = "ytsearch:";
    public static final String SC_SEARCH_PREFIX = "scsearch:";

    public static final String PLAY_EMOJI = "\u25B6";               // ▶
    public static final String PAUSE_EMOJI = "\u23F8";              // ⏸
    public static final String STOP_EMOJI = "\u23F9";               // ⏹
    public static final String PROGRESS_EMOJI = "\uD83D\uDD18";     // 🔘
    public static final String HEADPHONES_EMOJI = "\uD83C\uDFA7";   // 🎧
    public static final String LOAD_EMOJI = "\uD83D\uDCE5";         // 📥
    public static final String CANCEL_EMOJI = "\uD83D\uDEAB";       // 🚫
    public static final String VOLUME_OFF_EMOJI = "\uD83D\uDD07";   // 🔇
    public static final String VOLUME_30_EMOJI = "\uD83D\uDD08";    // 🔈
    public static final String VOLUME_70_EMOJI = "\uD83D\uDD09";    // 🔉
    public static final String VOLUME_100_EMOJI = "\uD83D\uDD0A";   // 🔊
    public static final String LOADING_SYMBOL = "\u21BA";           // ↺

    public static final Color MEME_COLOR = new Color(255, 69, 0);

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";

    public enum FileType {
        json(false),
        html(false),
        txt(false),
        csv(false),
        zip(false),
        jpg(true),
        png(true),
        gif(true),
        mp4(true),
        mp3(true);

        private final boolean supportedMediaFormat;

        FileType(boolean supportedMediaFormat) {
            this.supportedMediaFormat = supportedMediaFormat;
        }

        static List<FileType> getRawSupportedMediaFormats() {
            return Arrays.stream(values()).filter(FileType::isSupportedMediaFormat).collect(Collectors.toList());
        }

        public static String[] getRawSupportedMediaFormatsAsArray() {
            return getRawSupportedMediaFormats().stream().map(Enum::name).toArray(String[]::new);
        }
        private boolean isSupportedMediaFormat() {
            return supportedMediaFormat;
        }
    }

    public enum StatusAction {
        playing,
        watching,
        listening,
        // streaming // TODO Add streaming activity
    }

    public static final String[] NAME_INVALID_CHARS = new String[]{":", "*", "\"", "\\", "/", "|", "<", ">"};
}
