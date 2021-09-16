package com.l1sk1sh.vladikbot.commands.admin;

import com.l1sk1sh.vladikbot.network.dto.JenkinsJob;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.AuthUtils;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.UriBuilder;
import java.awt.*;

/**
 * @author l1sk1sh
 */
@Service
public class MinecraftServerCommand extends AdminV2Command {
    private static final Logger log = LoggerFactory.getLogger(MinecraftServerCommand.class);

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
        this.children = new AdminV2Command[]{
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
        this.minecraftJobUri = UriBuilder.fromUri(settings.get().getJenkinsApiHost() + "/job/minecraft-server-run/").toString();
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(this, children, name).toString()).setEphemeral(true).queue();
    }

    private class Status extends AdminV2Command {

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
                        .addBlankField(false)
                        .addField("Last time started", FormatUtils.getDateAndTimeFromTimestamp(latestBuild.getTimestamp()), false)
                        .addField("Time online", FormatUtils.getReadableDuration(latestBuild.getDuration()), false)
                        .addField((latestBuild.isBuilding()) ? "" : "Last server stop result", (latestBuild.isBuilding()) ? "" : latestBuild.getResult(), false)
                        .setImage("https://lh3.googleusercontent.com/proxy/rrJe5P0KT8F1TTrpx1XTq0IJ0sRKYx25ISU_RZZ_kN46euV4tdYw7wGj2I_8F9CuYXuS4CXQXPAvqYli7Y32fS1mQk05cY4Ut5rrtvNSnCcAV6gV-X5Dx8mEIScgfSYzqKGiexpU_9jKAw");

                event.reply(builder.setEmbeds(embedBuilder.build()).build()).queue();
            } catch (RestClientException e) {
                event.replyFormat("%1$s Error occurred: `%2$s`", getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
                log.error("Failed to process Jenkins status request.", e);
            }
        }
    }

    private class Start extends AdminV2Command {

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

                event.reply("Minecraft server has been launched!").queue();
            } catch (RestClientException e) {
                event.replyFormat("%1$s Error occurred: `%2$s`", getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
                log.error("Failed to process Jenkins build request.", e);
            }
        }
    }

    private class Stop extends AdminV2Command {

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

                event.reply("Minecraft server has been stopped!").queue();
            } catch (RestClientException e) {
                event.replyFormat("%1$s Error occurred: `%2$s`", getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
                log.error("Failed to process Jenkins stop request.", e);
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
            event.replyFormat("%1$s Failed to get correct status code: `%2$s`", getClient().getError(), jenkinsJob.getStatusCodeValue()).setEphemeral(true).queue();
            log.error("Failed to process Jenkins build request with status code {}", jenkinsJob.getStatusCode());

            return null;
        }

        JenkinsJob job = jenkinsJob.getBody();
        if (job == null) {
            event.replyFormat("%1$s Failed to get response from Jenkins.", getClient().getError()).setEphemeral(true).queue();
            log.error("Jenkins returned empty or incorrect response.");

            return null;
        }

        return job;
    }
}
