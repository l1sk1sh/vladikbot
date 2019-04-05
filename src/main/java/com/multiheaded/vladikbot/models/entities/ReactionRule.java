package com.multiheaded.vladikbot.models.entities;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Oliver Johnson
 */
public class ReactionRule {
    private final String ruleName;
    private final Set<String> reactToList;
    private final Set<String> reactWithList;

    public ReactionRule(String ruleName, Set<String> reactToList, Set<String> reactWithList) {
        this.ruleName = ruleName;
        this.reactToList = reactToList;
        this.reactWithList = reactWithList;
    }

    public String getRuleName() {
        return ruleName;
    }

    public Set<String> getReactToList() {
        return reactToList;
    }

    public Set<String> getReactWithList() {
        return reactWithList;
    }

    @Override
    public String toString() {
        return ruleName + ":" + Arrays.toString(reactToList.toArray())
                + "  " + Arrays.toString(reactWithList.toArray());
    }
}
