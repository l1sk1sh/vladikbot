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
        return services.cms.status;
    }

    public String getWebsiteOnline() {
        return services.cms.title;
    }

    public String getCommunityStatus() {
        return services.community.status;
    }

    public String getCommunityOnline() {
        return services.community.title;
    }

    public String getDatabaseHealth() {
        return services.database.title;
    }

    public String getIngameStatus() {
        return services.ingame.status;
    }

    public String getIngameAmount() {
        return services.ingame.title;
    }

    public int getOnline() {
        return online;
    }

    public long getTime() {
        return time;
    }

    private static class Services {
        CMS cms;
        Community community;
        Database database;
        InGame ingame;

        private static class CMS {
            String status;
            String title;
        }

        private static class Community {
            String status;
            String title;
        }

        private static class Database {
            String title;
        }

        private static class InGame {
            String status;
            String title;
        }
    }
}
