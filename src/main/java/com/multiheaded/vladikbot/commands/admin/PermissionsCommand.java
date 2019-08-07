package com.multiheaded.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import static com.multiheaded.vladikbot.settings.Constants.RECOMMENDED_PERMS;
import static com.multiheaded.vladikbot.utils.OtherUtils.getMissingPermissions;
import static com.multiheaded.vladikbot.utils.OtherUtils.getGrantedAndRequiredPermissions;

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
    protected void execute(CommandEvent event) {
        MessageBuilder builder = new MessageBuilder();

        List<Permission> grantedPermissions =
                getGrantedAndRequiredPermissions(event.getSelfMember().getPermissions(), RECOMMENDED_PERMS);
        List<Permission> missingPermissions =
                getMissingPermissions(event.getSelfMember().getPermissions(), RECOMMENDED_PERMS);
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
