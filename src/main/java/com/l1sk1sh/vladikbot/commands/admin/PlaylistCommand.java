package com.l1sk1sh.vladikbot.commands.admin;

import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.entity.Playlist;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.PlaylistLoader;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - Addition of separate shuffle command
 * - DI Spring
 * @author John Grosh
 */
@Slf4j
@Service
public class PlaylistCommand extends AdminCommand {

    private final PlaylistLoader playlistLoader;

    @Autowired
    public PlaylistCommand(GuildSettingsRepository guildSettingsRepository, PlaylistLoader playlistLoader) {
        this.playlistLoader = playlistLoader;
        this.name = "playlist";
        this.help = "Playlist management";
        this.guildOnly = false;
        this.children = new AdminCommand[]{
                new ReadCommand(),
                new UpdateCommand(),
                new DeleteCommand(),
                new CreateCommand(),
                new ShuffleCommand(),
                new DefaultListCommand(playlistLoader, guildSettingsRepository)
        };
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(this, children, name).toString()).setEphemeral(true).queue();
    }

    private final class CreateCommand extends AdminCommand {

        private static final String NAME_OPTION_KEY = "name";

        private CreateCommand() {
            this.name = "create";
            this.help = "Makes a new playlist";
            this.guildOnly = false;
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, NAME_OPTION_KEY, "Playlist's name").setRequired(false));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            OptionMapping nameOption = event.getOption(NAME_OPTION_KEY);
            if (nameOption == null) {
                event.replyFormat("%1$s Playlist's name is required!", getClient().getWarning()).setEphemeral(true).queue();

                return;
            }

            String playlistName = nameOption.getAsString();
            if (playlistLoader.getPlaylist(playlistName) == null) {
                playlistLoader.createPlaylist(playlistName);
                log.info("Playlist {} created by {}", playlistName, FormatUtils.formatAuthor(event));
                event.replyFormat("%1$s Successfully created playlist `%2$s`!", getClient().getSuccess(), playlistName).setEphemeral(true).queue();
            } else {
                event.replyFormat("%1$s Playlist `%2$s` already exists!", getClient().getWarning(), playlistName).setEphemeral(true).queue();
            }
        }
    }

    private final class ReadCommand extends AdminCommand {

        private ReadCommand() {
            this.name = "list";
            this.help = "Lists all available playlists";
            this.guildOnly = true;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            List<String> list = playlistLoader.getPlaylistNames();
            if (list == null) {
                event.replyFormat("%1$s Failed to load available playlists!", getClient().getWarning()).setEphemeral(true).queue();
            } else if (list.isEmpty()) {
                event.replyFormat("%1$s There are no playlists in the Playlists folder!", getClient().getWarning()).setEphemeral(true).queue();
            } else {
                String message = getClient().getSuccess() + " Available playlists:\r\n";
                StringBuilder builder = new StringBuilder(message);
                list.forEach(str -> builder.append("`").append(str).append("` "));
                event.reply(builder.toString()).setEphemeral(true).queue();
            }
        }
    }

    private final class UpdateCommand extends AdminCommand {

        private static final String URL_SEPARATOR = "\\|";

        private static final String NAME_OPTION_KEY = "name";
        private static final String URL_OPTION_KEY = "url";

        private UpdateCommand() {
            this.name = "update";
            this.help = "Appends songs to an existing playlist";
            this.guildOnly = false;
            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, NAME_OPTION_KEY, "Name of the playlist").setRequired(true));
            options.add(new OptionData(OptionType.STRING, URL_OPTION_KEY, "URL of a new song (for multiple URLs use " + URL_SEPARATOR + " as separator)").setRequired(true));
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            OptionMapping nameOption = event.getOption(NAME_OPTION_KEY);
            if (nameOption == null) {
                event.replyFormat("%1$s Specify name of playlist!",
                        getClient().getWarning()
                ).setEphemeral(true).queue();

                return;
            }

            OptionMapping urlOption = event.getOption(URL_OPTION_KEY);
            if (urlOption == null) {
                event.replyFormat("%1$s Specify at least one URL of a song!",
                        getClient().getWarning()
                ).setEphemeral(true).queue();

                return;
            }

            String playlistName = nameOption.getAsString();
            Playlist playlist = playlistLoader.getPlaylist(playlistName);

            if (playlist == null) {
                event.replyFormat("%1$s Playlist `%2$s` doesn't exist!", getClient().getError(), playlistName).setEphemeral(true).queue();
            } else {
                List<String> listOfUrlsToWrite = (playlist.getItems() == null)
                        ? new ArrayList<>()
                        : new ArrayList<>(playlist.getItems()
                );

                String joinedUrls = urlOption.getAsString();
                String[] urls = joinedUrls.split(URL_SEPARATOR);
                for (String url : urls) {
                    String u = url.trim();
                    if (u.startsWith("<") && u.endsWith(">")) {
                        u = u.substring(1, u.length() - 1);
                    }
                    listOfUrlsToWrite.add(u);
                }

                playlistLoader.writePlaylist(playlistName, listOfUrlsToWrite);
                log.info("Playlist {} updated by {}", playlistName, FormatUtils.formatAuthor(event));
                event.replyFormat("%1$s Successfully added %2$s items to playlist `%3$s`.", getClient().getSuccess(), urls.length, playlist).setEphemeral(true).queue();
            }
        }
    }

    private final class DeleteCommand extends AdminCommand {

        private static final String NAME_OPTION_KEY = "name";

        private DeleteCommand() {
            this.name = "delete";
            this.help = "Deletes an existing playlist";
            this.guildOnly = false;
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, NAME_OPTION_KEY, "Playlist's name").setRequired(false));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            OptionMapping nameOption = event.getOption(NAME_OPTION_KEY);
            if (nameOption == null) {
                event.replyFormat("%1$s Playlist's name is required!", getClient().getWarning()).setEphemeral(true).queue();

                return;
            }

            String playlistName = nameOption.getAsString();
            if (playlistLoader.getPlaylist(playlistName) == null) {
                event.replyFormat("%1$s Playlist `%2$s` doesn't exist!", getClient().getWarning(), playlistName).setEphemeral(true).queue();
            } else {
                if (playlistLoader.deletePlaylist(playlistName)) {
                    log.info("Playlist {} deleted by {}", playlistName, FormatUtils.formatAuthor(event));
                    event.replyFormat("%1$s Successfully deleted playlist `%2$s`", getClient().getSuccess(), playlistName).setEphemeral(true).queue();
                } else {
                    event.replyFormat("%1$s Unable to delete the playlist `%2$s`!", getClient().getError(), playlistName).setEphemeral(true).queue();
                }
            }
        }

    }

    private final class ShuffleCommand extends AdminCommand {

        private static final String NAME_OPTION_KEY = "name";

        private ShuffleCommand() {
            this.name = "shuffle";
            this.help = "Shuffles specified playlist";
            this.guildOnly = false;
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, NAME_OPTION_KEY, "Playlist's name").setRequired(false));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            OptionMapping nameOption = event.getOption(NAME_OPTION_KEY);
            if (nameOption == null) {
                event.replyFormat("%1$s Playlist's name is required!", getClient().getWarning()).setEphemeral(true).queue();

                return;
            }

            String playlistName = nameOption.getAsString();
            if (playlistLoader.getPlaylist(playlistName) == null) {
                event.replyFormat("%1$s Playlist `%2$s` doesn't exist!", getClient().getWarning(), playlistName).setEphemeral(true).queue();
            } else {
                List<String> list = playlistLoader.getPlaylistNames();
                if (list == null) {
                    event.replyFormat("%1$s Failed to load available playlists!", getClient().getError()).setEphemeral(true).queue();
                } else {
                    if (playlistLoader.shuffle(playlistName)) {
                        log.info("Playlist {} shuffled by {}", playlistName, FormatUtils.formatAuthor(event));
                        event.replyFormat("%1$s Successfully shuffled playlist `%2$s`", getClient().getSuccess(), playlistName).setEphemeral(true).queue();
                    } else {
                        event.replyFormat("%1$s Unable to shuffle the playlist `%2$s`!", getClient().getError(), playlistName).setEphemeral(true).queue();
                    }
                }
            }
        }
    }

    private static final class DefaultListCommand extends AdminCommand {

        private static final String PLAYLIST_OPTION_KEY = "playlist";

        private final PlaylistLoader playlistLoader;
        private final GuildSettingsRepository guildSettingsRepository;

        public DefaultListCommand(PlaylistLoader playlistLoader, GuildSettingsRepository guildSettingsRepository) {
            this.playlistLoader = playlistLoader;
            this.guildSettingsRepository = guildSettingsRepository;
            this.name = "default";
            this.help = "Sets the default playlist for the server";
            this.guildOnly = true;
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, PLAYLIST_OPTION_KEY, "Playlist's name. Set 'none' to remove one").setRequired(false));
        }

        @Override
        public final void execute(SlashCommandEvent event) {
            Optional<GuildSettings> settings = guildSettingsRepository.findById(Objects.requireNonNull(event.getGuild()).getIdLong());
            String currentPlaylist = settings.map(GuildSettings::getDefaultPlaylist).orElse(null);

            OptionMapping playlistOption = event.getOption(PLAYLIST_OPTION_KEY);
            if (playlistOption == null) {
                event.replyFormat("Current default playlist is %2$s", currentPlaylist).setEphemeral(true).queue();

                return;
            }

            String newPlaylist = playlistOption.getAsString();

            if (newPlaylist.equalsIgnoreCase("none")) {
                guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(guildSettings -> {
                    guildSettings.setDefaultPlaylist(null);
                    guildSettingsRepository.save(guildSettings);
                    String message = String.format("Cleared the default playlist for **%1$s**", event.getGuild().getName());
                    log.info("{}. Cleared by {}.", message, FormatUtils.formatAuthor(event));
                    event.replyFormat("%1$s %2$s", getClient().getSuccess(), message).setEphemeral(true).queue();
                });
                return;
            }

            String playlistName = newPlaylist.replaceAll("\\s+", "_");
            if (playlistLoader.getPlaylist(playlistName) == null) {
                event.replyFormat("%1$s Could not find `%2$s`!", getClient().getError(), playlistName).setEphemeral(true).queue();
            } else {
                guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(guildSettings -> {
                    guildSettings.setDefaultPlaylist(playlistName);
                    guildSettingsRepository.save(guildSettings);
                    log.info("Default playlist is set to {} by {}", playlistName, FormatUtils.formatAuthor(event));
                    event.replyFormat("%1$s The default playlist for **%2$s** is now `%3$s`", getClient().getSuccess(),
                            event.getGuild().getName(), playlistName).setEphemeral(true).queue();
                });
            }
        }
    }
}
