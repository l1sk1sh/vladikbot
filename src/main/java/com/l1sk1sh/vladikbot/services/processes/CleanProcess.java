package com.l1sk1sh.vladikbot.services.processes;

import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author Oliver Johnson
 */
public class CleanProcess {
    private static final Logger log = LoggerFactory.getLogger(CleanProcess.class);

    public CleanProcess(List<String> command) throws IOException, NotFound, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        pb.command(command);

        final Process process = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = br.readLine()) != null) {
            log.debug(line);
            if (line.contains("No such container")) {
                throw new NotFound();
            } else if (line.contains("Error")) {
                throw new IOException(line);
            }
        }
        process.waitFor();
    }
}
