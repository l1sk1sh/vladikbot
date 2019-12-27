package com.l1sk1sh.vladikbot.services;

import com.google.gson.reflect.TypeToken;
import com.l1sk1sh.vladikbot.models.entities.GameAndAction;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import net.dv8tion.jda.core.entities.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Oliver Johnson
 */
public class GameAndActionSimulationManager {
    private static final Logger log = LoggerFactory.getLogger(GameAndActionSimulationManager.class);

    private static final String GAME_AND_ACTION_SIMULATION_RULES_JSON = "simulations.json";

    private final Bot bot;
    private final String rulesFolder;
    private ScheduledFuture<?> scheduledFuture;
    private List<GameAndAction> simulationRules;

    public GameAndActionSimulationManager(Bot bot) {
        this.bot = bot;
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
        GameAndAction randomRule = simulationRules.get(Bot.rand.nextInt(simulationRules.size()));
        log.debug("Chosen GAASimulation rule '{}'.", randomRule);

        return (randomRule == null)
                ? new GameAndAction("Company of Heroes 2", Const.StatusAction.playing)
                : randomRule;
    }

    public void writeRule(GameAndAction rule) throws IOException {
        log.debug("Writing new GAASimulation rule '{}'.", rule);

        FileUtils.createFolderIfAbsent(rulesFolder);

        if (simulationRules.isEmpty()) {
            readRules();
        }

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

    private void readRules() throws IOException {
        File rulesFile = new File(rulesFolder + GAME_AND_ACTION_SIMULATION_RULES_JSON);

        if (!rulesFile.exists()) {
            FileUtils.createFolderIfAbsent(rulesFolder);

            return;
        }

        simulationRules = Bot.gson.fromJson(new FileReader(rulesFile), new TypeToken<List<GameAndAction>>(){}.getType());
    }

    private void writeRules() throws IOException {
        FileUtils.writeGson(simulationRules, new File(rulesFolder + GAME_AND_ACTION_SIMULATION_RULES_JSON));
    }

    public final void start() throws IOException {
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

        scheduledFuture = bot.getThreadPool().scheduleWithFixedDelay(rotation, 30, Const.STATUSES_ROTATION_FREQUENCY_IN_SECONDS, TimeUnit.SECONDS);
    }

    public void stop() {
        log.info("Disabling GAASimulation of the bot...");
        scheduledFuture.cancel(false);
    }
}
