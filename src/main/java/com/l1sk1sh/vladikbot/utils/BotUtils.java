package com.l1sk1sh.vladikbot.utils;

import com.l1sk1sh.vladikbot.settings.Const;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * - Removal of update version methods
 * @author John Grosh
 */
public final class BotUtils {
    private BotUtils() {}

    public static InputStream imageFromUrl(String url) {
        if (url == null) {
            return null;
        }

        try {
            URL u = new URL(url);
            URLConnection urlConnection = u.openConnection();
            urlConnection.setRequestProperty("User-Agent", Const.USER_AGENT);
            return urlConnection.getInputStream();
        } catch (IOException | IllegalArgumentException ignore) {
            /* Ignore */
        }

        return null;
    }

    public static Activity parseActivity(String activity) {
        if (activity == null || activity.trim().isEmpty() || activity.trim().equalsIgnoreCase("default")) {
            return null;
        }
        String lower = activity.toLowerCase();
        if (lower.startsWith("playing")) {
            return Activity.playing(makeNonEmpty(activity.substring(7).trim()));
        }
        if (lower.startsWith("listening to")) {
            return Activity.listening(makeNonEmpty(activity.substring(12).trim()));
        }
        if (lower.startsWith("listening")) {
            return Activity.listening(makeNonEmpty(activity.substring(9).trim()));
        }
        if (lower.startsWith("watching")) {
            return Activity.watching(makeNonEmpty(activity.substring(8).trim()));
        }
        if (lower.startsWith("streaming")) {
            String[] parts = activity.substring(9).trim().split("\\s+", 2);
            if (parts.length == 2) {
                return Activity.streaming(makeNonEmpty(parts[1]), "https://twitch.tv/" + parts[0]);
            }
        }
        return Activity.playing(activity);
    }

    private static String makeNonEmpty(String str) {
        return str == null || str.isEmpty() ? "\u200B" : str;
    }

    public static OnlineStatus parseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return OnlineStatus.ONLINE;
        }
        OnlineStatus onlineStatus = OnlineStatus.fromKey(status);
        return onlineStatus == null ? OnlineStatus.ONLINE : onlineStatus;
    }

    public static List<Permission> getMissingPermissions(EnumSet<Permission> available, List<Permission> required) {
        if (available.containsAll(required)) {
            return null;
        } else {
            return required.stream().filter(permission -> !available.contains(permission)).collect(Collectors.toList());
        }
    }

    public static List<Permission> getGrantedAndRequiredPermissions(EnumSet<Permission> available, List<Permission> required) {
        return available.stream().filter(required::contains).collect(Collectors.toList());
    }

    public static String getNormalizedGuildName(Guild guild) {
        return guild.getName().replaceAll("[^\\w]", "_");
    }
}
