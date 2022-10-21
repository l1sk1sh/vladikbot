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
public class EmojiStatsRecord implements Comparable<EmojiStatsRecord> {
    private String emojiName;
    private int amount;
    private long mostActiveUserId;

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof EmojiStatsRecord)
                && ((EmojiStatsRecord) obj).getEmojiName().equals(this.getEmojiName());
    }

    @Override
    public int hashCode() {
        return emojiName.hashCode();
    }

    @Override
    public int compareTo(@NotNull EmojiStatsRecord o) {
        return Integer.compare(this.getAmount(), o.getAmount());
    }
}
