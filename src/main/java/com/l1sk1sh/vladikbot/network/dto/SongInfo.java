package com.l1sk1sh.vladikbot.network.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * @author l1sk1sh
 */
@SuppressWarnings({"unused", "MismatchedReadAndWriteOfArray"})
@Getter
@ToString
public class SongInfo {
    private Result[] results;

    public String getArtistName() {
        return results[0].artistName;
    }

    public String getCollectionName() {
        return results[0].collectionName;
    }

    public String getTrackName() {
        return results[0].trackName;
    }

    public String getArtistViewUrl() {
        return results[0].artistViewUrl;
    }

    public String getCollectionViewUrl() {
        return results[0].collectionViewUrl;
    }

    public String getTrackViewUrl() {
        return results[0].trackViewUrl;
    }

    public String getArtworkUrl100() {
        return results[0].artworkUrl100;
    }

    public Date getReleaseDate() {
        return results[0].releaseDate;
    }

    public String getPrimaryGenreName() {
        return results[0].primaryGenreName;
    }

    public static class Result {
        private String artistName;
        private String collectionName;
        private String trackName;
        private String artistViewUrl;
        private String collectionViewUrl;
        private String trackViewUrl;
        private String artworkUrl100;
        private Date releaseDate;
        private String primaryGenreName;
    }
}
