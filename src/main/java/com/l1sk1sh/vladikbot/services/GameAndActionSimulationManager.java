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

    public static final String GAME_AND_ACTION_SIMULATION_RULES_JSON = "simulations.json";

    private final Bot bot;
    private final Gson gson;
    private final ScheduledExecutorService scheduler;
    private final String rulesFolder;
    private ScheduledFuture<?> scheduledFuture;
    private List<GameAndAction> simulationRules;

    public GameAndActionSimulationManager(Bot bot) {
        this.bot = bot;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.rulesFolder = bot.getBotSettings().getRulesFolder();
        this.simulationRules = new ArrayList<>();
    }

    private GameAndAction getRuleByGameName(String gameName) {
        for (GameAndAction rule : simulationRules) {
            if (rule.getGameName().equals(gameName)) {

                return rule;
            }
        }

        return null;
    }

    private GameAndAction getRandomRule() {
        GameAndAction randomRule = simulationRules.get(new Random().nextInt(simulationRules.size()));
        log.debug("Chosen GAASimulation rule '{}'.", randomRule);

        return (randomRule == null)
                ? new GameAndAction("Company of Heroes 2", Const.StatusAction.playing)
                : randomRule;
    }

    public void writeRule(GameAndAction rule) throws IOException {
        log.debug("Writing new GAASimulation rule '{}'.", rule);

        if (getRuleByGameName(rule.getGameName()) != null) {
            log.info("Rule '{}' already exists. Removing...", rule.getGameName());
            deleteRule(rule.getGameName());
        }

        simulationRules.add(rule);
        writeRules();
    }

    public void deleteRule(String gameName) throws IOException {
        GameAndAction rule = getRuleByGameName(gameName);

        if (rule == null) {
            throw new IOException("Rule was not found");
        }

        log.info("Trying to remove GAASimulation rule '{}'...", rule);
        simulationRules.remove(rule);
        writeRules();
    }

    public List<GameAndAction> getAllRules() throws IOException {
        if (simulationRules.isEmpty()) {
            readRules();
        }

        return simulationRules;
    }

    public void readRules() throws IOException {
        if (FileUtils.fileOrFolderIsAbsent(rulesFolder)) {
            FileUtils.createFolders(rulesFolder);

            return;
        }

        File folder = new File(rulesFolder);

        if (folder.listFiles() == null) {
            return;
        }

        File rulesFile = new File(rulesFolder + GAME_AND_ACTION_SIMULATION_RULES_JSON);

        if (!rulesFile.exists()) {
            return;
        }

        simulationRules = gson.fromJson(new FileReader(rulesFile), new TypeToken<List<GameAndAction>>(){}.getType());
    }

    private void writeRules() throws IOException {
        File rulesFile = new File(rulesFolder + GAME_AND_ACTION_SIMULATION_RULES_JSON);
        JsonWriter writer = new JsonWriter(new FileWriter(rulesFile));
        writer.setIndent("  ");
        writer.setHtmlSafe(false);
        gson.toJson(simulationRules, simulationRules.getClass(), writer);
        writer.close();
    }

    public final void enableSimulation() throws IOException {
        log.info("Enabling GAASimulation of the bot...");

        readRules();

        Runnable rotation = () -> {
            GameAndAction rule = getRandomRule();

            switch (rule.getAction()) {
                case playing:
                    bot.getJDA().getPresence().setGame(Game.playing(rule.getGameName()));
                    break;
                case listening:
                    bot.getJDA().getPresence().setGame(Game.listening(rule.getGameName()));
                    break;
                case watching:
                    bot.getJDA().getPresence().setGame(Game.watching(rule.getGameName()));
                    break;
            }
        };

        scheduledFuture = scheduler.scheduleWithFixedDelay(
                rotation, 30, Const.STATUSES_ROTATION_FREQUENCY_IN_SECONDS, TimeUnit.SECONDS);
    }

    public void disableSimulation() {
        log.info("Disabling GAASimulation of the bot...");
        scheduledFuture.cancel(false);
        scheduler.shutdown();
    }
}
