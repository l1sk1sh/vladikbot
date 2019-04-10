package com.multiheaded.vladikbot.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.settings.Constants;
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

import static com.multiheaded.vladikbot.utils.FileUtils.createFolder;
import static com.multiheaded.vladikbot.utils.FileUtils.fileOrFolderIsAbsent;

/**
 * @author Oliver Johnson
 */
public class ActionAndGameRotationManager {
    private static final Logger logger = LoggerFactory.getLogger(ActionAndGameRotationManager.class);

    private final VladikBot bot;
    private final Gson gson;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledFuture;

    public ActionAndGameRotationManager(VladikBot bot) {
        this.bot = bot;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        /* If rotation enabled - start rotation right away */
        if (bot.getSettings().shouldRotateActionsAndGames()) {
            activateRotation();
        }
    }

    public Map<String, String> getActionsAndGames() throws IOException {
        Map<String, String> pairs;

        if (fileOrFolderIsAbsent(bot.getSettings().getRotationFolder())) {
            createFolder(bot.getSettings().getRotationFolder());
            logger.info("Creating folder {}", bot.getSettings().getRotationFolder());
            return null;
        } else {
            File folder = new File(bot.getSettings().getRotationFolder());

            if (folder.listFiles() == null) {
                return null;
            }

            pairs = new HashMap<>();
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                if (file.getName().equals(Constants.STATUSES_JSON)) {

                    //noinspection unchecked
                    pairs = gson.fromJson(new FileReader(bot.getSettings().getRotationFolder()
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
            logger.error("Failed to get action and game {}", e.getLocalizedMessage());
            return null;
        }
    }

    private String[] getRandomStatusAndGame() {
        try {
            Map<String, String> pairs = getActionsAndGames();
            Object[] keySet = pairs.keySet().toArray();

            String chosenGame = (String) keySet[new Random().nextInt(keySet.length)];

            logger.debug("Chosen randomly: action - {}, game - {}", pairs.get(chosenGame), chosenGame);
            return new String[]{pairs.get(chosenGame), chosenGame};
        } catch (IOException e) {
            logger.error("Failed to get random action and game {}", e.getLocalizedMessage());
            return null;
        }
    }

    public void writeActionAndGame(String action, String gameName) throws IOException {
        if (fileOrFolderIsAbsent(bot.getSettings().getRotationFolder())) {
            createFolder(bot.getSettings().getRotationFolder());
            logger.info("Creating folder {}", bot.getSettings().getRotationFolder());
        }

        logger.debug("Writing new pair: action - {}, game - {}", action, gameName);
        Map<String, String> pairs = getActionsAndGames();

        pairs.put(gameName, action); /* Intentionally twisted! */

        JsonWriter writer = new JsonWriter(
                new FileWriter(bot.getSettings().getRotationFolder() + Constants.STATUSES_JSON));
        writer.setIndent("  ");
        writer.setHtmlSafe(false);
        gson.toJson(pairs, pairs.getClass(), writer);
        writer.close();
    }

    public void deleteActionAndGame(String action, String gameName) throws IOException {
        Map<String, String> pairs = getActionsAndGames();
        logger.info("Trying to remove action-game: action - {}, game - {}", action, gameName);

        pairs.remove(gameName, action); /* Intentionally twisted! */

        JsonWriter writer = new JsonWriter(
                new FileWriter(bot.getSettings().getRotationFolder() + Constants.STATUSES_JSON));
        writer.setIndent("  ");
        writer.setHtmlSafe(false);
        gson.toJson(pairs, pairs.getClass(), writer);
        writer.close();
    }

    public void activateRotation() {
        logger.debug("Rotating actions-games of the bot");
        try {
            Runnable rotation = () -> {
                String[] chosenPair = getRandomStatusAndGame(); /* [0] - chosen action; [1] - chosen game */
                logger.debug("Trying to set new action and game: {}", Arrays.toString(chosenPair));
                switch (Objects.requireNonNull(chosenPair)[0]) {
                    case Constants.ACTION_PLAYING:
                        bot.getJDA().getPresence().setGame(Game.playing(chosenPair[1]));
                        break;
                    case Constants.ACTION_LISTENING:
                        bot.getJDA().getPresence().setGame(Game.listening(chosenPair[1]));
                        break;
                    case Constants.ACTION_WATCHING:
                        bot.getJDA().getPresence().setGame(Game.watching(chosenPair[1]));
                        break;
                }
            };

            scheduledFuture = scheduler.scheduleWithFixedDelay(
                    rotation, 30, Constants.STATUSES_ROTATION_FREQUENCY_IN_SECONDS, TimeUnit.SECONDS);
        } catch (NullPointerException npe) {
            logger.error("Failed to obtain and set new action-game pair!");
        }
    }

    public void stopRotation() {
        logger.info("Cancelling rotation of actions-games");
        scheduledFuture.cancel(false);
        scheduler.shutdown();
    }
}
