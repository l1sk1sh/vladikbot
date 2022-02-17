package com.l1sk1sh.vladikbot.models.mood;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class MoodPoint {
    private int x;
    private int y;
    private MoodStatus mood;
}
