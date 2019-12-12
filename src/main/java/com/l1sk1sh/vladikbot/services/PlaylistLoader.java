package com.l1sk1sh.vladikbot.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.l1sk1sh.vladikbot.models.entities.GameAndAction;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * - Files changed to .json
 * - Removed 'comment-shuffle' and added direct command to shuffle file itself
 * @author John Grosh
 */
public class PlaylistLoader {
    private static final Logger log = LoggerFactory.getLogger(PlaylistLoader.class);

    private final Bot bot;
    private final Gson gson;
    private final String playlistFolder;

    public PlaylistLoader(Bot bot) {
        this.bot = bot;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.playlistFolder = bot.getBotSettings().getPlaylistsFolder();
    }

    public List<String> getPlaylistNames() throws IOException {
        if (FileUtils.fileOrFolderIsAbsent(playlistFolder)) {
            FileUtils.createFolders(playlistFolder);

            // TODO inspection
            //noinspection unchecked
            return Collections.EMPTY_LIST;
        } else {
            File folder = new File(playlistFolder);
            return Arrays.stream(Objects.requireNonNull(folder.listFiles((pathname) ->
                    pathname.getName().endsWith("." + Const.FileType.json))))
                    .map(f ->
                            f.getName().substring(0, f.getName().length() - ("." + Const.FileType.json.name()).length())).collect(Collectors.toList());
        }
    }

    public void createPlaylist(String name) throws IOException {
        FileUtils.createFile(playlistFolder + name + "." + Const.FileType.json.name());
        log.info("Created new playlist '{}'.", name);
    }

    public void deletePlaylist(String name) throws IOException {
        FileUtils.deleteFile(playlistFolder + name + "." + Const.FileType.json.name());
        log.info("Deleted playlist '{}'.", name);
    }

    public void writePlaylist(String name, List<String> listToWrite) throws IOException {
        JsonWriter writer = new JsonWriter(
                new FileWriter(playlistFolder + name + "." + Const.FileType.json.name()));
        writer.setIndent("  ");
        writer.setHtmlSafe(false);
        gson.toJson(listToWrite, listToWrite.getClass(), writer);
        writer.close();
    }

    public Playlist getPlaylist(String name) {
        try {
            if (!getPlaylistNames().contains(name)) {
                return null;
            }

            if (FileUtils.fileOrFolderIsAbsent(playlistFolder)) {
                FileUtils.createFolders(playlistFolder);
                
                return null;
            } else {
                List<String> list = gson.fromJson(new FileReader(playlistFolder
                        + name + "." + Const.FileType.json.name()), new TypeToken<List<String>>(){}.getType());
                return new Playlist(name, list);
            }
        } catch (IOException e) {
            return null;
        }
    }

    public void shuffle(String name) throws IOException {
        if (getPlaylist(name).getItems().isEmpty() || (getPlaylist(name).getItems() == null)) {
            throw new IOException("Playlist is empty and can't be shuffled");
        }

        List<String> listToShuffle = getPlaylist(name).getItems();
        for (int first = 0; first < listToShuffle.size(); first++) {
            int second = (int) (Math.random() * listToShuffle.size());
            String tmp = listToShuffle.get(first);
            listToShuffle.set(first, listToShuffle.get(second));
            listToShuffle.set(second, tmp);
        }
        writePlaylist(name, listToShuffle);
    }

    public class Playlist {
        private final String name;
        private final List<String> items;
        private final List<AudioTrack> tracks = new LinkedList<>();
        private final List<PlaylistLoadError> errors = new LinkedList<>();
        private boolean loaded = false;

        private Playlist(String name, List<String> items) {
            this.name = name;
            this.items = items;
        }

        public void loadTracks(AudioPlayerManager manager, Consumer<AudioTrack> consumer, Runnable callback) {
            if (loaded) {
                return;
            }

            loaded = true;

            for (int i = 0; i < items.size(); i++) {
                boolean last = i + 1 == items.size();
                int index = i;
                manager.loadItemOrdered(name, items.get(i), new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack at) {
                        if (bot.getBotSettings().isTooLong(at)) {
                            errors.add(new PlaylistLoadError(index, items.get(index),
                                    "This track is longer than the allowed maximum"));
                        } else {
                            at.setUserData(0L);
                            tracks.add(at);
                            consumer.accept(at);
                        }
                        if (last && callback != null) {
                            callback.run();
                        }
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist audioPlaylist) {
                        if (audioPlaylist.isSearchResult()) {
                            if (bot.getBotSettings().isTooLong(audioPlaylist.getTracks().get(0))) {
                                errors.add(new PlaylistLoadError(index, items.get(index),
                                        "This track is longer than the allowed maximum"));
                            } else {
                                audioPlaylist.getTracks().get(0).setUserData(0L);
                                tracks.add(audioPlaylist.getTracks().get(0));
                                consumer.accept(audioPlaylist.getTracks().get(0));
                            }
                        } else if (audioPlaylist.getSelectedTrack() != null) {
                            if (bot.getBotSettings().isTooLong(audioPlaylist.getSelectedTrack())) {
                                errors.add(new PlaylistLoadError(index, items.get(index),
                                        "This track is longer than the allowed maximum"));
                            } else {
                                audioPlaylist.getSelectedTrack().setUserData(0L);
                                tracks.add(audioPlaylist.getSelectedTrack());
                                consumer.accept(audioPlaylist.getSelectedTrack());
                            }
                        } else {
                            List<AudioTrack> loaded = new ArrayList<>(audioPlaylist.getTracks());

                            loaded.removeIf(bot.getBotSettings()::isTooLong);
                            loaded.forEach(at -> at.setUserData(0L));
                            tracks.addAll(loaded);
                            loaded.forEach(consumer);
                        }
                        if (last && callback != null) {
                            callback.run();
                        }
                    }

                    @Override
                    public void noMatches() {
                        errors.add(new PlaylistLoadError(index, items.get(index), "No matches found."));
                        if (last && callback != null) {
                            callback.run();
                        }
                    }

                    @Override
                    public void loadFailed(FriendlyException fe) {
                        errors.add(new PlaylistLoadError(index, items.get(index), "Failed to load track: "
                                + fe.getLocalizedMessage()));
                        if (last && callback != null) {
                            callback.run();
                        }
                    }
                });

            }
        }

        public List<String> getItems() {
            return items;
        }

        public List<AudioTrack> getTracks() {
            return tracks;
        }

        public List<PlaylistLoadError> getErrors() {
            return errors;
        }
    }

    public static class PlaylistLoadError {
        private final int number;
        private final String item;
        private final String reason;

        private PlaylistLoadError(int number, String item, String reason) {
            this.number = number;
            this.item = item;
            this.reason = reason;
        }

        public int getIndex() {
            return number;
        }

        public String getItem() {
            return item;
        }

        public String getReason() {
            return reason;
        }
    }
}
