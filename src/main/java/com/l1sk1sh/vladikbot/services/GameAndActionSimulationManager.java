package com.l1sk1sh.vladikbot.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.l1sk1sh.vladikbot.models.entities.GameAndAction;
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
public class GameAndActionSimulationManager {
    private static final Logger log = LoggerFactory.getLogger(GameAndActionSimulationManager.class);

    private final Bot bot;
    private final Gson gson;
    private final ScheduledExecutorService scheduler;
    private final String rotationFolder;
    private ScheduledFuture<?> scheduledFuture;

    public GameAndActionSimulationManager(Bot bot) {
        this.bot = bot;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.rotationFolder = bot.getBotSettings().getRotationFolder();
    }

    public List<GameAndAction> getAllGamesAndActions() throws IOException {
        List<GameAndAction> pairs;

        if (FileUtils.fileOrFolderIsAbsent(rotationFolder)) {
            FileUtils.createFolders(rotationFolder);

            return null;
        }

        File folder = new File(rotationFolder);

        if (folder.listFiles() == null) {
            return null;
        }

        pairs = new ArrayList<>();
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.getName().equals(Const.GAME_AND_ACTION_SIMULATION_JSON)) {
                pairs = gson.fromJson(new FileReader(rotationFolder + file.getName()), new TypeToken<List<GameAndAction>>(){}.getType());
            }
        }

        return pairs;
    }

    public GameAndAction getGameAndActionByGameName(String gameName) {
        try {
            List<GameAndAction> pairs = getAllGamesAndActions();

            for (GameAndAction pair : pairs) {
                if (pair.getGameName().equals(gameName)) {
                    return pair;
                }
            }

            return null;
        } catch (IOException e) {
            log.error("Failed to get action and game {}", e.getLocalizedMessage());

            return null;
        }
    }

    private GameAndAction getRandomGameAndAction() {
        try {
            List<GameAndAction> pairs = getAllGamesAndActions();

            Random rand = new Random();
            GameAndAction randomPair = pairs.get(rand.nextInt(pairs.size()));
            log.debug("Chosen pair {}", randomPair);

            return randomPair;
        } catch (IOException e) {
            log.error("Failed to get random action and game {}", e.getLocalizedMessage());

            return new GameAndAction("Company of Heroes 2", Const.StatusAction.playing);
        }
    }

    public void writeGameAndAction(GameAndAction pair) throws IOException {
        FileUtils.createFolderIfAbsent(rotationFolder);

        log.debug("Writing new pair {}", pair);
        List<GameAndAction> pairs = getAllGamesAndActions();

        pairs.add(pair);
        writeJson(pairs);
    }

    public void deleteGameAndAction(GameAndAction pair) throws IOException {
        List<GameAndAction> pairs = getAllGamesAndActions();
        log.info("Trying to remove rotation pair {}", pair);

        pairs.remove(pair);
        writeJson(pairs);
    }

    private void writeJson(List<GameAndAction> pairs) throws IOException {
        JsonWriter writer = new JsonWriter(new FileWriter(rotationFolder + Const.GAME_AND_ACTION_SIMULATION_JSON));
        writer.setIndent("  ");
        writer.setHtmlSafe(false);
        gson.toJson(pairs, pairs.getClass(), writer);
        writer.close();
    }

    public final void enableSimulation() {
        log.info("Changing Game And Action of the bot...");

        Runnable rotation = () -> {
            GameAndAction chosenPair = getRandomGameAndAction();

            switch (chosenPair.getAction()) {
                case playing:
                    bot.getJDA().getPresence().setGame(Game.playing(chosenPair.getGameName()));
                    break;
                case listening:
                    bot.getJDA().getPresence().setGame(Game.listening(chosenPair.getGameName()));
                    break;
                case watching:
                    bot.getJDA().getPresence().setGame(Game.watching(chosenPair.getGameName()));
                    break;
            }
        };

        scheduledFuture = scheduler.scheduleWithFixedDelay(
                rotation, 30, Const.STATUSES_ROTATION_FREQUENCY_IN_SECONDS, TimeUnit.SECONDS);
    }

    public void disableSimulation() {
        log.info("Cancelling rotation of actions-games");
        scheduledFuture.cancel(false);
        scheduler.shutdown();
    }
}
