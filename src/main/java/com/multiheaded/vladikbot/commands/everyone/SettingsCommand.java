package com.multiheaded.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.settings.Constants;
import com.multiheaded.vladikbot.settings.Settings;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SettingsCommand extends Command {
    private final Settings settings;

    public SettingsCommand(Settings settings) {
        this.name = "settings";
        this.help = "shows the bots settings";
        this.aliases = new String[]{"status"};
        this.guildOnly = true;
        this.settings = settings;
    }

    @Override
    protected void execute(CommandEvent event) {
        MessageBuilder builder = new MessageBuilder()
                .append(Constants.HEADPHONES_EMOJI + " **")
                .append(event.getSelfUser().getName())
                .append("** settings:");
        TextChannel textChannel = settings.getTextChannel(event.getGuild());
        VoiceChannel voiceChannel = settings.getVoiceChannel(event.getGuild());
        Role djRole = settings.getDjRole(event.getGuild());

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(event.getSelfMember().getColor())
                .setDescription("Text Channel: " + ((textChannel == null) ? "Any" : "**#" + textChannel.getName() + "**")
                        + "\r\nVoice Channel: "
                        + ((voiceChannel == null) ? "Any" : "**" + voiceChannel.getName() + "**")
                        + "\r\nDJ Role: "
                        + ((djRole == null) ? "None" : "**" + djRole.getName() + "**")
                        + "\r\nRepeat Mode: **"
                        + (settings.shouldRepeat() ? "On" : "Off") + "**"
                        + "\r\nDefault Playlist: "
                        + ((settings.getDefaultPlaylist() == null) ? "None" : "**" + settings.getDefaultPlaylist() + "**")
                )
                .setFooter(event.getJDA().getGuilds().size() + " servers | "
                        + event.getJDA().getGuilds().stream().filter(g -> g.getSelfMember().getVoiceState().inVoiceChannel()).count()
                        + " audio connections", null);
        event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }

}
