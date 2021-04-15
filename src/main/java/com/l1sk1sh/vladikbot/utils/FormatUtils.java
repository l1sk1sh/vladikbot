package com.l1sk1sh.vladikbot.utils;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.services.logging.MessageCache;
import com.l1sk1sh.vladikbot.settings.Const;
import net.dv8tion.jda.api.entities.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformatted code
 * @author John Grosh
 */
public final class FormatUtils {
    private FormatUtils() {
    }

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

    @SuppressWarnings("DuplicatedCode")
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

    @SuppressWarnings("DuplicatedCode")
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
        return input.replace("\u202E", "")
                .replace("@everyone", "@\u0435veryone") /* cyrillic letter e */
                .replace("@here", "@h\u0435re") /* cyrillic letter e */
                .trim();
    }

    public static String getDateFromDatetime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        return formatter.format(date);
    }

    public static String getDateAndTimeFromDatetime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

        return formatter.format(date);
    }

    public static String getNormalizedCurrentDate() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");

        return dateFormat.format(date);
    }

    public static String filterEveryone(String input) {
        return input.replace("\u202E", "") // RTL override
                .replace("@everyone", "@\u0435veryone") // cyrillic e
                .replace("@here", "@h\u0435re") // cyrillic e
                .replace("discord.gg/", "dis\u0441ord.gg/"); // cyrillic c
    }

    public static String formatMessage(Message message) {
        StringBuilder sb = new StringBuilder(message.getContentRaw());
        message.getAttachments().forEach(att -> sb.append("\n").append(att.getUrl()));
        return sb.length() > 2048 ? sb.toString().substring(0, 2040) : sb.toString();
    }

    public static String formatMessage(MessageCache.CachedMessage message) {
        StringBuilder sb = new StringBuilder(message.getContent());
        message.getAttachments().forEach(att -> sb.append("\n").append(att.getUrl()));
        return sb.length() > 2048 ? sb.toString().substring(0, 2040) : sb.toString();
    }

    public static String formatCachedMessageFullUser(MessageCache.CachedMessage msg) {
        return filterEveryone("**" + msg.getUsername() + "**#" + msg.getDiscriminator() + " (ID:" + msg.getAuthor().getId() + ")");
    }

    @SuppressWarnings("unused")
    public static String formatUser(User user) {
        return filterEveryone("**" + user.getName() + "**#" + user.getDiscriminator());
    }

    public static String formatFullUser(User user) {
        return filterEveryone("**" + user.getName() + "**#" + user.getDiscriminator() + " (ID:" + user.getId() + ")");
    }

    public static String formatAuthor(CommandEvent event) {
        return String.format("{%1$s}:[{%2$s}]", event.getAuthor().getName(), event.getAuthor().getId());
    }
}
