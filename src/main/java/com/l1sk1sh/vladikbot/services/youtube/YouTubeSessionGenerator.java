package com.l1sk1sh.vladikbot.services.youtube;

import com.l1sk1sh.vladikbot.network.dto.JenkinsJob;
import com.l1sk1sh.vladikbot.services.jenkins.JenkinsCommandsService;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import dev.lavalink.youtube.clients.Web;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class YouTubeSessionGenerator {

    private static final String SESSION_JOB_NAME = "yt-session-generator";
    private final BotSettingsManager settings;
    private final JenkinsCommandsService jenkins;
    private String errorMessage;

    @Autowired
    public YouTubeSessionGenerator(BotSettingsManager settings, JenkinsCommandsService jenkins) {
        this.settings = settings;
        this.jenkins = jenkins;
    }

    public void fetchAndSetYtSession() {
        errorMessage = null;

        JenkinsJob job = jenkins.getJenkinsJobStatus(SESSION_JOB_NAME);
        if (job == null) {
            log.warn("Failed to get response from Jenkins.");
            errorMessage = "Failed to get response from Jenkins.";

            return;
        }

        String consoleLog = jenkins.buildJobAndWaitForConsole(SESSION_JOB_NAME);

        if (consoleLog == null) {
            errorMessage = jenkins.getErrorMessage();
            if (errorMessage == null) {
                errorMessage = "Console response is empty";
            }

            log.error(errorMessage);
            return;
        }

        String visitorData = null;
        Pattern visitorDataPattern = Pattern.compile("(?<=visitor_data:\\s)\\S+");
        Matcher matcherData = visitorDataPattern.matcher(consoleLog);
        if (matcherData.find()) {
            visitorData = matcherData.group();
            settings.get().setYtVisitorData(visitorData);
            log.info("'visitor_data' updated");
        } else {
            log.warn("'visitor_data' is missing in console log");
        }

        String poToken = null;
        Pattern poTokenPattern = Pattern.compile("(?<=po_token:\\s)\\S+");
        Matcher matcherToken = poTokenPattern.matcher(consoleLog);
        if (matcherToken.find()) {
            poToken = matcherToken.group();
            settings.get().setYtPoToken(poToken);
            log.info("'po_token' updated");
        } else {
            log.warn("'po_token' is missing in console log");
        }

        if (poToken != null && visitorData != null) {
            Web.setPoTokenAndVisitorData(poToken, visitorData);
        }
    }
}
