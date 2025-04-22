package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.services.ShutdownHandler;
import com.l1sk1sh.vladikbot.services.jenkins.JenkinsCommandsService;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class RestartCommand extends OwnerCommand {

    private final JenkinsCommandsService jenkins;
    private final ShutdownHandler shutdownHandler;
    private static final String BOT_JOB_NAME = "vladikbot";


    @Autowired
    public RestartCommand(ShutdownHandler shutdownHandler, JenkinsCommandsService jenkins) {
        this.jenkins = jenkins;
        this.shutdownHandler = shutdownHandler;
        this.name = "restart";
        this.help = "Safely restart bot";
        this.guildOnly = false;
    }

    @Override
    protected final void execute(SlashCommandEvent event) {
        log.info("Bot is being restarted by {}.", FormatUtils.formatAuthor(event));
        event.deferReply(true).queue();
        event.getHook().editOriginal("Restarting...").queue();

        try {
            boolean success = jenkins.queueAndCheckStatusOfJenkinsJob(BOT_JOB_NAME);
            String errorMessage = jenkins.getErrorMessage();
            if (!success) {
                event.getHook().editOriginalFormat("%1$s %2$s", event.getClient().getError(), errorMessage).queue();

                return;
            } else if (errorMessage != null) {
                event.getHook().editOriginalFormat("%1$s %2$s", event.getClient().getWarning(), errorMessage).queue();

                return;
            }
        } catch (RestClientException e) {
            log.error("Failed to process Jenkins build request.", e);
            event.getHook().editOriginalFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).queue();
            return;
        }

        event.getHook().editOriginal("Bot scheduled. Shutting down...").queue();
        shutdownHandler.shutdown();
    }
}
