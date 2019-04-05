package com.multiheaded.vladikbot.services.processes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author Oliver Johnson
 */
public class BackupProcess {
    private static final Logger log = LoggerFactory.getLogger(BackupProcess.class);

    public BackupProcess(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        boolean failed = true;
        pb.command(command);

        final Process process = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = br.readLine()) != null) {
            log.debug(line);
            if (line.contains("Completed ✓")) {
                failed = false;
            } else if (line.contains("Error")) {
                throw new IOException(line);
            }
        }
        process.waitFor();

        if (failed) throw new IOException("Backup has not been completed");
    }
}
