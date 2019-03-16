package com.multiheaded.vladikbot.models.playlist;

import com.multiheaded.vladikbot.settings.Settings;
import com.multiheaded.vladikbot.settings.SettingsManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
@SuppressWarnings("unchecked")
public class PlaylistLoader {
    private final Settings settings;

    public PlaylistLoader() {
        this.settings = SettingsManager.getInstance().getSettings();
    }

    public List<String> getPlaylistNames() {
        if (folderExists()) {
            File folder = new File(settings.getPlaylistsFolder());
            return Arrays.stream(
                    Objects.requireNonNull(folder.listFiles((pathname) -> pathname.getName().endsWith(".txt")))
            ).map(f -> f.getName().substring(0, f.getName().length() - 4)).collect(Collectors.toList());
        } else {
            createFolder();
            return Collections.EMPTY_LIST;
        }
    }

    public void createFolder() {
        try {
            Files.createDirectory(Paths.get(settings.getPlaylistsFolder()));
        } catch (IOException ignore) {
        }
    }

    public boolean folderExists() {
        return Files.exists(Paths.get(settings.getPlaylistsFolder()));
    }

    public void createPlaylist(String name) throws IOException {
        Files.createFile(Paths.get(settings.getPlaylistsFolder() + File.separator + name + ".txt"));
    }

    public void deletePlaylist(String name) throws IOException {
        Files.delete(Paths.get(settings.getPlaylistsFolder() + File.separator + name + ".txt"));
    }

    public void writePlaylist(String name, String text) throws IOException {
        Files.write(Paths.get(settings.getPlaylistsFolder() + File.separator + name + ".txt"),
                text.trim().getBytes());
    }

    public Playlist getPlaylist(String name) {
        if (!getPlaylistNames().contains(name))
            return null;
        try {
            if (folderExists()) {
                boolean[] shuffle = {false};
                List<String> list = new ArrayList<>();
                Files.readAllLines(Paths.get(settings.getPlaylistsFolder()
                        + File.separator + name + ".txt")).forEach(str ->
                {
                    String s = str.trim();
                    if (s.isEmpty()) {
                        return;
                    }
                    if (s.startsWith("#") || s.startsWith("//")) {
                        s = s.replaceAll("\\s+", "");
                        if (s.equalsIgnoreCase("#shuffle") || s.equalsIgnoreCase("//shuffle")) {
                            shuffle[0] = true;
                        }
                    } else {
                        list.add(s);
                    }
                });

                if (shuffle[0]) {
                    shuffle(list);
                }
                return new Playlist(name, list, shuffle[0]);
            } else {
                createFolder();
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }


    private static <T> void shuffle(List<T> list) {
        for (int first = 0; first < list.size(); first++) {
            int second = (int) (Math.random() * list.size());
            T tmp = list.get(first);
            list.set(first, list.get(second));
            list.set(second, tmp);
        }
    }


    public class Playlist {
        private final String name;
        private final List<String> items;
        private final boolean shuffle;
        private final List<AudioTrack> tracks = new LinkedList<>();
        private final List<PlaylistLoadError> errors = new LinkedList<>();
        private boolean loaded = false;

        private Playlist(String name, List<String> items, boolean shuffle) {
            this.name = name;
            this.items = items;
            this.shuffle = shuffle;
        }

        public void loadTracks(AudioPlayerManager manager, Consumer<AudioTrack> consumer, Runnable callback) {
            if (!loaded) {
                loaded = true;

                for (int i = 0; i < items.size(); i++) {
                    boolean last = i + 1 == items.size();
                    int index = i;
                    manager.loadItemOrdered(name, items.get(i), new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack at) {
                            if (settings.isTooLong(at)) {
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
                                if (settings.isTooLong(audioPlaylist.getTracks().get(0))) {
                                    errors.add(new PlaylistLoadError(index, items.get(index),
                                            "This track is longer than the allowed maximum"));
                                } else {
                                    audioPlaylist.getTracks().get(0).setUserData(0L);
                                    tracks.add(audioPlaylist.getTracks().get(0));
                                    consumer.accept(audioPlaylist.getTracks().get(0));
                                }
                            } else if (audioPlaylist.getSelectedTrack() != null) {
                                if (settings.isTooLong(audioPlaylist.getSelectedTrack())) {
                                    errors.add(new PlaylistLoadError(index, items.get(index),
                                            "This track is longer than the allowed maximum"));
                                } else {
                                    audioPlaylist.getSelectedTrack().setUserData(0L);
                                    tracks.add(audioPlaylist.getSelectedTrack());
                                    consumer.accept(audioPlaylist.getSelectedTrack());
                                }
                            } else {
                                List<AudioTrack> loaded = new ArrayList<>(audioPlaylist.getTracks());
                                if (shuffle) {
                                    for (int first = 0; first < loaded.size(); first++) {
                                        int second = (int) (Math.random() * loaded.size());
                                        AudioTrack tmp = loaded.get(first);
                                        loaded.set(first, loaded.get(second));
                                        loaded.set(second, tmp);
                                    }
                                }
                                loaded.removeIf(settings::isTooLong);
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
            if (shuffle) {
                shuffleTracks();
            }
        }

        void shuffleTracks() {
            shuffle(tracks);
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

    public class PlaylistLoadError {
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
