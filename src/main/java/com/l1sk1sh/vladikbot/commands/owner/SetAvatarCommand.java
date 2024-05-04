package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.utils.BotUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Slf4j
@Service
public class SetAvatarCommand extends OwnerCommand {

    private static final String URL_OPTION_KEY = "url";

    public SetAvatarCommand() {
        this.name = "setavatar";
        this.help = "sets the avatar of the bot";
        this.guildOnly = false;
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, URL_OPTION_KEY, "URL of a new avatar").setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        OptionMapping urlOption = event.getOption(URL_OPTION_KEY);
        if (urlOption == null) {
            event.replyFormat("%1$s URL is required for this command.", event.getClient().getWarning()).setEphemeral(true).queue();
            return;
        }

        String url = urlOption.getAsString();

        try (InputStream inputStream = BotUtils.imageFromUrl(url)) {
            if (inputStream == null) {
                event.replyFormat("%1$s Invalid or missing URL.", event.getClient().getError()).setEphemeral(true).queue();

                return;
            }

            try {
                log.info("Avatar was changed by {}", FormatUtils.formatAuthor(event));
                event.getJDA().getSelfUser().getManager().setAvatar(Icon.from(inputStream)).queue(
                        v -> event.reply(event.getClient().getSuccess() + " Successfully changed avatar.").setEphemeral(true).queue(),
                        t -> event.reply(event.getClient().getError() + " Failed to set avatar.").setEphemeral(true).queue()
                );
            } catch (IOException e) {
                event.replyFormat("%1$s Could not load from provided URL.", event.getClient().getError()).setEphemeral(true).queue();
            }

        } catch (IOException e) {
            log.error("Failed to download image from URL for avatar.", e);
            event.replyFormat("%1$s Unable to download image from specified URL.", event.getClient().getError()).setEphemeral(true).queue();
        }
    }
}
