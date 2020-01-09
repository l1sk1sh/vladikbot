package com.l1sk1sh.vladikbot.utils;

import com.l1sk1sh.vladikbot.settings.Const;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public final class FormatUtils {
    private FormatUtils() {}
    
    public static String formatTimeTillHours(long duration) {
        final float durationDivider = 1000f;
        final int thresholdTime = 10;
        if (duration == Long.MAX_VALUE) {
            return "LIVE";
        }
        long seconds = Math.round(duration / durationDivider);
        long hours = seconds / (Const.SECONDS_IN_MINUTES * Const.SECONDS_IN_MINUTES);
        seconds %= Const.SECONDS_IN_MINUTES * Const.SECONDS_IN_MINUTES;
        long minutes = seconds / Const.SECONDS_IN_MINUTES;
        seconds %= Const.SECONDS_IN_MINUTES;
        return (hours > 0 ? hours + ":" : "") + (minutes < thresholdTime ? "0" + minutes : minutes)
                + ":" + (seconds < thresholdTime ? "0" + seconds : seconds);
    }

    public static String progressBar(double percent) {
        StringBuilder stringBuilder = new StringBuilder();
        final int progressBarPart = 12;

        for (int i = 0; i < progressBarPart; i++) {
            if (i == (int) (percent * progressBarPart)) {
                stringBuilder.append(Const.PROGRESS_EMOJI);
            } else {
                stringBuilder.append("▬");
            }
        }
        return stringBuilder.toString();
    }

    public static String volumeIcon(int volume) {
        final int volume30 = 30;
        final int volume70 = 70;

        if (volume == 0) {
            return Const.VOLUME_OFF_EMOJI;
        }
        if (volume < volume30) {
            return Const.VOLUME_30_EMOJI;
        }
        if (volume < volume70) {
            return Const.VOLUME_70_EMOJI;
        }
        return Const.VOLUME_100_EMOJI;
    }

    public static String listOfTextChannels(List<TextChannel> list, String query) {
        final int textChannelsListLimit = 6;
        StringBuilder out = new StringBuilder(" Multiple text channels found matching \"" + query + "\":");

        for (int i = 0; i < textChannelsListLimit && i < list.size(); i++) {
            out.append("\r\n - ").append(list.get(i).getName()).append(" (<#").append(list.get(i).getId()).append(">)");
        }
        if (list.size() > textChannelsListLimit) {
            out.append("\r\n**And ").append(list.size() - textChannelsListLimit).append(" more...**");
        }
        return out.toString();
    }

    public static String listOfVoiceChannels(List<VoiceChannel> list, String query) {
        final int voiceChannelsListLimit = 6;
        StringBuilder out = new StringBuilder(" Multiple voice channels found matching \"" + query + "\":");

        for (int i = 0; i < voiceChannelsListLimit && i < list.size(); i++) {
            out.append("\r\n - ").append(list.get(i).getName()).append(" (ID:").append(list.get(i).getId()).append(")");
        }
        if (list.size() > voiceChannelsListLimit) {
            out.append("\r\n**And ").append(list.size() - voiceChannelsListLimit).append(" more...**");
        }
        return out.toString();
    }

    public static String listOfRoles(List<Role> list, String query) {
        final int rolesListLimit = 6;
        StringBuilder out = new StringBuilder(" Multiple text channels found matching \"" + query + "\":");

        for (int i = 0; i < rolesListLimit && i < list.size(); i++) {
            out.append("\r\n - ").append(list.get(i).getName()).append(" (ID:").append(list.get(i).getId()).append(")");
        }
        if (list.size() > rolesListLimit) {
            out.append("\r\n**And ").append(list.size() - rolesListLimit).append(" more...**");
        }
        return out.toString();
    }

    public static String filter(String input) {
        return input.replace("@everyone", "@\u0435veryone")
                .replace("@here", "@h\u0435re").trim(); /* cyrillic letter e */
    }

    public static String getDateFromDatetime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        return formatter.format(date);
    }
}
