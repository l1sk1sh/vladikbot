package com.l1sk1sh.vladikbot.network.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author l1sk1sh
 */
@SuppressWarnings({"unused"})
@Getter
@Setter
@ToString
public class JenkinsJobsList {
    private List<JobDescription> jobs;

    @Getter
    public static class JobDescription {
        private String name;
        private String color;

        public boolean isRunning() {
            return color.contains("_anime");
        }
    }
}
