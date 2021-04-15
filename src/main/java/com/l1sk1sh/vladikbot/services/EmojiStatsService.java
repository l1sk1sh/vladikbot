package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.models.CsvParsedDiscordMessage;
import com.l1sk1sh.vladikbot.models.UsedEmoji;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author l1sk1sh
 */
public class EmojiStatsService implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(EmojiStatsService.class);

    private final JDA jda;
    private final BotSettingsManager settings;
    private final Map<String, Integer> emojiList;
    private final List<UsedEmoji> allUsedEmojis;
    private final List<Emote> serverEmojiList;
    private final File exportedTextFile;
    private final String localUsedEmojisName;
    private final String localUsedEmojisPath;
    private String failMessage = "Failed due to unknown reason";
    private final boolean ignoreUnicodeEmoji;
    private final boolean ignoreUnknownEmoji;
    private final boolean exportCsv;
    private boolean hasFailed = true;
    private File usedEmojisCsv;

    public EmojiStatsService(JDA jda, BotSettingsManager settings, File exportedTextFile, List<Emote> serverEmojiList, boolean ignoreUnicodeEmoji, boolean ignoreUnknownEmoji, boolean exportCsv) {
        this.jda = jda;
        this.settings = settings;
        this.exportedTextFile = exportedTextFile;
        this.localUsedEmojisName = exportedTextFile.getName().replace("." + Const.FileType.csv.name(), "") + " - used emoji";
        this.localUsedEmojisPath = this.settings.get().getLocalTmpFolder() + "used-emojis/";
        this.serverEmojiList = serverEmojiList;
        this.emojiList = new HashMap<>();
        this.allUsedEmojis = new ArrayList<>();
        this.ignoreUnicodeEmoji = ignoreUnicodeEmoji;
        this.ignoreUnknownEmoji = ignoreUnknownEmoji;
        this.exportCsv = exportCsv;
    }

    @Override
    public void run() {
        settings.get().setLockedBackup(true);

        try (Reader reader = new StringReader(
                FileUtils.readFile(exportedTextFile, StandardCharsets.UTF_8)
                        .replaceAll("[\"]", "")) /* Removing all quotes as DockerBackup cannot properly form CSV */
        ) {
            List<CsvParsedDiscordMessage> chatMessages = new ArrayList<>();

            @SuppressWarnings({"unchecked", "rawtypes"})
            CsvToBean<CsvParsedDiscordMessage> csv = new CsvToBeanBuilder(reader)
                    .withType(CsvParsedDiscordMessage.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            csv.setThrowExceptions(false);

            Iterator<CsvParsedDiscordMessage> csvIterator = csv.iterator();

            //noinspection WhileLoopReplaceableByForEach
            while (csvIterator.hasNext()) {
                try {
                    CsvParsedDiscordMessage message = csvIterator.next();
                    chatMessages.add(message);
                    log.debug("Processed line '{}'.", message);
                } catch (Exception e) { /* CsvMalformedLineException, CsvRequiredFieldEmptyException */
                    log.error("Failed to parse line with exception:", e);
                }
            }

            Matcher emojiMatcher;
            Pattern serverEmojiPattern = Pattern.compile(":[A-Za-z0-9]+:");
            Pattern unicodeEmojiPattern = Pattern.compile("[\ud83c\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                    Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

            /* Retrieving all emojis into separate data collection */
            for (CsvParsedDiscordMessage message : chatMessages) {

                /* Checking content of message for server specific emoji */
                String textOfMessage = message.getContent();
                emojiMatcher = serverEmojiPattern.matcher(textOfMessage);
                while (emojiMatcher.find()) {
                    UsedEmoji usedEmoji = new UsedEmoji(
                            message.getAuthorID(),
                            message.getAuthor(),
                            message.getDate(),
                            emojiMatcher.group(),
                            false,
                            false
                    );
                    allUsedEmojis.add(usedEmoji);
                }

                /* Checking unicode emoji */
                emojiMatcher = unicodeEmojiPattern.matcher(textOfMessage);
                while (emojiMatcher.find()) {
                    UsedEmoji usedEmoji = new UsedEmoji(
                            message.getAuthorID(),
                            message.getAuthor(),
                            message.getDate(),
                            emojiMatcher.group(),
                            false,
                            true
                    );
                    allUsedEmojis.add(usedEmoji);
                }
            }

            /* Gathering collection that will be returned to chat */
            for (UsedEmoji usedEmoji : allUsedEmojis) {
                User user = jda.getUserById(usedEmoji.getAuthorId());

                if (user != null && user.isBot()) {
                    continue;
                }
                
                if (usedEmoji.isUnicode() && ignoreUnicodeEmoji) {
                    continue;
                }
                
                if (!isEmojiServerInList(usedEmoji.getEmoji()) && ignoreUnknownEmoji) {
                    continue;
                }
                
                addElementToList(usedEmoji.getEmoji());
            }

            if (exportCsv) {
                log.info("Exporting emoji statistics to csv file...");
                exportToCsv();
            }

            if (emojiList.isEmpty()) {
                failMessage = "Emoji list is empty at the end of execution!";
            }

            hasFailed = false;

        } catch (IOException e) {
            log.error("Failed to read exportedFile:", e);
            failMessage = "Error with IO operation";
        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e){
            log.error("Failed to write used emojis csv:", e);
            failMessage = "Failed to write data CSV file.";
        } finally {
            settings.get().setLockedBackup(false);
        }
    }

    private void addElementToList(String element) {

        /* If not null - increase counter. If null - add element */
        emojiList.merge(element, 1, Integer::sum);
    }

    private void exportToCsv() throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        FileUtils.createFolderIfAbsent(localUsedEmojisPath);
        String pathToCsv = localUsedEmojisPath + localUsedEmojisName + "." + Const.FileType.csv.name();
        usedEmojisCsv = new File(pathToCsv);
        try (Writer writer = Files.newBufferedWriter(usedEmojisCsv.toPath())) {

            @SuppressWarnings({"unchecked", "rawtypes"})
            StatefulBeanToCsv<UsedEmoji> beanToCsv = new StatefulBeanToCsvBuilder(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();

            beanToCsv.write(allUsedEmojis);
        }
    }

    private boolean isEmojiServerInList(String emoji) {
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

    public File getUsedEmojisCsv() {
        return usedEmojisCsv;
    }
}
