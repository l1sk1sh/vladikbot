package com.l1sk1sh.vladikbot.services.audio;

import com.l1sk1sh.vladikbot.data.entity.Playlist;
import com.l1sk1sh.vladikbot.data.repository.PlaylistRepository;
import com.l1sk1sh.vladikbot.models.PlaylistLoadError;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformatted code
 * - Files changed to .json
 * - Removed 'comment-shuffle' and added direct command to shuffle file itself
 * - DI Spring
 * - Removed playlist data-class
 * @author John Grosh
 */
@Service
public class PlaylistLoader {
    private static final Logger log = LoggerFactory.getLogger(PlaylistLoader.class);

    private final BotSettingsManager settings;
    private final PlaylistRepository playlistRepository;

    @Autowired
    public PlaylistLoader(BotSettingsManager settings, PlaylistRepository playlistRepository) {
        this.settings = settings;
        this.playlistRepository = playlistRepository;
    }

    public List<String> getPlaylistNames() {
        List<Playlist> playlists = playlistRepository.findAll();

        return playlists.stream().map(Playlist::getName).collect(Collectors.toList());
    }

    public void createPlaylist(String name) {
        playlistRepository.save(new Playlist(name));
        log.info("Created new playlist '{}'.", name);
    }

    public boolean deletePlaylist(String name) {
        Playlist playlist = getPlaylist(name);

        if (getPlaylist(name) != null) {
            playlistRepository.delete(playlist);
            log.info("Deleted playlist '{}'.", name);
            return true;
        } else {
            return false;
        }
    }

    public void writePlaylist(String name, List<String> listToWrite) {
        Playlist playlist = getPlaylist(name);

        if (playlist == null) {
            playlist = new Playlist(name);
        }

        playlist.setItems(listToWrite);

        playlistRepository.save(playlist);
    }

    public Playlist getPlaylist(String name) {
        return playlistRepository.getPlaylistByName(name);
    }

    public boolean shuffle(String name) {
        if (getPlaylist(name).getItems().isEmpty() || (getPlaylist(name).getItems() == null)) {
            return false;
        }

        List<String> listToShuffle = getPlaylist(name).getItems();
        for (int first = 0; first < listToShuffle.size(); first++) {
            int second = (int) (Math.random() * listToShuffle.size());
            String tmp = listToShuffle.get(first);
            listToShuffle.set(first, listToShuffle.get(second));
            listToShuffle.set(second, tmp);
        }
        writePlaylist(name, listToShuffle);
        return true;
    }

    public void loadTracksIntoPlaylist(Playlist playlist, AudioPlayerManager manager, Consumer<AudioTrack> consumer, Runnable callback) {
        if (playlist.isLoaded()) {
            return;
        }

        playlist.setLoaded(true);

        for (int i = 0; i < playlist.getItems().size(); i++) {
            boolean last = i + 1 == playlist.getItems().size();
            int index = i;
            manager.loadItemOrdered(playlist.getName(), playlist.getItems().get(i), new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack at) {
                    if (settings.get().isTooLong(at)) {
                        playlist.getErrors().add(new PlaylistLoadError(index, playlist.getItems().get(index),
                                "This track is longer than the allowed maximum"));
                    } else {
                        at.setUserData(0L);
                        playlist.getTracks().add(at);
                        consumer.accept(at);
                    }
                    if (last && callback != null) {
                        callback.run();
                    }
                }

                @Override
                public void playlistLoaded(AudioPlaylist audioPlaylist) {
                    if (audioPlaylist.isSearchResult()) {
                        if (settings.get().isTooLong(audioPlaylist.getTracks().get(0))) {
                            playlist.getErrors().add(new PlaylistLoadError(index, playlist.getItems().get(index),
                                    "This track is longer than the allowed maximum"));
                        } else {
                            audioPlaylist.getTracks().get(0).setUserData(0L);
                            playlist.getTracks().add(audioPlaylist.getTracks().get(0));
                            consumer.accept(audioPlaylist.getTracks().get(0));
                        }
                    } else if (audioPlaylist.getSelectedTrack() != null) {
                        if (settings.get().isTooLong(audioPlaylist.getSelectedTrack())) {
                            playlist.getErrors().add(new PlaylistLoadError(index, playlist.getItems().get(index),
                                    "This track is longer than the allowed maximum"));
                        } else {
                            audioPlaylist.getSelectedTrack().setUserData(0L);
                            playlist.getTracks().add(audioPlaylist.getSelectedTrack());
                            consumer.accept(audioPlaylist.getSelectedTrack());
                        }
                    } else {
                        List<AudioTrack> loaded = new ArrayList<>(audioPlaylist.getTracks());

                        loaded.removeIf(settings.get()::isTooLong);
                        loaded.forEach(at -> at.setUserData(0L));
                        playlist.getTracks().addAll(loaded);
                        loaded.forEach(consumer);
                    }
                    if (last && callback != null) {
                        callback.run();
                    }
                }

                @Override
                public void noMatches() {
                    playlist.getErrors().add(new PlaylistLoadError(index, playlist.getItems().get(index), "No matches found."));
                    if (last && callback != null) {
                        callback.run();
                    }
                }

                @Override
                public void loadFailed(FriendlyException fe) {
                    playlist.getErrors().add(new PlaylistLoadError(index, playlist.getItems().get(index), "Failed to load track: "
                            + fe.getLocalizedMessage()));
                    if (last && callback != null) {
                        callback.run();
                    }
                }
            });
        }
    }
}
