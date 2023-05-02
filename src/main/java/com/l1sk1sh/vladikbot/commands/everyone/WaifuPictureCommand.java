package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.network.dto.WaifuPicture;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author l1sk1sh
 * <p>
 * More extended info on API could be found here https://waifu.pics/docs
 */
@Slf4j
@Service
public class WaifuPictureCommand extends SlashCommand {

    private static final String TAG_1_OPTION_KEY = "tag_page_1";
    private static final String TAG_2_OPTION_KEY = "tag_page_2";

    private final RestTemplate restTemplate;

    @Autowired
    public WaifuPictureCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "waifu";
        this.help = "Get a random waifu picture";
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.STRING, TAG_1_OPTION_KEY, "Picture tag - first page").setRequired(false)
                .addChoice("waifu", "waifu,sfw")
                .addChoice("neko", "neko,sfw")
                .addChoice("shinobu", "shinobu,sfw")
                .addChoice("megumin", "megumin,sfw")
                .addChoice("bully", "bully,sfw")
                .addChoice("cuddle", "cuddle,sfw")
                .addChoice("cry", "cry,sfw")
                .addChoice("hug", "hug,sfw")
                .addChoice("awoo", "awoo,sfw")
                .addChoice("kiss", "kiss,sfw")
                .addChoice("lick", "lick,sfw")
                .addChoice("pat", "pat,sfw")
                .addChoice("smug", "smug,sfw")
                .addChoice("bonk", "bonk,sfw")
                .addChoice("yeet", "yeet,sfw")
        );
        options.add(new OptionData(OptionType.STRING, TAG_2_OPTION_KEY, "Picture tag - second page").setRequired(false)
                .addChoice("blush", "blush,sfw")
                .addChoice("smile", "smile,sfw")
                .addChoice("highfive", "highfive,sfw")
                .addChoice("handhold", "handhold,sfw")
                .addChoice("nom", "nom,sfw")
                .addChoice("bite", "bite,sfw")
                .addChoice("glomp", "glomp,sfw")
                .addChoice("slap", "slap,sfw")
                .addChoice("carrot", "blowjob,nsfw")
                .addChoice("kill", "kill,sfw")
                .addChoice("kick", "kick,sfw")
                .addChoice("waifu", "waifu,nsfw")
                .addChoice("neko", "neko,nsfw")
                .addChoice("happy", "happy,sfw")
                .addChoice("wink", "wink,sfw")
                .addChoice("poke", "poke,sfw")
                .addChoice("trap", "trap,nsfw")
                .addChoice("dance", "dance,sfw")
        );
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Sed tag from first page if available
        OptionMapping tagOneOption = event.getOption(TAG_1_OPTION_KEY);
        String tag = (tagOneOption != null) ? tagOneOption.getAsString() : "waifu,sfw";

        // Replace tag from first page if second page has been used
        OptionMapping tagTwoOption = event.getOption(TAG_2_OPTION_KEY);
        tag = (tagTwoOption != null) ? tagTwoOption.getAsString() : tag;

        String[] tagType = tag.split(",");

        WaifuPicture waifuPicture;
        try {
            waifuPicture = restTemplate.getForObject("https://api.waifu.pics/" + tagType[1] + "/" + tagType[0], WaifuPicture.class);
        } catch (RestClientException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("not found")) {
                event.replyFormat("%1$s No waifu for you!", "\uD83D\uDC31").setEphemeral(true).queue();

                return;
            }

            event.replyFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
            log.error("Failed to consume API.", e);

            return;
        }

        if (waifuPicture == null) {
            event.replyFormat("%1$s Failed to retrieve your waifu!", event.getClient().getError()).setEphemeral(true).queue();
            log.error("Failed to retrieve a waifu picture.");

            return;
        }

        String waifuPictureUrl = waifuPicture.getUrl();
        String waifuMessage = waifuPicture.getMessage();

        if (waifuPictureUrl == null || waifuPictureUrl.isEmpty()
                || (waifuMessage != null && waifuMessage.equalsIgnoreCase("Not Found"))) {
            event.replyFormat("%1$s No waifu for you!", "\uD83D\uDC31").setEphemeral(true).queue();

            return;
        }

        MessageCreateBuilder builder = new MessageCreateBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(new Color(20, 120, 120))
                .setImage(waifuPictureUrl);

        event.reply(builder.setEmbeds(embedBuilder.build()).build()).queue();
    }
}
