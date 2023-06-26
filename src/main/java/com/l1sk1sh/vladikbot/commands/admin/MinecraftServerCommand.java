package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.network.dto.JenkinsJob;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.AuthUtils;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.time.Duration;
import java.util.Collections;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class MinecraftServerCommand extends AdminCommand {

    private final BotSettingsManager settings;

    private final RestTemplate restTemplate;
    private HttpHeaders headers;
    private String minecraftJobUri;
    private String minecraftRconJobUri;

    @Autowired
    public MinecraftServerCommand(BotSettingsManager settings) {
        this.settings = settings;
        this.restTemplate = new RestTemplate();
        this.name = "mcserver";
        this.help = "Manage this guild's minecraft server";
        this.children = new AdminCommand[]{
                new Status(),
                new Start(),
                new Stop(),
                new RCON()
        };
    }

    public void init() {
        this.headers = AuthUtils.createBasicAuthenticationHeaders(
                settings.get().getJenkinsApiUsername(),
                settings.get().getJenkinsApiPassword());
        this.minecraftJobUri = settings.get().getJenkinsApiHost() + "/job/minecraft-server/";
        this.minecraftRconJobUri = settings.get().getJenkinsApiHost() + "/job/minecraft-server-command/";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(event, children, name).toString()).setEphemeral(true).queue();
    }

    private class Status extends AdminCommand {

        private Status() {
            this.name = "status";
            this.help = "Get current status of the minecraft server";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            try {
                JenkinsJob job = getServerJobStatus(event);
                if (job == null) {
                    return;
                }

                JenkinsJob.Build latestBuild = job.getLastBuild();
                if (latestBuild == null) {
                    event.replyFormat("%1$s Server has never been launched before.", event.getClient().getWarning()).setEphemeral(true).queue();

                    return;
                }

                long duration = latestBuild.getDuration();
                long startTime = latestBuild.getTimestamp();

                MessageCreateBuilder builder = new MessageCreateBuilder();
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor("Minecraft server status", null, "https://cdn.icon-icons.com/icons2/2699/PNG/512/minecraft_logo_icon_168974.png")
                        .setColor(new Color(114, 56, 45))
                        .addField("Server status is", (latestBuild.isBuilding()) ? event.getClient().getSuccess() + " **Online**" : event.getClient().getError() + " **Offline**", false)
                        .addField("Last time started", FormatUtils.getDateAndTimeFromTimestamp(startTime), false)
                        .setImage("https://lh3.googleusercontent.com/proxy/rrJe5P0KT8F1TTrpx1XTq0IJ0sRKYx25ISU_RZZ_kN46euV4tdYw7wGj2I_8F9CuYXuS4CXQXPAvqYli7Y32fS1mQk05cY4Ut5rrtvNSnCcAV6gV-X5Dx8mEIScgfSYzqKGiexpU_9jKAw");

                if (duration > 0) {
                    embedBuilder.addField("Time online", FormatUtils.getReadableMMSSDuration(duration), false);
                } else {
                    embedBuilder.addField("Time online", FormatUtils.getReadableMMSSDuration(System.currentTimeMillis() - startTime), false);
                }

                if (!latestBuild.isBuilding()) {
                    embedBuilder.addField("Last server stop result", latestBuild.getResult(), false);
                }

                event.reply(builder.setEmbeds(embedBuilder.build()).build()).setEphemeral(true).queue();
            } catch (RestClientException e) {
                event.replyFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
                log.error("Failed to process Jenkins status request.", e);
            }
        }
    }

    private class Start extends AdminCommand {

        private Start() {
            this.name = "start";
            this.help = "Start minecraft server";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            try {
                JenkinsJob job = getServerJobStatus(event);
                if (job == null) {
                    return;
                }

                if (job.getLastBuild().isBuilding() || job.getQueueItem() != null) {
                    event.replyFormat("%1$s Server is already running.", event.getClient().getWarning()).setEphemeral(true).queue();

                    return;
                }

                headers.setContentType(MediaType.APPLICATION_JSON);
                ResponseEntity<Void> response = restTemplate.exchange
                        (minecraftJobUri + "build", HttpMethod.POST, new HttpEntity<Void>(headers), Void.class);

                if (response.getStatusCode() != HttpStatus.CREATED && response.getStatusCode() != HttpStatus.FOUND) {
                    event.replyFormat("%1$s Server hasn't been started correctly: `%2$s`", event.getClient().getWarning(), response.getStatusCode()).setEphemeral(true).queue();

                    return;
                }

                log.info("Minecraft server has been started by {}", FormatUtils.formatAuthor(event));
                event.reply("Minecraft server has been launched!").queue();
            } catch (RestClientException e) {
                log.error("Failed to process Jenkins build request.", e);
                event.replyFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
            }
        }
    }

    private class Stop extends AdminCommand {

        private Stop() {
            this.name = "stop";
            this.help = "Stop minecraft server";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            try {
                JenkinsJob job = getServerJobStatus(event);
                if (job == null) {
                    return;
                }

                if (!job.getLastBuild().isBuilding()) {
                    event.replyFormat("%1$s Server is already stopped.", event.getClient().getWarning()).setEphemeral(true).queue();

                    return;
                }

                JenkinsJob.Build latestBuild = job.getLastBuild();
                if (latestBuild == null) {
                    event.replyFormat("%1$s Server has never been launched before.", event.getClient().getWarning()).setEphemeral(true).queue();

                    return;
                }

                headers.setContentType(MediaType.APPLICATION_JSON);
                ResponseEntity<Void> response = restTemplate.exchange
                        (minecraftJobUri + "/" + latestBuild.getId() + "/stop", HttpMethod.POST, new HttpEntity<Void>(headers), Void.class);

                if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.FOUND) {
                    event.replyFormat("%1$s Server hasn't been stopped correctly: `%2$s`", event.getClient().getWarning(), response.getStatusCode()).setEphemeral(true).queue();

                    return;
                }

                log.info("Minecraft server has been stopped by {}", FormatUtils.formatAuthor(event));
                event.reply("Minecraft server has been stopped!").queue();
            } catch (RestClientException e) {
                log.error("Failed to process Jenkins stop request.", e);
                event.replyFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
            }
        }
    }

    private class RCON extends AdminCommand {

        private static final String COMMAND_OPTION_KEY = "command";

        private RCON() {
            this.name = "rcon";
            this.help = "Send RCON command to the server";
            this.ownerCommand = true; // Rewrite access level
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, COMMAND_OPTION_KEY, "RCON command to execute").setRequired(true));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            try {
                if (settings.get().getOwnerId() != event.getUser().getIdLong()) {
                    return;
                }

                OptionMapping commandOption = event.getOption(COMMAND_OPTION_KEY);
                if (commandOption == null) {
                    event.replyFormat("%1$s Command must be specified.", event.getClient().getWarning()).setEphemeral(true).queue();
                    return;
                }

                event.deferReply(true).queue();

                String rconCommand = commandOption.getAsString();
                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                map.add("COMMAND", rconCommand);

                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
                ResponseEntity<Void> buildResponse = restTemplate.exchange
                        (minecraftRconJobUri + "buildWithParameters", HttpMethod.POST, request, Void.class);

                if (buildResponse.getStatusCode() != HttpStatus.CREATED && buildResponse.getStatusCode() != HttpStatus.FOUND) {
                    event.getHook().editOriginalFormat("%1$s Command hasn't been processed correctly: `%2$s`", event.getClient().getWarning(), buildResponse.getStatusCode()).queue();

                    return;
                }

                try {
                    // There is no workaround for that - Jenkins can't keep up with next console request and serves previous build instead
                    Thread.sleep(Duration.ofSeconds(15).toMillis());
                } catch (InterruptedException e) {
                    event.getHook().editOriginalFormat("%1$s Sleep has failed.", event.getClient().getError()).queue();

                    return;
                }

                headers.setContentType(MediaType.APPLICATION_JSON);
                ResponseEntity<String> consoleResponse = restTemplate.exchange
                        (minecraftRconJobUri + "lastBuild/consoleText", HttpMethod.GET, new HttpEntity<Void>(headers), String.class);

                if (consoleResponse.getStatusCode() != HttpStatus.OK) {
                    event.getHook().editOriginalFormat("%1$s Console wasn't fetched correctly: `%2$s`", event.getClient().getWarning(), buildResponse.getStatusCode()).queue();

                    return;
                }

                event.getHook().editOriginalFormat("```%1$s```", consoleResponse.getBody()).queue();
            } catch (RestClientException e) {
                event.getHook().editOriginalFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).queue();
                log.error("Failed to process Jenkins status request.", e);
            }
        }
    }

    private JenkinsJob getServerJobStatus(SlashCommandEvent event) throws RestClientException {
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<JenkinsJob> jenkinsJob = restTemplate.exchange(
                minecraftJobUri + "api/json?depth=1",
                HttpMethod.GET,
                new HttpEntity<JenkinsJob>(headers),
                JenkinsJob.class);

        return handleJobStatus(jenkinsJob, event);
    }

    private JenkinsJob handleJobStatus(ResponseEntity<JenkinsJob> jenkinsJob, SlashCommandEvent event) throws RestClientException {

        if (jenkinsJob.getStatusCode() != HttpStatus.OK) {
            log.error("Failed to process Jenkins build request with status code {}", jenkinsJob.getStatusCode());
            event.replyFormat("%1$s Failed to get correct status code: `%2$s`", event.getClient().getError(), jenkinsJob.getStatusCodeValue()).setEphemeral(true).queue();

            return null;
        }

        JenkinsJob job = jenkinsJob.getBody();
        if (job == null) {
            log.error("Jenkins returned empty or incorrect response.");
            event.replyFormat("%1$s Failed to get response from Jenkins.", event.getClient().getError()).setEphemeral(true).queue();

            return null;
        }

        return job;
    }
}
