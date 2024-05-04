package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/**
 * @author l1sk1sh
 */
@Service
public class PermissionsCommand extends AdminCommand {

    public PermissionsCommand() {
        this.name = "permissions";
        this.help = "Shows available and missing bot permissions";
        this.guildOnly = true;
    }

    @Override
    protected final void execute(SlashCommandEvent event) {
        MessageCreateBuilder builder = new MessageCreateBuilder();

        EnumSet<Permission> apiPermissions = Objects.requireNonNull(event.getGuild()).getSelfMember().getPermissions();
        List<Permission> grantedPermissions =
                BotUtils.getGrantedAndRequiredPermissions(apiPermissions, Const.RECOMMENDED_PERMS);
        List<Permission> missingPermissions =
                BotUtils.getMissingPermissions(apiPermissions, Const.RECOMMENDED_PERMS);
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(Objects.requireNonNull(event.getGuild()).getName(), null, event.getGuild().getIconUrl())
                .setColor(new Color(244, 180, 0))
                .addField("Available permissions", (grantedPermissions == null) ?
                        "```diff\nNot a single permissions was given! This should not be displayed...\n```"
                        : "```css\n" + Arrays.toString(grantedPermissions.toArray()) + "\n```", false)
                .addField("Missing permissions", (missingPermissions == null) ?
                        "```css\nAll permissions are granted\n```"
                        : "```fix\n" + Arrays.toString(missingPermissions.toArray()) + "\n```", false);
        event.reply(builder.setEmbeds(embedBuilder.build()).build()).queue();
    }
}
