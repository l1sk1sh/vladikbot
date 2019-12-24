package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.utils.FileUtils;

import java.io.File;
import java.io.IOException;

// TODO Remove it, and move method somewhere else
abstract class AbstractRulesManager {
    File getRulesFile(String rulesFolder, String rulesFileName) throws IOException {
        if (FileUtils.fileOrFolderIsAbsent(rulesFolder)) {
            FileUtils.createFolders(rulesFolder);

            return null;
        }

        File folder = new File(rulesFolder);

        if (folder.listFiles() == null) {
            return null;
        }

        File rulesFile = new File(rulesFolder + rulesFileName);

        if (!rulesFile.exists()) {
            return null;
        }

        return rulesFile;
    }
}
