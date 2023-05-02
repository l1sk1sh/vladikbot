package com.l1sk1sh.vladikbot.models;

import lombok.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author l1sk1sh
 */
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@ToString
@NoArgsConstructor
public class MessageStatsRecord implements Comparable<MessageStatsRecord> {
    @NonNull
    private String message;
    @NonNull
    private Long amount;
    private Long mostActiveUserId;

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof MessageStatsRecord)
                && ((MessageStatsRecord) obj).getMessage().equals(this.getMessage());
    }

    @Override
    public int hashCode() {
        return message.hashCode();
    }

    @Override
    public int compareTo(@NotNull MessageStatsRecord o) {
        return Long.compare(this.getAmount(), o.getAmount());
    }
}
