package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.settings.Const;
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class DebugCommand extends OwnerCommand {
    private static final Logger log = LoggerFactory.getLogger(DebugCommand.class);
    private final static String[] PROPERTIES = {"java.version", "java.vm.name", "java.vm.specification.version",
            "java.runtime.name", "java.runtime.version", "java.specification.version", "os.arch", "os.name"};

    private final Bot bot;

    public DebugCommand(Bot bot) {
        this.bot = bot;
        this.name = "debug";
        this.help = "shows debug info";
        this.guildOnly = false;
    }

    @Override
    protected final void execute(CommandEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("System Properties:");
        for (String key : PROPERTIES) {
            sb.append("\n  ").append(key).append(" = ").append(System.getProperty(key));
        }

        sb.append("\n\nJMusicBot Information:")
                .append("\n  Owner = ").append(bot.getBotSettings().getOwnerId())
                .append("\n  Prefix = ").append(bot.getBotSettings().getPrefix())
                .append("\n  MaxSeconds = ").append(bot.getBotSettings().getMaxSeconds())
                .append("\n  NPImages = ").append(bot.getBotSettings().useNpImages())
                .append("\n  SongInStatus = ").append(bot.getBotSettings().shouldSongBeInStatus())
                .append("\n  LeaveChannel = ").append(bot.getBotSettings().shouldLeaveChannel());

        sb.append("\n\nDependency Information:")
                .append("\n  JDA Version = ").append(JDAInfo.VERSION)
                .append("\n  JDA-Utilities Version = ").append(JDAUtilitiesInfo.VERSION)
                .append("\n  Lavaplayer Version = ").append(PlayerLibrary.VERSION);

        long total = Runtime.getRuntime().totalMemory() / Const.BITS_IN_BYTE / Const.BITS_IN_BYTE;
        long used = total - (Runtime.getRuntime().freeMemory() / Const.BITS_IN_BYTE / Const.BITS_IN_BYTE);
        sb.append("\n\nRuntime Information:")
                .append("\n  Total Memory = ").append(total)
                .append("\n  Used Memory = ").append(used);

        sb.append("\n\nDiscord Information:")
                .append("\n  ID = ").append(event.getJDA().getSelfUser().getId())
                .append("\n  Guilds = ").append(event.getJDA().getGuildCache().size())
                .append("\n  Users = ").append(event.getJDA().getUserCache().size());

        if (event.isFromType(ChannelType.PRIVATE)
                || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_ATTACH_FILES)) {
            event.getChannel().sendFile(sb.toString().getBytes(), "debug_information.txt").queue();
        } else {
            event.reply("Debug Information: ```\n" + sb.toString() + "\n```");
        }

        log.info("Debug command was sent to {}:[{}]", event.getAuthor().getName(), event.getAuthor().getId());
    }
}
