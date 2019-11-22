package com.l1sk1sh.vladikbot.models.entities;

import java.util.Arrays;
import java.util.List;

/**
 * @author Oliver Johnson
 */
public class ReactionRule {
    private final String ruleName;
    private final List<String> reactToList;
    private final List<String> reactWithList;

    public ReactionRule(String ruleName, List<String> reactToList, List<String> reactWithList) {
        this.ruleName = ruleName;
        this.reactToList = reactToList;
        this.reactWithList = reactWithList;
    }

    public final String getRuleName() {
        return ruleName;
    }

    public final List<String> getReactToList() {
        return reactToList;
    }

    public final List<String> getReactWithList() {
        return reactWithList;
    }

    @Override
    public final String toString() {
        return ruleName + ":" + Arrays.toString(reactToList.toArray())
                + "  " + Arrays.toString(reactWithList.toArray());
    }
}
