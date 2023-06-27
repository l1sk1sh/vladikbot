package com.l1sk1sh.vladikbot.contexts;

import com.jagrosh.jdautilities.command.UserContextMenu;
import com.jagrosh.jdautilities.command.UserContextMenuEvent;
import com.l1sk1sh.vladikbot.commands.everyone.DickCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class DickContextMenu extends UserContextMenu {

    private final DickCommand dickCommand;

    public DickContextMenu(DickCommand dickCommand) {
        this.dickCommand = dickCommand;
        this.name = "Пісюн?";
    }

    @Override
    protected void execute(UserContextMenuEvent event) {
        dickCommand.callDickGrow(event);
    }
}