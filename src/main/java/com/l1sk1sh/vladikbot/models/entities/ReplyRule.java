package com.l1sk1sh.vladikbot.models.entities;

import java.util.Arrays;
import java.util.List;

/**
 * @author Oliver Johnson
 */
public class ReplyRule {
    private final List<String> reactToList;
    private final List<String> reactWithList;

    public ReplyRule(List<String> reactToList, List<String> reactWithList) {
        this.reactToList = reactToList;
        this.reactWithList = reactWithList;
    }

    public final int getRuleId() {
        return Arrays.toString(reactToList.toArray()).hashCode();
    }

    public final List<String> getReactToList() {
        return reactToList;
    }

    public final List<String> getReactWithList() {
        return reactWithList;
    }

    @Override
    public final String toString() {
        return getRuleId() + ":" + Arrays.toString(reactToList.toArray())
                + "  " + Arrays.toString(reactWithList.toArray());
    }
}
