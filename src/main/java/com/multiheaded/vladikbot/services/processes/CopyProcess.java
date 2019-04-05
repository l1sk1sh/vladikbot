package com.multiheaded.vladikbot.services.processes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author Oliver Johnson
 */
public class CopyProcess {
    private static final Logger logger = LoggerFactory.getLogger(CopyProcess.class);

    public CopyProcess(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        pb.command(command);

        final Process process = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = br.readLine()) != null) {
            logger.debug(line);
            if (line.contains("No such container:path")) {
                throw new FileNotFoundException(line);
            } else if (line.contains("Error")) {
                throw new IOException(line);
            }
        }

        process.waitFor();
    }
}
