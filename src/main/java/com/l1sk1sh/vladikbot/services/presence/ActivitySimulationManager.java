package com.l1sk1sh.vladikbot.services.presence;

import com.l1sk1sh.vladikbot.data.entity.Activity;
import com.l1sk1sh.vladikbot.data.repository.ActivityRepository;
import com.l1sk1sh.vladikbot.settings.Const;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Oliver Johnson
 */
@Service
public class ActivitySimulationManager {
    private static final Logger log = LoggerFactory.getLogger(ActivitySimulationManager.class);

    private final JDA jda;
    private final ScheduledExecutorService frontThreadPool;
    private final Random random;
    private final ActivityRepository activityRepository;

    @Autowired
    public ActivitySimulationManager(JDA jda, @Qualifier("frontThreadPool") ScheduledExecutorService frontThreadPool, ActivityRepository activityRepository) {
        this.jda = jda;
        this.frontThreadPool = frontThreadPool;
        this.random = new Random();
        this.activityRepository = activityRepository;
    }

    private ScheduledFuture<?> scheduledFuture;
    private List<Activity> simulationRules = new ArrayList<>();

    private Activity getRandomRule() {
        Activity randomRule = simulationRules.get(random.nextInt(simulationRules.size()));
        log.debug("Chosen ActivitySimulation rule '{}'.", randomRule);

        return (randomRule == null)
                ? new Activity("Company of Heroes 2", Const.StatusAction.playing)
                : randomRule;
    }

    public void writeRule(Activity rule) {
        log.debug("Writing new ActivitySimulation rule '{}'.", rule);

        activityRepository.save(rule);

        simulationRules.add(rule);
    }

    public void deleteRule(String activityName) throws IOException {
        Activity rule = activityRepository.getActivityByActivityName(activityName);

        if (rule == null) {
            throw new IOException("Rule was not found");
        }

        log.info("Trying to remove ActivitySimulation rule '{}'...", rule);
        activityRepository.delete(rule);
        simulationRules.remove(rule);
    }

    public List<Activity> getAllRules() {
        if (simulationRules.isEmpty()) {
            readRules();
        }

        return simulationRules;
    }

    private void readRules() {
        simulationRules = activityRepository.findAll();
    }

    public final void start() {
        log.info("Enabling ActivitySimulation of the bot...");

        readRules();

        Runnable rotation = () -> {
            Activity rule = getRandomRule();

            switch (rule.getStatusAction()) {
                case playing:
                    jda.getPresence().setActivity(net.dv8tion.jda.api.entities.Activity.playing(rule.getActivityName()));
                    break;
                case listening:
                    jda.getPresence().setActivity(net.dv8tion.jda.api.entities.Activity.listening(rule.getActivityName()));
                    break;
                case watching:
                    jda.getPresence().setActivity(net.dv8tion.jda.api.entities.Activity.watching(rule.getActivityName()));
                    break;
            }
        };

        scheduledFuture = frontThreadPool.scheduleWithFixedDelay(rotation, 30, Const.STATUSES_ROTATION_FREQUENCY_IN_SECONDS, TimeUnit.SECONDS);
    }

    public void stop() {
        log.info("Disabling ActivitySimulation of the bot...");
        scheduledFuture.cancel(false);
    }
}
