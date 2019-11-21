package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.models.LockService;
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
public class EmojiStatsService {
    private static final Logger log = LoggerFactory.getLogger(EmojiStatsService.class);

    private final Map<String, Integer> emojiList = new HashMap<>();

    private final List<Emote> serverEmojiList;
    private boolean includeUnicodeEmoji = false;
    private boolean ignoreUnknownEmoji = false;

    public EmojiStatsService(File exportedFile, List<Emote> serverEmojiList, String[] args, LockService lock) {
        this.serverEmojiList = serverEmojiList;

        try {
            lock.setLocked(true);
            processArguments(args);
            String input = FileUtils.readFile(exportedFile, StandardCharsets.UTF_8);

            /* Custom :emoji: matcher */
            Matcher customEmojiMatcher = Pattern.compile(":(::|[^:\\r\n\\s/()])+:").matcher(input);
            while (customEmojiMatcher.find()) {
                if (ignoreUnknownEmoji) {
                    if (isEmojiInList(customEmojiMatcher.group()))
                        addElementToList(customEmojiMatcher.group());
                } else {
                    addElementToList(customEmojiMatcher.group());
                }
            }

            if (!includeUnicodeEmoji) {

                /* Unicode \ud83c\udc00 matcher */
                Matcher unicodeEmojiMathcer =
                        Pattern.compile("[\ud83c\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE).matcher(input);
                while (unicodeEmojiMathcer.find()) {
                    addElementToList(unicodeEmojiMathcer.group());
                }
            }

        } catch (IOException e) {
            log.error("Failed to read exportedFile. {}", e.getLocalizedMessage());
        } finally {
            lock.setLocked(false);
        }
    }

    private void processArguments(String[] args) {
        if (args.length > 0) {
            for (String arg : args) {
                switch (arg) {
                    case "-iu":
                        ignoreUnknownEmoji = true;
                        /* falls through */
                    case "-i":
                        includeUnicodeEmoji = true;
                        break;
                }
            }
        }
    }

    private void addElementToList(String element) {

        /* If not null - increase counter. If null - add element */
        emojiList.merge(element, 1, Integer::sum);
    }

    private boolean isEmojiInList(String emoji) {
        boolean present = false;

        for (Emote serverEmoji : serverEmojiList) {
            if (serverEmoji.getName().toLowerCase().equals(emoji.replaceAll(":", "")))
                present = true;
        }

        return present;
    }

    public Map<String, Integer> getEmojiList() {
        return emojiList;
    }
}
