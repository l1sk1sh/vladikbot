package com.multiheaded.disbot.service;

import com.multiheaded.disbot.process.BackupProcess;
import com.multiheaded.disbot.process.CleanProcess;
import com.multiheaded.disbot.process.CopyProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.multiheaded.disbot.DisBot.settings;

public class BackupService {
    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);

    private boolean completed = false;

    public BackupService(String channelId, String format) {

        try {
            BackupProcess bp = new BackupProcess(settings.token, channelId, settings.dockerContainerName, format);
            bp.getThread().join();

            if(bp.isCompleted()) {
                CopyProcess cp = new CopyProcess(settings.dockerContainerName,
                        settings.dockerPathToExport, settings.localPathToExport);
                cp.getThread().join();
                completed = cp.isCompleted();
            }
        } catch (Exception error) {
            logger.error("Backup thread interrupted on service level.", error.getCause());
        } finally {
            new CleanProcess(settings.dockerContainerName);
        }
    }

    public boolean isCompleted() {
        return completed;
    }
}
