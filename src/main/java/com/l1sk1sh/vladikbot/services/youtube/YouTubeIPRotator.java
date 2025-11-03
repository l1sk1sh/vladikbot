package com.l1sk1sh.vladikbot.services.youtube;

import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class YouTubeIPRotator {

    private final BotSettingsManager settings;

    @Autowired
    public YouTubeIPRotator(BotSettingsManager settings) {
        this.settings = settings;
    }

    /*
     * Rotator is not finished yet
     */
    public void setRoutePlannerIfAvailable(HttpInterfaceManager ytHttpInterfaceManager) {
        settings.get();
        return;
    }
}
