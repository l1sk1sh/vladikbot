package com.multiheaded.disbot.utils;

import com.multiheaded.disbot.settings.Constants;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.List;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class FormatUtil {
    public static String formatTime(long duration) {
        if (duration == Long.MAX_VALUE) {
            return "LIVE";
        }
        long seconds = Math.round(duration / 1000.0);
        long hours = seconds / (60 * 60);
        seconds %= 60 * 60;
        long minutes = seconds / 60;
        seconds %= 60;
        return (hours > 0 ? hours + ":" : "") + (minutes < 10 ? "0" + minutes : minutes)
                + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }

    public static String progressBar(double percent) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 12; i++)
            if (i == (int) (percent * 12)) {
                stringBuilder.append(Constants.PROGRESS_EMOJI);
            } else {
                stringBuilder.append("▬");
            }
        return stringBuilder.toString();
    }

    public static String volumeIcon(int volume) {
        if (volume == 0) {
            return Constants.VOLUME_OFF_EMOJI;
        }
        if (volume < 30) {
            return Constants.VOLUME_30_EMOJI;
        }
        if (volume < 70) {
            return Constants.VOLUME_70_EMOJI;
        }
        return Constants.VOLUME_100_EMOJI;
    }

    public static String listOfTChannels(List<TextChannel> list, String query) {
        StringBuilder out = new StringBuilder(" Multiple text channels found matching \"" + query + "\":");
        for (int i = 0; i < 6 && i < list.size(); i++) {
            out.append("\n - ").append(list.get(i).getName()).append(" (<#").append(list.get(i).getId()).append(">)");
        }
        if (list.size() > 6) {
            out.append("\n**And ").append(list.size() - 6).append(" more...**");
        }
        return out.toString();
    }

    public static String listOfVChannels(List<VoiceChannel> list, String query) {
        StringBuilder out = new StringBuilder(" Multiple voice channels found matching \"" + query + "\":");
        for (int i = 0; i < 6 && i < list.size(); i++) {
            out.append("\n - ").append(list.get(i).getName()).append(" (ID:").append(list.get(i).getId()).append(")");
        }
        if (list.size() > 6) {
            out.append("\n**And ").append(list.size() - 6).append(" more...**");
        }
        return out.toString();
    }

    public static String listOfRoles(List<Role> list, String query) {
        StringBuilder out = new StringBuilder(" Multiple text channels found matching \"" + query + "\":");
        for (int i = 0; i < 6 && i < list.size(); i++) {
            out.append("\n - ").append(list.get(i).getName()).append(" (ID:").append(list.get(i).getId()).append(")");
        }
        if (list.size() > 6) {
            out.append("\n**And ").append(list.size() - 6).append(" more...**");
        }
        return out.toString();
    }

    public static String filter(String input) {
        return input.replace("@everyone", "@\u0435veryone")
                .replace("@here", "@h\u0435re").trim(); // cyrillic letter e
    }
}
