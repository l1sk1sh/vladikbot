package com.l1sk1sh.vladikbot.utils;

import com.l1sk1sh.vladikbot.settings.Const;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Game;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
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
        if (url == null)
            return null;
        try {
            URL u = new URL(url);
            URLConnection urlConnection = u.openConnection();
            urlConnection.setRequestProperty("User-Agent", Const.USER_AGENT);
            return urlConnection.getInputStream();
        } catch (IOException | IllegalArgumentException ignore) {
        }
        return null;
    }

    public static Game parseGame(String game) {
        if (game == null || game.trim().isEmpty() || game.trim().equalsIgnoreCase("default")) {
            return null;
        }
        String lower = game.toLowerCase();
        if (lower.startsWith("playing")) {
            return Game.playing(game.substring(7).trim());
        }
        if (lower.startsWith("listening to")) {
            return Game.listening(game.substring(12).trim());
        }
        if (lower.startsWith("listening")) {
            return Game.listening(game.substring(9).trim());
        }
        if (lower.startsWith("watching")) {
            return Game.watching(game.substring(8).trim());
        }
        if (lower.startsWith("streaming")) {
            String[] parts = game.substring(9).trim().split("\\s+", 2);
            if (parts.length == 2) {
                return Game.streaming(parts[1], "https://twitch.tv/" + parts[0]);
            }
        }
        return Game.playing(game);
    }

    public static OnlineStatus parseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return OnlineStatus.ONLINE;
        }
        OnlineStatus onlineStatus = OnlineStatus.fromKey(status);
        return onlineStatus == null ? OnlineStatus.ONLINE : onlineStatus;
    }

    public static List<Permission> getMissingPermissions(List<Permission> available, List<Permission> required) {
        if (available.containsAll(required)) {
            return null;
        } else {
            return required.stream().filter(permission -> !available.contains(permission)).collect(Collectors.toList());
        }
    }

    public static List<Permission> getGrantedAndRequiredPermissions(List<Permission> available, List<Permission> required) {
        return available.stream().filter(required::contains).collect(Collectors.toList());
    }
}
