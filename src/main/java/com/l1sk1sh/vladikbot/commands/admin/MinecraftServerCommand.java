package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.network.dto.JenkinsJob;
import com.l1sk1sh.vladikbot.services.jenkins.JenkinsCommandsService;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import java.awt.*;
import java.util.Collections;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class MinecraftServerCommand extends AdminCommand {

    private final BotSettingsManager settings;

    private final JenkinsCommandsService jenkins;
    private static final String SERVER_JOB_NAME = "minecraft-server";
    private static final String RCON_JOB_NAME = "minecraft-server-command";

    @Autowired
    public MinecraftServerCommand(BotSettingsManager settings, JenkinsCommandsService jenkins) {
        this.settings = settings;
        this.jenkins = jenkins;
        this.name = "mcserver";
        this.help = "Manage this guild's minecraft server";
        this.children = new AdminCommand[]{
                new Status(),
                new Start(),
                new Stop(),
                new RCON()
        };
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
                JenkinsJob job = jenkins.getJenkinsJobStatus(SERVER_JOB_NAME);
                if (job == null) {
                    event.replyFormat("%1$s Failed to get response from Jenkins.", event.getClient().getError()).setEphemeral(true).queue();

                    return;
                }

                JenkinsJob.Build latestBuild = job.getLastBuild();
                if (latestBuild == null) {
                    event.replyFormat("%1$s Server has never been launched before.", event.getClient().getWarning()).setEphemeral(true).queue();

                    return;
                }

                MessageCreateBuilder builder = new MessageCreateBuilder();
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor("Minecraft server status", null, "https://cdn.icon-icons.com/icons2/2699/PNG/512/minecraft_logo_icon_168974.png")
                        .setColor(new Color(114, 56, 45))
                        .addField("Server status is", (latestBuild.isBuilding()) ? event.getClient().getSuccess() + " **Online**" : event.getClient().getError() + " **Offline**", false)
                        .addField("Last time started", FormatUtils.getDateAndTimeFromTimestamp(latestBuild.getTimestamp()), false);

                if (latestBuild.getReadableDuration() != null) {
                    embedBuilder.addField("Time online", latestBuild.getReadableDuration(),
                            false);
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
                boolean otherServerRunning = jenkins.isAnyGameServerRunning();
                if (otherServerRunning) {
                    event.replyFormat("%1$s %2$s", event.getClient().getWarning(),
                            "Cannot start minecraft server as another server is running at the moment")
                            .setEphemeral(true).queue();

                    return;
                }

                boolean success = jenkins.startAndCheckStatusOfJenkinsJob(SERVER_JOB_NAME);
                String errorMessage = jenkins.getErrorMessage();
                if (!success) {
                    event.replyFormat("%1$s %2$s", event.getClient().getError(), errorMessage).setEphemeral(true).queue();

                    return;
                } else if (errorMessage != null) {
                    event.replyFormat("%1$s %2$s", event.getClient().getWarning()).setEphemeral(true).queue();

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
                boolean success = jenkins.stopAndCheckStatusOfJenkinsJob(SERVER_JOB_NAME);
                String errorMessage = jenkins.getErrorMessage();
                if (!success) {
                    event.replyFormat("%1$s %2$s", event.getClient().getError(), errorMessage).setEphemeral(true).queue();

                    return;
                } else if (errorMessage != null) {
                    event.replyFormat("%1$s %2$s", event.getClient().getWarning()).setEphemeral(true).queue();

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

                String consoleLog = jenkins.buildJobWithParametersAndWaitForConsole(RCON_JOB_NAME, map);

                if (consoleLog == null) {
                    event.getHook().editOriginalFormat("%1$s %2$s", event.getClient().getWarning(), jenkins.getErrorMessage()).queue();

                    return;
                }

                event.getHook().editOriginalFormat("```%1$s```", consoleLog).queue();
            } catch (RestClientException e) {
                event.getHook().editOriginalFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).queue();
                log.error("Failed to process Jenkins status request.", e);
            }
        }
    }
}
