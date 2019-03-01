package com.multiheaded.disbot.settings;

public class Constants {
    //Non error, no action exit codes
    public static final int NORMAL_SHUTDOWN = 10;
    public static final int RESTART_EXITCODE = 11;
    public static final int NEWLY_CREATED_CONFIG = 12;

    //Non error, action required exit codes
    public static final int UPDATE_LATEST_EXITCODE = 20;
    public static final int UPDATE_RECOMMENDED_EXITCODE = 21;

    //Error exit codes
    public static final int UNABLE_TO_CONNECT_TO_DISCORD = 30;
    public static final int BAD_USERNAME_PASS_COMBO = 31;
    public static final int NO_USERNAME_PASS_COMBO = 32;

    public static String BOT_PREFIX = "!";

    static final String CONFIG_NAME = "settings.json";
}
