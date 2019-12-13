package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import net.dv8tion.jda.core.entities.Emote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Oliver Johnson
 */
// TODO Ignore emojis from Bot (take real name Владик)
// TODO Rewrite using CSV as it way easier
// TODO Research why paginator doesn't work for longer time (5 min or so)
// TODO By default emoji should calculate only present at the moment at the server emoji
// TODO FinderUtil from JDA
public class EmojiStatsService implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(EmojiStatsService.class);

    private final Bot bot;
    private final Map<String, Integer> emojiList;
    private final List<Emote> serverEmojiList;
    private final File exportedTextFile;
    private String failMessage;
    private boolean includeUnicodeEmoji;
    private boolean includeUnknownEmoji;
    private boolean hasFailed = false;

    public EmojiStatsService(Bot bot, File exportedTextFile, List<Emote> serverEmojiList, boolean includeUnicodeEmoji, boolean includeUnknownEmoji) {
        this.bot = bot;
        this.exportedTextFile = exportedTextFile;
        this.serverEmojiList = serverEmojiList;
        this.emojiList = new HashMap<>();
        this.includeUnicodeEmoji = includeUnicodeEmoji;
        this.includeUnknownEmoji = includeUnknownEmoji;
    }

    @Override
    public void run() {
        try {
            bot.setLockedBackup(true);
            String input = FileUtils.readFile(exportedTextFile, StandardCharsets.UTF_8);

            /* Custom :emoji: matcher */
            Matcher customEmojiMatcher = Pattern.compile(":(::|[^:\\r\n\\s/()])+:").matcher(input);
            while (customEmojiMatcher.find()) {
                if (includeUnknownEmoji && isEmojiInList(customEmojiMatcher.group())) {
                    addElementToList(customEmojiMatcher.group());
                } else {
                    addElementToList(customEmojiMatcher.group());
                }
            }

            if (includeUnicodeEmoji) {

                /* Unicode \ud83c\udc00 matcher */
                Matcher unicodeEmojiMathcer = Pattern.compile("[\ud83c\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                        Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE).matcher(input);
                while (unicodeEmojiMathcer.find()) {
                    addElementToList(unicodeEmojiMathcer.group());
                }
            }

            if (getEmojiList() == null) {
                failMessage = "Emoji list is empty at the end of execution!";
                hasFailed = true;
            }

        } catch (IOException e) {
            log.error("Failed to read exportedFile:", e);
        } finally {
            bot.setLockedBackup(false);
        }
    }

    private void addElementToList(String element) {

        /* If not null - increase counter. If null - add element */
        emojiList.merge(element, 1, Integer::sum);
    }

    private boolean isEmojiInList(String emoji) {
        boolean present = false;

        for (Emote serverEmoji : serverEmojiList) {
            if (serverEmoji.getName().equalsIgnoreCase(emoji.replaceAll(":", ""))) {
                present = true;
            }
        }

        return present;
    }

    public Map<String, Integer> getEmojiList() {
        return emojiList;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public boolean hasFailed() {
        return hasFailed;
    }
}
