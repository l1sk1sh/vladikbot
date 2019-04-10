package com.multiheaded.vladikbot.models.entities;

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

    public String getRuleName() {
        return ruleName;
    }

    public List<String> getReactToList() {
        return reactToList;
    }

    public List<String> getReactWithList() {
        return reactWithList;
    }

    @Override
    public String toString() {
        return ruleName + ":" + Arrays.toString(reactToList.toArray())
                + "  " + Arrays.toString(reactWithList.toArray());
    }
}
