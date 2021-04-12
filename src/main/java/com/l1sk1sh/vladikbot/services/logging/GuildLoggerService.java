package com.l1sk1sh.vladikbot.services.logging;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.DownloadUtils;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import com.l1sk1sh.vladikbot.utils.StringUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * @author Oliver Johnson
 */
public class GuildLoggerService {
    private static final Logger log = LoggerFactory.getLogger(GuildLoggerService.class);
    private final Logger glog = LoggerFactory.getLogger("GUILD_LOGGER");

    private static final String EVENTS_LOG = "events.log";
    private static final int REACTION_EDIT_DISTANCE = 4;

    private final Bot bot;

    public GuildLoggerService(Bot bot) {
        this.bot = bot;

        /* It might be good idea to create separate loggers for each guild, but who cares */
        System.setProperty("guild_log.name", GuildLoggerService.EVENTS_LOG);
        System.setProperty("guild_log.path", bot.getBotSettings().getLogsFolder());
        bot.resetLoggerContext();
    }

    public void onMessageDelete(GuildMessageDeleteEvent event) {
        MessageCache.CachedMessage oldMessage = bot.getMessageCache().pullMessage(event.getGuild(), event.getMessageIdLong());

        if (oldMessage == null) {
            return;
        }

        TextChannel mtc = oldMessage.getTextChannel(event.getGuild());
        PermissionOverride po = mtc.getPermissionOverride(event.getGuild().getSelfMember());
        if (po != null && po.getDenied().contains(Permission.MESSAGE_HISTORY)) {
            return;
        }

        String formattedMessage = FormatUtils.formatMessage(oldMessage);
        if (formattedMessage.isEmpty()) {
            return;
        }

        User author = oldMessage.getAuthor();
        String authorName = (author == null)
                ? FormatUtils.formatCachedMessageFullUser(oldMessage)
                : FormatUtils.formatFullUser(author);

        String notificationMessage = String.format("Message **removal**:\r\n'_%1$s_'\r\nauthored by %2$s removed from %3$s.",
                formattedMessage, authorName, mtc.getAsMention());

        glog.info(notificationMessage);
        bot.getNotificationService().sendEmbeddedWarning(event.getGuild(), notificationMessage);
    }

    public void onMessageUpdate(GuildMessageUpdateEvent event) {
        Message newMessage = event.getMessage();
        MessageCache.CachedMessage oldMessage = bot.getMessageCache().putMessage(newMessage);

        if (oldMessage == null) {
            return;
        }

        TextChannel mtc = oldMessage.getTextChannel(event.getGuild());
        PermissionOverride po = mtc.getPermissionOverride(mtc.getGuild().getSelfMember());
        if (po != null && po.getDenied().contains(Permission.MESSAGE_HISTORY)) {
            return;
        }

        if (newMessage.getContentRaw().equals(oldMessage.getContentRaw())) {
            return;
        }

        String formattedOldMessage = FormatUtils.formatMessage(oldMessage);
        String formattedNewMessage = FormatUtils.formatMessage(newMessage);

        if (formattedNewMessage.isEmpty() || formattedOldMessage.isEmpty()) {
            return;
        }

        // Ignoring edit if it has some minor changes
        String strippedOldMessage = formattedOldMessage.toLowerCase().trim().replaceAll(" ", "");
        String strippedNewMessage = formattedNewMessage.toLowerCase().trim().replaceAll(" ", "");
        if (StringUtils.editDistance(strippedOldMessage, strippedNewMessage) <= REACTION_EDIT_DISTANCE) {
            return;
        }

        String notificationMessage = String.format("Message **edit**: \r\n'_%1$s_'\r\nchanged to\r\n'_%2$s_'\r\nby %3$s in %4$s.",
                formattedOldMessage, formattedNewMessage, FormatUtils.formatFullUser(newMessage.getAuthor()), mtc.getAsMention());

        glog.info(notificationMessage);
        bot.getNotificationService().sendEmbeddedWarning(event.getGuild(), notificationMessage);
    }

    public void onAvatarUpdate(UserUpdateAvatarEvent event) {
        String pathToAvatars = bot.getBotSettings().getLogsFolder() + "avatars/" +
                event.getUser().getName() + "_" + event.getUser().getId() + "/";

        try {
            FileUtils.createFolderIfAbsent(pathToAvatars);
        } catch (IOException e) {
            log.error("Failed to create avatars folder [{}]: {}", pathToAvatars, e.getLocalizedMessage());
            return;
        }

        String avatarUrl = event.getUser().getAvatarUrl();
        try {
            URL url = new URL(Objects.requireNonNull(avatarUrl).replace("." + Const.FileType.gif.name(), "." + Const.FileType.png.name()));

            if (!DownloadUtils.downloadAndSaveToFolder(url, pathToAvatars)) {
                log.error("Failed to save avatar from url '{}'.", url.toString());
            }
        } catch (IOException e) {
            log.error("IO error on avatar update:", e);
        }

        glog.info("User {}:{} changed avatar.", event.getUser().getName(), event.getUser().getId());
    }
}
