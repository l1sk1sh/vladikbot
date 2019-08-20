package com.multiheaded.vladikbot.services;

import com.multiheaded.vladikbot.Bot;
import com.multiheaded.vladikbot.models.RotatingTask;
import com.multiheaded.vladikbot.models.RotatingTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Johnson
 */
public class RotatingBackupMediaService implements RotatingTask {
    private static final Logger logger = LoggerFactory.getLogger(BackupChannelService.class);
    private RotatingTaskExecutor rotatingTaskExecutor;
    private Bot bot;

    public RotatingBackupMediaService(Bot bot) {
        this.bot = bot;
        rotatingTaskExecutor = new RotatingTaskExecutor(this);
    }

    public void execute() {
        /* TODO
           - Get list of available guilds
           - Get list of channels
           - Hit BackupChannelService in loop with those channels
           - Process response and send it to notification channel if available
         */
    }

    public void enableExecution() {
        rotatingTaskExecutor.startExecutionAt(bot.getBotSettings().getTargetHourForBackup(), 0, 0);
    }

    public void disableExecution() throws InterruptedException {
        rotatingTaskExecutor.stop();
    }
}
