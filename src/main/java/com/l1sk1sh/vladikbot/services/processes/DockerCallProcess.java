package com.l1sk1sh.vladikbot.services.processes;

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
public class DockerCallProcess {
    private static final Logger log = LoggerFactory.getLogger(CopyProcess.class);

    public DockerCallProcess(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        pb.command(command);

        final Process process = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = br.readLine()) != null) {
            log.debug(line);
            if (line.contains("command not found")) {
                throw new FileNotFoundException("Command not found on line [" + line + "]");
            }
        }
        process.waitFor();
    }
}
