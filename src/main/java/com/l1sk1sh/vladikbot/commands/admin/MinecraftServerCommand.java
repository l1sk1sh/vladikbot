package com.l1sk1sh.vladikbot.commands.admin;

import com.l1sk1sh.vladikbot.network.dto.JenkinsJob;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.AuthUtils;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;

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

    @Autowired
    public MinecraftServerCommand(BotSettingsManager settings) {
        this.settings = settings;
        this.restTemplate = new RestTemplate();
        this.name = "mcserver";
        this.help = "Manage this guild's minecraft server";
        this.children = new AdminCommand[]{
                new Status(),
                new Start(),
                new Stop()
        };
    }

    public void init() {
        this.headers = AuthUtils.createBasicAuthenticationHeaders(
                settings.get().getJenkinsApiUsername(),
                settings.get().getJenkinsApiPassword());
        this.headers.setContentType(MediaType.APPLICATION_JSON);
        this.minecraftJobUri = settings.get().getJenkinsApiHost() + "/job/minecraft-server-run/";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(this, children, name).toString()).setEphemeral(true).queue();
    }

    private class Status extends AdminCommand {

        private Status() {
            this.name = "status";
            this.help = "Get current status of the minecraft server";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            try {
                JenkinsJob job = getJobStatus(event);
                if (job == null) {
                    return;
                }

                JenkinsJob.Build latestBuild = job.getLastBuild();

                if (latestBuild == null) {
                    event.replyFormat("%1$s Server has never been launched before.", getClient().getWarning()).setEphemeral(true).queue();

                    return;
                }

                MessageBuilder builder = new MessageBuilder();
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor("Minecraft server status", null, "https://cdn.icon-icons.com/icons2/2699/PNG/512/minecraft_logo_icon_168974.png")
                        .setColor(new Color(114, 56, 45))
                        .addField("Server status is", (latestBuild.isBuilding()) ? getClient().getSuccess() + " **Online**" : getClient().getError() + " **Offline**", false)
                        .addField("Last time started", FormatUtils.getDateAndTimeFromTimestamp(latestBuild.getTimestamp()), false)
                        .addField("Time online", FormatUtils.getReadableDuration(latestBuild.getDuration()), false)
                        .setImage("https://lh3.googleusercontent.com/proxy/rrJe5P0KT8F1TTrpx1XTq0IJ0sRKYx25ISU_RZZ_kN46euV4tdYw7wGj2I_8F9CuYXuS4CXQXPAvqYli7Y32fS1mQk05cY4Ut5rrtvNSnCcAV6gV-X5Dx8mEIScgfSYzqKGiexpU_9jKAw");

                if (!latestBuild.isBuilding()) {
                    embedBuilder.addField("Last server stop result", latestBuild.getResult(), false);
                }

                event.reply(builder.setEmbeds(embedBuilder.build()).build()).setEphemeral(true).queue();
            } catch (RestClientException e) {
                event.replyFormat("%1$s Error occurred: `%2$s`", getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
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
                JenkinsJob job = getJobStatus(event);
                if (job == null) {
                    return;
                }

                if (job.getLastBuild().isBuilding() || job.getQueueItem() != null) {
                    event.replyFormat("%1$s Server is already running.", getClient().getWarning()).setEphemeral(true).queue();

                    return;
                }

                ResponseEntity<Void> response = restTemplate.exchange
                        (minecraftJobUri + "build", HttpMethod.POST, new HttpEntity<Void>(headers), Void.class);

                if (response.getStatusCode() != HttpStatus.CREATED) {
                    event.replyFormat("%1$s Server hasn't been started correctly: `%2$s`", getClient().getWarning(), response.getStatusCode()).setEphemeral(true).queue();

                    return;
                }

                log.info("Minecraft server has been started by {}", FormatUtils.formatAuthor(event));
                event.reply("Minecraft server has been launched!").queue();
            } catch (RestClientException e) {
                log.error("Failed to process Jenkins build request.", e);
                event.replyFormat("%1$s Error occurred: `%2$s`", getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
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
                JenkinsJob job = getJobStatus(event);
                if (job == null) {
                    return;
                }

                if (!job.getLastBuild().isBuilding()) {
                    event.replyFormat("%1$s Server is already stopped.", getClient().getWarning()).setEphemeral(true).queue();

                    return;
                }

                JenkinsJob.Build latestBuild = job.getLastBuild();

                if (latestBuild == null) {
                    event.replyFormat("%1$s Server has never been launched before.", getClient().getWarning()).setEphemeral(true).queue();

                    return;
                }

                ResponseEntity<Void> response = restTemplate.exchange
                        (minecraftJobUri + "/" + latestBuild.getId() + "/stop", HttpMethod.POST, new HttpEntity<Void>(headers), Void.class);

                if (response.getStatusCode() != HttpStatus.OK
                        || response.getStatusCode() != HttpStatus.FOUND) {
                    event.replyFormat("%1$s Server hasn't been stopped correctly: `%2$s`", getClient().getWarning(), response.getStatusCode()).setEphemeral(true).queue();

                    return;
                }

                log.info("Minecraft server has been stopped by {}", FormatUtils.formatAuthor(event));
                event.reply("Minecraft server has been stopped!").queue();
            } catch (RestClientException e) {
                log.error("Failed to process Jenkins stop request.", e);
                event.replyFormat("%1$s Error occurred: `%2$s`", getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
            }
        }
    }

    private JenkinsJob getJobStatus(SlashCommandEvent event) throws RestClientException {
        ResponseEntity<JenkinsJob> jenkinsJob = restTemplate.exchange(
                minecraftJobUri + "api/json?depth=1",
                HttpMethod.GET,
                new HttpEntity<JenkinsJob>(headers),
                JenkinsJob.class);

        if (jenkinsJob.getStatusCode() != HttpStatus.OK) {
            log.error("Failed to process Jenkins build request with status code {}", jenkinsJob.getStatusCode());
            event.replyFormat("%1$s Failed to get correct status code: `%2$s`", getClient().getError(), jenkinsJob.getStatusCodeValue()).setEphemeral(true).queue();

            return null;
        }

        JenkinsJob job = jenkinsJob.getBody();
        if (job == null) {
            log.error("Jenkins returned empty or incorrect response.");
            event.replyFormat("%1$s Failed to get response from Jenkins.", getClient().getError()).setEphemeral(true).queue();

            return null;
        }

        return job;
    }
}
