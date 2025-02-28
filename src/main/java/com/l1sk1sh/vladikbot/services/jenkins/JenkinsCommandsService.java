package com.l1sk1sh.vladikbot.services.jenkins;

import com.l1sk1sh.vladikbot.network.dto.JenkinsJob;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.AuthUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class JenkinsCommandsService {
    private final BotSettingsManager settings;

    private final RestTemplate restTemplate;
    private HttpHeaders headers;

    private String jenkinsHost;

    @Getter
    private String errorMessage;

    JenkinsCommandsService(BotSettingsManager settings) {
        this.settings = settings;
        this.restTemplate = new RestTemplate();
    }

    public void init() {
        this.headers = AuthUtils.createBasicAuthenticationHeaders(
                settings.get().getJenkinsApiUsername(),
                settings.get().getJenkinsApiPassword());
        this.jenkinsHost = settings.get().getJenkinsApiHost();
    }

    public JenkinsJob getJenkinsJobStatus(String jobName) throws RestClientException {
        errorMessage = null;

        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<JenkinsJob> jenkinsJob = restTemplate.exchange(
                jenkinsHost + "/job/" + jobName + "/api/json?depth=1",
                HttpMethod.GET,
                new HttpEntity<JenkinsJob>(headers),
                JenkinsJob.class);

        return handleJobStatus(jenkinsJob);
    }

    public boolean startAndCheckStatusOfJenkinsJob(String jobName) {
        errorMessage = null;

        JenkinsJob job = getJenkinsJobStatus(jobName);
        if (job == null) {
            errorMessage = "Failed to get response from Jenkins.";

            return false;
        }

        if (job.getLastBuild().isBuilding() || job.getQueueItem() != null) {
            errorMessage = "Server is already running.";

            return true;
        }

        return buildJenkinsJob(jobName);
    }

    public boolean stopAndCheckStatusOfJenkinsJob(String jobName) {
        errorMessage = null;

        JenkinsJob job = getJenkinsJobStatus(jobName);
        if (job == null) {
            errorMessage = "Failed to get response from Jenkins.";

            return false;
        }

        if (!job.getLastBuild().isBuilding()) {
            errorMessage = "Server is already stopped.";

            return true;
        }

        JenkinsJob.Build latestBuild = job.getLastBuild();
        // noinspection ConstantValue
        if (latestBuild == null) {
            errorMessage = "Server has never been launched before.";

            return true;
        }

        return stopJenkinsJob(jobName, latestBuild.getId());
    }

    public String buildJobAndWaitForConsole(String jobName) throws RestClientException {
        errorMessage = null;

        if (!buildJenkinsJob(jobName)) {
            return null;
        }

        return waitAndReadConsoleForJob(jobName);
    }

    public String buildJobWithParametersAndWaitForConsole(String jobName, MultiValueMap<String, String> parameters) throws RestClientException {
        errorMessage = null;

        if (!buildWithParametersJenkinsJob(jobName, parameters)) {
            return null;
        }

        return waitAndReadConsoleForJob(jobName);
    }

    private String waitAndReadConsoleForJob(String jobName) {
        try {
            // There is no workaround for that - Jenkins can't keep up with next console request and serves previous build instead
            Thread.sleep(Duration.ofSeconds(15).toMillis());
        } catch (InterruptedException e) {
            errorMessage = "Sleep has failed.";

            return null;
        }

        String consoleLog = readJobConsole(jobName);

        if (consoleLog == null) {
            errorMessage = "Console wasn't fetched correctly.";

            return null;
        }

        return consoleLog;
    }

    private boolean buildJenkinsJob(String jobName) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Void> response = restTemplate.exchange
                (jenkinsHost + "/job/" + jobName + "/build", HttpMethod.POST, new HttpEntity<Void>(headers), Void.class);

        boolean started = response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.FOUND;
        if (!started) {
            log.error("Job hasn't been started correctly: {}", response.getStatusCode());
            errorMessage = String.format("Job hasn't been started correctly: %1$s", response.getStatusCode());
        }

        return started;
    }

    private boolean stopJenkinsJob(String jobName, String latestBuildId) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Void> response = restTemplate.exchange
                (jenkinsHost + "/job/" + jobName + "/" + latestBuildId + "/stop", HttpMethod.POST, new HttpEntity<Void>(headers), Void.class);

        boolean started = response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.FOUND;
        if (!started) {
            log.error("Job hasn't been stopped correctly: {}", response.getStatusCode());
            errorMessage = String.format("Job hasn't been stopped correctly: %1$s", response.getStatusCode());
        }

        return started;
    }

    private boolean buildWithParametersJenkinsJob(String jobName, MultiValueMap<String, String> parameters) throws RestClientException {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(parameters, headers);
        ResponseEntity<Void> response = restTemplate.exchange
                (jenkinsHost + "/job/" + jobName + "/buildWithParameters", HttpMethod.POST, request, Void.class);

        boolean started = response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.FOUND;
        if (!started) {
            log.error("Job with parameters hasn't been started correctly: {}", response.getStatusCode());
            errorMessage = String.format("Command hasn't been processed correctly: %1$s", response.getStatusCode());
        }

        return started;
    }

    private String readJobConsole(String jobName) throws RestClientException {
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.exchange
                (jenkinsHost + "/job/" + jobName + "/lastBuild/consoleText", HttpMethod.GET, new HttpEntity<Void>(headers), String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Console wasn't fetched correctly: {}", response.getStatusCode());

            return null;
        }

        return response.getBody();
    }

    private JenkinsJob handleJobStatus(ResponseEntity<JenkinsJob> jenkinsJob) throws RestClientException {

        if (jenkinsJob.getStatusCode() != HttpStatus.OK) {
            log.error("Failed to process Jenkins build request with status code {}", jenkinsJob.getStatusCode());

            return null;
        }

        JenkinsJob job = jenkinsJob.getBody();
        if (job == null) {
            log.error("Jenkins returned empty or incorrect response.");

            return null;
        }

        return job;
    }
}
