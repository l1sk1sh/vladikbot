package com.l1sk1sh.vladikbot.models.entities;

import com.l1sk1sh.vladikbot.settings.Const;

/**
 * @author Oliver Johnson
 */
public class GameAndAction {
    private String gameName;
    private Const.StatusAction action;

    public GameAndAction(String gameName, Const.StatusAction action) {
        this.gameName = gameName;
        this.action = action;
    }

    public String getGameName() {
        return gameName;
    }

    public Const.StatusAction getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "[" + gameName + ':'  + action + "]";
    }
}
