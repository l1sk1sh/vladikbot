package com.l1sk1sh.vladikbot.models.entities;

import com.l1sk1sh.vladikbot.settings.Const;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Oliver Johnson
 */
@AllArgsConstructor
@Getter
@ToString
public class GameAndAction {
    private final String gameName;
    private final Const.StatusAction action;
}
