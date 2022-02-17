package com.l1sk1sh.vladikbot.models;

import lombok.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author l1sk1sh
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
@NoArgsConstructor
public class EmoteStatsRecord implements Comparable<EmoteStatsRecord> {
    private String emoteName;
    private int amount;
    private long mostActiveUserId;

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof EmoteStatsRecord)
                && ((EmoteStatsRecord) obj).getEmoteName().equals(this.getEmoteName());
    }

    @Override
    public int hashCode() {
        return emoteName.hashCode();
    }

    @Override
    public int compareTo(@NotNull EmoteStatsRecord o) {
        return Integer.compare(this.getAmount(), o.getAmount());
    }
}
