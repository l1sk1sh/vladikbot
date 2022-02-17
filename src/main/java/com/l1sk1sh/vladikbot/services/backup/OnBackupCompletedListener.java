package com.l1sk1sh.vladikbot.services.backup;

public interface OnBackupCompletedListener {
    void onBackupCompleted(boolean success, String message);
}
