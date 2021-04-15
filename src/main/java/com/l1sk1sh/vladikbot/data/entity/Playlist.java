package com.l1sk1sh.vladikbot.data.entity;

import com.l1sk1sh.vladikbot.models.PlaylistLoadError;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.*;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author l1sk1sh
 */
@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "name", nullable = false)
    @NonNull
    private String name;

    @Column(name = "items")
    @ElementCollection
    @CollectionTable(name = "playlist_items", joinColumns = @JoinColumn(name = "id"))
    private List<String> items;

    @Transient
    private List<AudioTrack> tracks = new LinkedList<>();

    @Transient
    private List<PlaylistLoadError> errors = new LinkedList<>();

    @Transient
    private boolean loaded = false;
}
