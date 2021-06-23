package com.l1sk1sh.vladikbot.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.l1sk1sh.vladikbot.data.entity.Activity;
import com.l1sk1sh.vladikbot.data.entity.ReplyRule;
import com.l1sk1sh.vladikbot.services.presence.ActivitySimulationManager;
import com.l1sk1sh.vladikbot.services.presence.AutoReplyManager;
import com.l1sk1sh.vladikbot.settings.Const;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * @author l1sk1sh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MigrationUtils {
    private static final Logger log = LoggerFactory.getLogger(MigrationUtils.class);

    public static final String SIMULATIONS_FILE_NAME = "simulations.json";
    public static final String REPLIES_FILE_NAME = "replies.json";

    public static void migrateReplyRules(AutoReplyManager autoReplyManager, Gson gson) {
        File replyRulesFile = new File("./" + REPLIES_FILE_NAME);
        try {
            List<ReplyRuleJson> jsonRules = gson.fromJson(
                    Files.readString(replyRulesFile.toPath()), new TypeToken<List<ReplyRuleJson>>() {
                    }.getType());

            for (ReplyRuleJson rule : jsonRules) {
                log.trace("Writing rule {} to database.", rule);
                autoReplyManager.writeRule(rule.asEntity());
            }

            if (replyRulesFile.delete()) {
                log.info("Migration of {} completed. File deleted.", REPLIES_FILE_NAME);
            }

        } catch (IOException e) {
            log.error("Failed to read replies .json file.", e);
        }
    }

    public static void migrateActivities(ActivitySimulationManager activitySimulationManager, Gson gson) {
        File activitiesJson = new File("./" + SIMULATIONS_FILE_NAME);
        try {
            List<ActivityRuleJson> jsonActivities = gson.fromJson(
                    Files.readString(activitiesJson.toPath()), new TypeToken<List<ActivityRuleJson>>() {
                    }.getType());

            for (ActivityRuleJson activity : jsonActivities) {
                log.trace("Writing activity {} to database.", activity);
                activitySimulationManager.writeRule(activity.asEntity());
            }

            if (activitiesJson.delete()) {
                log.info("Migration of {} completed. File deleted.", SIMULATIONS_FILE_NAME);
            }

        } catch (IOException e) {
            log.error("Failed to read activities .json file.", e);
        }
    }

    @Getter
    @Setter
    @ToString
    private static class ReplyRuleJson {
        private List<String> reactToList;
        private List<String> reactWithList;

        public ReplyRule asEntity() {
            return new ReplyRule(reactToList, reactWithList);
        }
    }

    @Getter
    @Setter
    @ToString
    private static class ActivityRuleJson {
        private String gameName;
        private Const.StatusAction action;

        public Activity asEntity() {
            return new Activity(gameName, action);
        }
    }
}
