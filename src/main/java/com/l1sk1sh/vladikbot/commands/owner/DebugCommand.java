package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class DebugCommand extends OwnerCommand {
    private static final Logger log = LoggerFactory.getLogger(DebugCommand.class);
    private final static String[] PROPERTIES = {"java.version", "java.vm.name", "java.vm.specification.version",
            "java.runtime.name", "java.runtime.version", "java.specification.version", "os.arch", "os.name"};

    private final BotSettingsManager settings;

    @Autowired
    public DebugCommand(BotSettingsManager settings) {
        this.settings = settings;
        this.name = "debug";
        this.help = "shows debug info";
        this.guildOnly = false;
    }

    @Override
    protected final void execute(CommandEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("```\n");
        sb.append("System Properties:");
        for (String key : PROPERTIES) {
            sb.append("\n  ").append(key).append(" = ").append(System.getProperty(key));
        }

        sb.append("\n\nJMusicBot Information:")
                .append("\n  Owner = ").append(settings.get().getOwnerId())
                .append("\n  Prefix = ").append(settings.get().getPrefix())
                .append("\n  MaxSeconds = ").append(settings.get().getMaxSeconds())
                .append("\n  NPImages = ").append(settings.get().isNpImages())
                .append("\n  SongInStatus = ").append(settings.get().isSongInStatus())
                .append("\n  LeaveChannel = ").append(settings.get().isLeaveChannel());

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
        sb.append("\n```");

        if (event.isFromType(ChannelType.PRIVATE)
                || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_ATTACH_FILES)) {
            event.getChannel().sendFile(sb.toString().getBytes(), "debug_information.txt").queue();
        } else {
            event.reply("Debug Information: " + sb.toString() + "");
        }

        log.info("Debug command was sent to {}.", FormatUtils.formatAuthor(event));
    }
}
