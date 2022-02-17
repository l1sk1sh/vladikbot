package com.l1sk1sh.vladikbot.services.backup;

import java.io.File;

public interface OnFileCreatedListener {
    void onFileCreated(boolean success, String message, File file);
}
