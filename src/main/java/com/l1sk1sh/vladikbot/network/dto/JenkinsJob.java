package com.l1sk1sh.vladikbot.network.dto;

import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author l1sk1sh
 */
@SuppressWarnings({"unused"})
@Getter
@Setter
@ToString
public class JenkinsJob {
    private String displayName;
    private boolean buildable;
    private Build lastBuild;
    private Object queueItem;

    @Getter
    public static class Build {
        private String id;
        private String displayName;
        private boolean building;
        private long duration;
        private String result;
        private long timestamp;

        public String getReadableDuration() {
            if (duration > 0) {
                return FormatUtils.getReadableMMSSDuration(duration);
            }

            long fromStartDuration = System.currentTimeMillis() - timestamp;

            if (fromStartDuration > 0) {
                return FormatUtils.getReadableMMSSDuration(fromStartDuration);
            }

            return null;
        }
    }
}
