package com.multiheaded.disbot.utils;

import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * - Removal of update version methods
 * @author John Grosh
 */
public class OtherUtil {
    public static InputStream imageFromUrl(String url) {
        if (url == null)
            return null;
        try {
            URL u = new URL(url);
            URLConnection urlConnection = u.openConnection();
            urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.112 Safari/537.36");
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
        OnlineStatus st = OnlineStatus.fromKey(status);
        return st == null ? OnlineStatus.ONLINE : st;
    }
}
