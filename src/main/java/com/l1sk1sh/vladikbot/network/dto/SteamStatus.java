package com.l1sk1sh.vladikbot.network.dto;

import lombok.Getter;
import lombok.ToString;

/**
 * @author Oliver Johnson
 */
@SuppressWarnings("unused")
@Getter
@ToString
public class SteamStatus {
    private int online;
    private Services services;
    private long time;

    public String getWebsiteStatus() {
        return services.getCms().getStatus();
    }

    public String getWebsiteOnline() {
        return services.getCms().getTitle();
    }

    public String getCommunityStatus() {
        return services.getCommunity().getStatus();
    }

    public String getCommunityOnline() {
        return services.getCommunity().getTitle();
    }

    public String getDatabaseHealth() {
        return services.getDatabase().getTitle();
    }

    public String getIngameStatus() {
        return services.getIngame().getStatus();
    }

    public String getIngameAmount() {
        return services.getIngame().getTitle();
    }

    public int getOnline() {
        return online;
    }

    public long getTime() {
        return time;
    }

    @Getter
    private static class Services {
        private CMS cms;
        private Community community;
        private Database database;
        private InGame ingame;

        @Getter
        private static class CMS {
            private String status;
            private String title;
        }

        @Getter
        private static class Community {
            private String status;
            private String title;
        }

        @Getter
        private static class Database {
            private String title;
        }

        @Getter
        private static class InGame {
            private String status;
            private String title;
        }
    }
}
