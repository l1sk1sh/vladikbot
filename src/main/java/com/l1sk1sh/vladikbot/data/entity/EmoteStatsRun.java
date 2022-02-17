package com.l1sk1sh.vladikbot.data.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "emote_stats_run")
public class EmoteStatsRun {

    @Id
    @Column(name = "channel_id", nullable = false)
    @NonNull
    private Long channelId;

    @Column(name = "last_launched_time", nullable = false)
    @NonNull
    private Long lastLaunchedTime;
}
