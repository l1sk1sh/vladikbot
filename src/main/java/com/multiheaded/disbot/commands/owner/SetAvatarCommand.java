package com.multiheaded.disbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.disbot.utils.OtherUtil;
import net.dv8tion.jda.core.entities.Icon;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SetAvatarCommand extends OwnerCommand {
    public SetAvatarCommand() {
        this.name = "setavatar";
        this.help = "sets the avatar of the bot";
        this.arguments = "<url>";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        String url;
        if (event.getArgs().isEmpty()) {
            if (!event.getMessage().getAttachments().isEmpty() && event.getMessage().getAttachments().get(0).isImage()) {
                url = event.getMessage().getAttachments().get(0).getUrl();
            } else {
                url = null;
            }
        } else {
            url = event.getArgs();
        }

        InputStream inputStream = OtherUtil.imageFromUrl(url);
        if (inputStream == null) {
            event.reply(event.getClient().getError() + " Invalid or missing URL");
        } else {
            try {
                event.getSelfUser().getManager().setAvatar(Icon.from(inputStream)).queue(
                        v -> event.reply(event.getClient().getSuccess() + " Successfully changed avatar."),
                        t -> event.reply(event.getClient().getError() + " Failed to set avatar."));
            } catch (IOException e) {
                event.reply(event.getClient().getError() + " Could not load from provided URL.");
            }
        }
    }
}
