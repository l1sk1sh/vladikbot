package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.BotUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

/**
 * @author Oliver Johnson
 */
public class PermissionsCommand extends AdminCommand {

    public PermissionsCommand() {
        this.name = "permissions";
        this.help = "shows available and missing bot permissions";
        this.guildOnly = true;
    }

    @Override
    protected final void execute(CommandEvent event) {
        MessageBuilder builder = new MessageBuilder();

        List<Permission> grantedPermissions =
                BotUtils.getGrantedAndRequiredPermissions(event.getSelfMember().getPermissions(), Const.RECOMMENDED_PERMS);
        List<Permission> missingPermissions =
                BotUtils.getMissingPermissions(event.getSelfMember().getPermissions(), Const.RECOMMENDED_PERMS);
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                .setColor(new Color(244, 160, 0))
                .addField("Available permissions", (grantedPermissions == null) ?
                        "```diff\nNot a single permissions was given! This should not be displayed...\n```"
                        : "```css\n" + Arrays.toString(grantedPermissions.toArray()) + "\n```", false)
                .addField("Missing permissions", (missingPermissions == null) ?
                        "```css\nAll permissions are granted\n```"
                        : "```fix\n" + Arrays.toString(missingPermissions.toArray()) + "\n```", false);
        event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }
}
