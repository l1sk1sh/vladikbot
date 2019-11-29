package com.l1sk1sh.vladikbot.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import net.dv8tion.jda.core.entities.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Oliver Johnson
 */
// TODO Rewrite using custom Class for Game + Status, not Map
public class ActionAndGameRotationManager {
    private static final Logger log = LoggerFactory.getLogger(ActionAndGameRotationManager.class);

    private final Bot bot;
    private final Gson gson;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledFuture;

    public ActionAndGameRotationManager(Bot bot) {
        this.bot = bot;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public Map<String, String> getActionsAndGames() throws IOException {
        Map<String, String> pairs;

        if (FileUtils.fileOrFolderIsAbsent(bot.getBotSettings().getRotationFolder())) {
            FileUtils.createFolders(bot.getBotSettings().getRotationFolder());
            log.info("Creating folder {}", bot.getBotSettings().getRotationFolder());
            return null;
        } else {
            File folder = new File(bot.getBotSettings().getRotationFolder());

            if (folder.listFiles() == null) {
                return null;
            }

            pairs = new HashMap<>();
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                if (file.getName().equals(Const.STATUSES_JSON)) {

                    //noinspection unchecked
                    pairs = gson.fromJson(new FileReader(bot.getBotSettings().getRotationFolder()
                            + file.getName()), Map.class);
                }
            }
            return pairs;
        }
    }

    public String getActionByGameTitle(String gameName) {
        try {
            Map<String, String> pairs = getActionsAndGames();
            return pairs.get(gameName);
        } catch (IOException e) {
            log.error("Failed to get action and game {}", e.getLocalizedMessage());
            return null;
        }
    }

    private Map.Entry<String, String> getRandomStatusAndGame() {
        try {
            Map<String, String> pairs = getActionsAndGames();
            Object[] keySet = pairs.keySet().toArray();

            String chosenGame = (String) keySet[new Random().nextInt(keySet.length)];

            Map.Entry<String, String> randomPair = new HashMap.SimpleEntry<>(chosenGame, pairs.get(chosenGame));
            log.debug("Chosen randomly: action - {}, game - {}", randomPair.getValue(), randomPair.getKey());

            return randomPair;
        } catch (IOException e) {
            log.error("Failed to get random action and game {}", e.getLocalizedMessage());
            return new HashMap.SimpleEntry<>("Company of Heroes 2", Const.ACTION_PLAYING);
        }
    }

    public void writeActionAndGame(String action, String gameName) throws IOException {
        if (FileUtils.fileOrFolderIsAbsent(bot.getBotSettings().getRotationFolder())) {
            FileUtils.createFolders(bot.getBotSettings().getRotationFolder());
            log.info("Creating folder {}", bot.getBotSettings().getRotationFolder());
        }

        log.debug("Writing new pair: action - {}, game - {}", action, gameName);
        Map<String, String> pairs = getActionsAndGames();

        pairs.put(gameName, action); /* Intentionally twisted! */
        writeJson(pairs);
    }

    public void deleteActionAndGame(String action, String gameName) throws IOException {
        Map<String, String> pairs = getActionsAndGames();
        log.info("Trying to remove action-game: action - {}, game - {}", action, gameName);

        pairs.remove(gameName, action); /* Intentionally twisted! */
        writeJson(pairs);
    }

    private void writeJson(Map<String, String> pairs) throws IOException {
        JsonWriter writer = new JsonWriter(
                new FileWriter(bot.getBotSettings().getRotationFolder() + Const.STATUSES_JSON));
        writer.setIndent("  ");
        writer.setHtmlSafe(false);
        gson.toJson(pairs, pairs.getClass(), writer);
        writer.close();
    }

    public final void activateRotation() {
        log.debug("Rotating actions-games of the bot");
        Runnable rotation = () -> {
            Map.Entry<String, String> chosenPair = getRandomStatusAndGame(); /* [0] - chosen action; [1] - chosen game */

            switch (chosenPair.getValue()) {
                case Const.ACTION_PLAYING:
                    bot.getJDA().getPresence().setGame(Game.playing(chosenPair.getKey()));
                    break;
                case Const.ACTION_LISTENING:
                    bot.getJDA().getPresence().setGame(Game.listening(chosenPair.getKey()));
                    break;
                case Const.ACTION_WATCHING:
                    bot.getJDA().getPresence().setGame(Game.watching(chosenPair.getKey()));
                    break;
            }
        };

        scheduledFuture = scheduler.scheduleWithFixedDelay(
                rotation, 30, Const.STATUSES_ROTATION_FREQUENCY_IN_SECONDS, TimeUnit.SECONDS);
    }

    public void stopRotation() {
        log.info("Cancelling rotation of actions-games");
        scheduledFuture.cancel(false);
        scheduler.shutdown();
    }
}
