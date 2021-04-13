package com.l1sk1sh.vladikbot.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

/**
 * @author Oliver Johnson
 */
@AllArgsConstructor
@Getter
@ToString
public class ReplyRule {
    private final List<String> reactToList;
    private final List<String> reactWithList;

    public final int getRuleId() {
        return Arrays.toString(reactToList.toArray()).hashCode();
    }
}
