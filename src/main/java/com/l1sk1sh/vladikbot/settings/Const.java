package com.l1sk1sh.vladikbot.settings;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Oliver Johnson
 */
public final class Const {
    private Const() {}

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
        RECOMMENDED_PERMS.add(Permission.VOICE_CONNECT);
        RECOMMENDED_PERMS.add(Permission.VOICE_SPEAK);
        RECOMMENDED_PERMS.add(Permission.NICKNAME_CHANGE);
    }

    public final static List<GatewayIntent> REQUIRED_INTENTS = new ArrayList<>();
    static {
        REQUIRED_INTENTS.add(GatewayIntent.GUILD_MEMBERS);
        REQUIRED_INTENTS.add(GatewayIntent.GUILD_EMOJIS);
        REQUIRED_INTENTS.add(GatewayIntent.GUILD_VOICE_STATES);
        REQUIRED_INTENTS.add(GatewayIntent.GUILD_PRESENCES);
        REQUIRED_INTENTS.add(GatewayIntent.GUILD_MESSAGES);
        REQUIRED_INTENTS.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        REQUIRED_INTENTS.add(GatewayIntent.DIRECT_MESSAGES);
    }

    public static final int DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000;
    public static final int EIGHT_MEGABYTES_IN_BYTES = 8 * 1024 * 1024;
    public static final int STATUSES_ROTATION_FREQUENCY_IN_SECONDS = 30 * 60;
    public static final int NEWS_UPDATE_FREQUENCY_IN_SECONDS = 10 * 60;
    public static final int MEMES_UPDATE_FREQUENCY_IN_SECONDS = 60 * 60;
    public static final int BITS_IN_BYTE = 1024;
    public static final int SECONDS_IN_MINUTES = 60;
    public static final int ARTICLE_STORE_LIMIT = 20;
    public static final int MEME_STORE_LIMIT = 30;

    public static final String YT_SEARCH_PREFIX = "ytsearch:";
    public static final String SC_SEARCH_PREFIX = "scsearch:";

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
        mp4(true);

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
        streaming
    }

    public enum BackupFileType {
        CSV("Csv", FileType.csv),
        HTML_DARK("HtmlDark", FileType.html),
        PLAIN_TEXT("PlainText", FileType.txt),
        HTML_LIGHT("HtmlLight", FileType.html);

        private final String backupTypeName;
        private final FileType fileType;

        BackupFileType(String backupTypeName, FileType fileType) {
            this.backupTypeName = backupTypeName;
            this.fileType = fileType;
        }

        public String getBackupTypeName() {
            return backupTypeName;
        }

        public FileType getFileType() {
            return fileType;
        }
    }

    public enum MatchingStrategy {
        full,
        inline
    }

    public static final String[] NAME_INVALID_CHARS = new String[]{":", "*", "\"", "\\", "/", "|", "<", ">"};
}
