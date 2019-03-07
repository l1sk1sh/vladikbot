package com.multiheaded.disbot.core.service;

import com.multiheaded.disbot.util.FileUtils;
import net.dv8tion.jda.core.entities.Emote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.*;

public class EmojiStatsService {
    private static final Logger logger = LoggerFactory.getLogger(EmojiStatsService.class);

    private Map<String, Integer> emojiList = new HashMap<>();

    private String[] args;
    private List<Emote> serverEmojiList;
    private boolean ignoreUnicodeEmoji = false;
    private boolean ignoreUnknownEmoji = false;

    public EmojiStatsService(File exportedFile, List<Emote> serverEmojiList, String[] args) {
        this.args = args;
        this.serverEmojiList = serverEmojiList;

        try {
            processArguments();
            String input = FileUtils.readFile(exportedFile, StandardCharsets.UTF_8);

            // Custom :emoji: matcher
            Matcher customEmojiMatcher = Pattern.compile(":(::|[^:\\n\\s/()])+:").matcher(input);
            while (customEmojiMatcher.find()) {
                if (ignoreUnknownEmoji) {
                    if (isEmojiInList(customEmojiMatcher.group()))
                        addElementToList(customEmojiMatcher.group());
                } else {
                    addElementToList(customEmojiMatcher.group());
                }
            }

            if (!ignoreUnicodeEmoji) {
                // Unicode \ud83c\udc00 matcher
                Matcher unicodeEmojiMathcer =
                        Pattern.compile("[\ud83c\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE).matcher(input);
                while (unicodeEmojiMathcer.find()) {
                    addElementToList(unicodeEmojiMathcer.group());
                }
            }

            // Sort Descending using Stream API
            emojiList = emojiList
                    .entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                            LinkedHashMap::new));
        } catch (IOException e) {
            logger.error("Failed to read exportedFile.", e.getMessage(), e.getCause());
        }
    }

    private void processArguments() {
        if (args.length > 0) {
            for (String arg : args) {
                switch (arg) {
                    case "-iu":
                        ignoreUnknownEmoji = true;
                    case "-i":
                        ignoreUnicodeEmoji = true;
                        break;
                }
            }
        }
    }

    private void addElementToList(String element) {
        // If not null - increase counter. If null - add element
        emojiList.merge(element, 1, (a, b) -> a + b);
    }

    private boolean isEmojiInList(String emoji) {
        boolean present = false;

        for (Emote serverEmoji : serverEmojiList) {
            if (serverEmoji.getName().equals(emoji.replaceAll(":", "")))
                present = true;
        }

        return present;
    }

    public Map<String, Integer> getEmojiList() {
        return emojiList;
    }
}
