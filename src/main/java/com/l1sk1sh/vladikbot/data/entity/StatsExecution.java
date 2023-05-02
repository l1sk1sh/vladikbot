package com.l1sk1sh.vladikbot.data.entity;

import lombok.*;

import javax.persistence.*;

@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "stats_executions")
public class StatsExecution {

    @Id
    @Column(name = "channel_id", nullable = false)
    @NonNull
    private Long channelId;

    @Column(name = "last_launched_time", nullable = false)
    @NonNull
    private Long lastLaunchedTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Type type = Type.MESSAGE;

    public enum Type {
        EMOJI, MESSAGE
    }
}
