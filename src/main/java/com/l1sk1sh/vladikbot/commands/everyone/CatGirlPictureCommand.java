package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.l1sk1sh.vladikbot.network.dto.CatGirlPicture;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.util.Collections;

/**
 * @author not l1sk1sh
 *
 * More extended info on nekos.life API could be found here https://www.npmjs.com/package/nekos.life
 */
@Service
public class CatGirlPictureCommand extends SlashCommand {
    private static final Logger log = LoggerFactory.getLogger(CatGirlPictureCommand.class);

    private static final String TAG_OPTION_KEY = "tag";

    private final RestTemplate restTemplate;

    @Autowired
    public CatGirlPictureCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "catgirl";
        this.help = "Get a random catgirl picture";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, TAG_OPTION_KEY, "Picture tag ;3").setRequired(false));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        OptionMapping tagOption = event.getOption(TAG_OPTION_KEY);
        String tag = (tagOption != null) ? tagOption.getAsString() : "neko";

        CatGirlPicture catGirlPicture;
        try {
            catGirlPicture = restTemplate.getForObject("https://nekos.life/api/v2/img/" + tag, CatGirlPicture.class);
        } catch (RestClientException e) {
            event.replyFormat("%1$s Error occurred: `%2$s`", getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
            log.error("Failed to consume API.", e);

            return;
        }

        if (catGirlPicture == null) {
            event.replyFormat("%1$s Failed to retrieve catgirl's picture.", getClient().getError()).setEphemeral(true).queue();
            log.error("Failed to retrieve a catgirl picture.");

            return;
        }

        String catGirlPictureUrl = catGirlPicture.getUrl();
        String catGirlMessage = catGirlPicture.getMsg();

        if (catGirlPictureUrl == null || catGirlPictureUrl.isEmpty()
                || (catGirlMessage != null && catGirlMessage.equalsIgnoreCase("404"))) {
            event.replyFormat("%1$s No kitty for you!", "\uD83D\uDC31").setEphemeral(true).queue();

            return;
        }

        MessageBuilder builder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("Nyan!", null, null)
                .setColor(new Color(20, 120, 120))
                .setImage(catGirlPictureUrl);

        event.reply(builder.setEmbeds(embedBuilder.build()).build()).queue();
    }
}
