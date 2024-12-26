package com.jagrosh.jdautilities.command;

import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.util.*;

/**
 * "Fix" of SlashCommand class for updated JDA version.
 */
@Getter
public abstract class SlashCommand extends Command {
    protected Map<DiscordLocale, String> nameLocalization = new HashMap<>();
    protected Map<DiscordLocale, String> descriptionLocalization = new HashMap<>();
    protected String requiredRole = null;
    protected SlashCommand[] children = new SlashCommand[0];
    protected SubcommandGroupData subcommandGroup = null;
    protected List<OptionData> options = new ArrayList<>();
    protected CommandClient client;

    public SlashCommand() {
    }

    protected abstract void execute(SlashCommandEvent var1);

    @SuppressWarnings("unused")
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
    }

    protected void execute(CommandEvent event) {
    }

    @SuppressWarnings("unused")
    public final void run(SlashCommandEvent event) {
        this.client = event.getClient();
        if (this.ownerCommand && !this.isOwner(event, this.client)) {
            this.terminate(event, "Only an owner may run this command. Sorry.", this.client);
        } else {
            try {
                if (!this.isAllowed(event.getTextChannel())) {
                    this.terminate(event, "That command cannot be used in this channel!", this.client);
                    return;
                }
            } catch (Exception ignored) {
            }

            if (this.requiredRole != null && (event.getChannelType() != ChannelType.TEXT || Objects.requireNonNull(event.getMember()).getRoles().stream().noneMatch((r) -> r.getName().equalsIgnoreCase(this.requiredRole)))) {
                this.terminate(event, this.client.getError() + " You must have a role called `" + this.requiredRole + "` to use that!", this.client);
            } else {
                int remaining;
                if (event.getChannelType() != ChannelType.PRIVATE) {
                    Permission[] var2 = this.userPermissions;
                    remaining = var2.length;

                    int var4;
                    Permission p;
                    for (var4 = 0; var4 < remaining; ++var4) {
                        p = var2[var4];
                        if (event.getMember() != null) {
                            if (p.isChannel()) {
                                if (!event.getMember().hasPermission(event.getGuildChannel(), p)) {
                                    this.terminate(event, String.format(this.userMissingPermMessage, this.client.getError(), p.getName(), "channel"), this.client);
                                    return;
                                }
                            } else if (!event.getMember().hasPermission(p)) {
                                this.terminate(event, String.format(this.userMissingPermMessage, this.client.getError(), p.getName(), "server"), this.client);
                                return;
                            }
                        }
                    }

                    var2 = this.botPermissions;
                    remaining = var2.length;

                    for (var4 = 0; var4 < remaining; ++var4) {
                        p = var2[var4];
                        if (p != Permission.VIEW_CHANNEL && p != Permission.MESSAGE_EMBED_LINKS) {
                            Member selfMember = event.getGuild() == null ? null : event.getGuild().getSelfMember();
                            if (p.isChannel()) {
                                GuildVoiceState gvc = Objects.requireNonNull(event.getMember()).getVoiceState();
                                AudioChannel vc = gvc == null ? null : gvc.getChannel();
                                if (vc == null) {
                                    this.terminate(event, this.client.getError() + " You must be in a voice channel to use that!", this.client);
                                    return;
                                }

                                if (!Objects.requireNonNull(selfMember).hasPermission(vc, p)) {
                                    this.terminate(event, String.format(this.botMissingPermMessage, this.client.getError(), p.getName(), "voice channel"), this.client);
                                    return;
                                }
                            } else if (!Objects.requireNonNull(selfMember).hasPermission(event.getGuildChannel(), p)) {
                                this.terminate(event, String.format(this.botMissingPermMessage, this.client.getError(), p.getName(), "channel"), this.client);
                                return;
                            }
                        }
                    }

                    if (this.nsfwOnly && event.getChannelType() == ChannelType.TEXT && !event.getTextChannel().isNSFW()) {
                        this.terminate(event, "This command may only be used in NSFW text channels!", this.client);
                        return;
                    }
                } else if (this.guildOnly) {
                    this.terminate(event, this.client.getError() + " This command cannot be used in direct messages", this.client);
                    return;
                }

                if (this.cooldown > 0 && !this.isOwner(event, this.client)) {
                    String key = this.getCooldownKey(event);
                    remaining = this.client.getRemainingCooldown(key);
                    if (remaining > 0) {
                        this.terminate(event, this.getCooldownError(event, remaining, this.client), this.client);
                        return;
                    }

                    this.client.applyCooldown(key, this.cooldown);
                }

                try {
                    this.execute(event);
                } catch (Throwable var10) {
                    if (this.client.getListener() != null) {
                        this.client.getListener().onSlashCommandException(event, this, var10);
                        return;
                    }

                    throw var10;
                }

                if (this.client.getListener() != null) {
                    this.client.getListener().onCompletedSlashCommand(event, this);
                }

            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isOwner(SlashCommandEvent event, CommandClient client) {
        if (event.getUser().getId().equals(client.getOwnerId())) {
            return true;
        } else if (client.getCoOwnerIds() == null) {
            return false;
        } else {
            String[] var3 = client.getCoOwnerIds();
            for (String id : var3) {
                if (id.equals(event.getUser().getId())) {
                    return true;
                }
            }

            return false;
        }
    }

    @SuppressWarnings("unused")
    public CommandData buildCommandData() {
        SlashCommandData data = Commands.slash(this.getName(), this.getHelp());
        if (!this.getOptions().isEmpty()) {
            data.addOptions(this.getOptions());
        }

        if (!this.getNameLocalization().isEmpty()) {
            data.setNameLocalizations(this.getNameLocalization());
        }

        if (!this.getDescriptionLocalization().isEmpty()) {
            data.setDescriptionLocalizations(this.getDescriptionLocalization());
        }

        if (this.children.length != 0) {
            Map<String, SubcommandGroupData> groupData = new HashMap<>();
            SlashCommand[] var3 = this.children;
            int var4 = var3.length;

            for (SlashCommand child : var3) {
                SubcommandData subcommandData = new SubcommandData(child.getName(), child.getHelp());
                if (!child.getOptions().isEmpty()) {
                    subcommandData.addOptions(child.getOptions());
                }

                if (!child.getNameLocalization().isEmpty()) {
                    subcommandData.setNameLocalizations(child.getNameLocalization());
                }

                if (!child.getDescriptionLocalization().isEmpty()) {
                    subcommandData.setDescriptionLocalizations(child.getDescriptionLocalization());
                }

                if (child.getSubcommandGroup() != null) {
                    SubcommandGroupData group = child.getSubcommandGroup();
                    SubcommandGroupData newData = groupData.getOrDefault(group.getName(), group).addSubcommands(subcommandData);
                    groupData.put(group.getName(), newData);
                } else {
                    data.addSubcommands(subcommandData);
                }
            }

            if (!groupData.isEmpty()) {
                data.addSubcommandGroups(groupData.values());
            }
        }

        if (this.getUserPermissions() == null) {
            data.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
        } else {
            data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(this.getUserPermissions()));
        }

        data.setGuildOnly(this.guildOnly);
        return data;
    }

    private void terminate(SlashCommandEvent event, String message, CommandClient client) {
        if (message != null) {
            event.reply(message).setEphemeral(true).queue();
        }

        if (client.getListener() != null) {
            client.getListener().onTerminatedSlashCommand(event, this);
        }

    }

    public String getCooldownKey(SlashCommandEvent event) {
        return switch (this.cooldownScope) {
            case USER -> this.cooldownScope.genKey(this.name, event.getUser().getIdLong());
            case USER_GUILD ->
                    event.getGuild() != null ? this.cooldownScope.genKey(this.name, event.getUser().getIdLong(), event.getGuild().getIdLong()) : CooldownScope.USER_CHANNEL.genKey(this.name, event.getUser().getIdLong(), event.getChannel().getIdLong());
            case USER_CHANNEL ->
                    this.cooldownScope.genKey(this.name, event.getUser().getIdLong(), event.getChannel().getIdLong());
            case GUILD ->
                    event.getGuild() != null ? this.cooldownScope.genKey(this.name, event.getGuild().getIdLong()) : CooldownScope.CHANNEL.genKey(this.name, event.getChannel().getIdLong());
            case CHANNEL -> this.cooldownScope.genKey(this.name, event.getChannel().getIdLong());
            case SHARD -> {
                event.getJDA().getShardInfo();
                yield this.cooldownScope.genKey(this.name, event.getJDA().getShardInfo().getShardId());
            }
            case USER_SHARD -> {
                event.getJDA().getShardInfo();
                yield this.cooldownScope.genKey(this.name, event.getUser().getIdLong(), event.getJDA().getShardInfo().getShardId());
            }
            case GLOBAL -> this.cooldownScope.genKey(this.name, 0L);
        };
    }

    public String getCooldownError(SlashCommandEvent event, int remaining, CommandClient client) {
        if (remaining <= 0) {
            return null;
        } else {
            String front = client.getWarning() + " That command is on cooldown for " + remaining + " more seconds";
            if (this.cooldownScope.equals(CooldownScope.USER)) {
                return front + "!";
            } else if (this.cooldownScope.equals(CooldownScope.USER_GUILD) && event.getGuild() == null) {
                return front + " " + CooldownScope.USER_CHANNEL.errorSpecification + "!";
            } else {
                return this.cooldownScope.equals(CooldownScope.GUILD) && event.getGuild() == null ? front + " " + CooldownScope.CHANNEL.errorSpecification + "!" : front + " " + this.cooldownScope.errorSpecification + "!";
            }
        }
    }
}
