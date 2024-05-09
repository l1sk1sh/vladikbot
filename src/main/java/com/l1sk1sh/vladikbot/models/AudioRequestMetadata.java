package com.l1sk1sh.vladikbot.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.User;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * @author John Grosh (john.a.grosh@gmail.com)
 */
@Getter
public class AudioRequestMetadata {
    public static final AudioRequestMetadata EMPTY = new AudioRequestMetadata(null);
    private final UserInfo user;

    public AudioRequestMetadata(User user) {
        this.user = (user == null)
                ? null
                : new UserInfo(user.getIdLong(), user.getName(), user.getEffectiveAvatarUrl());
    }

    public long getOwner() {
        return user == null ? 0L : user.id;
    }

    @SuppressWarnings("unused")
    @Getter
    @RequiredArgsConstructor
    public static class RequestInfo {
        private final String query;
        private final String url;
    }

    @Getter
    @RequiredArgsConstructor
    public static class UserInfo {
        private final long id;
        private final String username;
        private final String avatar;
    }
}