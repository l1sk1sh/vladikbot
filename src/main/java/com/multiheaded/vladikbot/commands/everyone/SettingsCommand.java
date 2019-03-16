package com.multiheaded.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.settings.Constants;
import com.multiheaded.vladikbot.settings.Settings;
import com.multiheaded.vladikbot.settings.SettingsManager;
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
    public SettingsCommand() {
        this.name = "settings";
        this.help = "shows the bots settings";
        this.aliases = new String[]{"status"};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        Settings settings = SettingsManager.getInstance().getSettings();
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
                        + "\nVoice Channel: "
                        + ((voiceChannel == null) ? "Any" : "**" + voiceChannel.getName() + "**")
                        + "\nDJ Role: "
                        + ((djRole == null) ? "None" : "**" + djRole.getName() + "**")
                        + "\nRepeat Mode: **"
                        + (settings.shouldRepeat() ? "On" : "Off") + "**"
                        + "\nDefault Playlist: "
                        + ((settings.getDefaultPlaylist() == null) ? "None" : "**" + settings.getDefaultPlaylist() + "**")
                )
                .setFooter(event.getJDA().getGuilds().size() + " servers | "
                        + event.getJDA().getGuilds().stream().filter(g -> g.getSelfMember().getVoiceState().inVoiceChannel()).count()
                        + " audio connections", null);
        event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }

}
