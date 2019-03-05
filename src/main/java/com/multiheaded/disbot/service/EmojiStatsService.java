package com.multiheaded.disbot.service;

import com.multiheaded.disbot.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.*;

public class EmojiStatsService {
    private static final Logger logger = LoggerFactory.getLogger(EmojiStatsService.class);

    private Map<String, Integer> emojiList = new HashMap<>();

    public EmojiStatsService(File exportedFile) {
        try {
            String input = FileUtils.readFile(exportedFile, StandardCharsets.UTF_8);

            // Custom :emoji: matcher
            Matcher customEmojiMatcher =
                    Pattern.compile(":(::|[^:\\n\\s])+:").matcher(input);
            while (customEmojiMatcher.find()) {
                addElementToList(customEmojiMatcher.group());
            }

            // Unicode \ud83c\udc00 matcher
            Matcher unicodeEmojiMathcer =
                    Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                    Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE).matcher(input);
            while (unicodeEmojiMathcer.find()) {
                addElementToList(unicodeEmojiMathcer.group());
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

    private void addElementToList(String element) {
        if (emojiList.get(element) != null) {
            emojiList.put(element, emojiList.get(element) + 1);
        } else {
            emojiList.put(element, 1);
        }
    }

    public Map<String, Integer> getEmojiList() {
        return emojiList;
    }
}
