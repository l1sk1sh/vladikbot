package com.l1sk1sh.vladikbot.services.processes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Oliver Johnson
 */
public class CopyDockerFileProcess implements Callable<Integer> {
    private static final Logger log = LoggerFactory.getLogger(CopyDockerFileProcess.class);
    private final List<String> command;

    public CopyDockerFileProcess(String dockerContainerName, String dockerPathToExport, String localPathToExport) {
        command = new ArrayList<>();
        command.add("docker");
        command.add("cp");
        command.add(dockerContainerName + ":" + dockerPathToExport + ".");
        command.add(localPathToExport);
    }

    @Override
    public Integer call() throws IOException, InterruptedException {
        log.debug("CopyProcess receives command '{}'...", command);
        int exitCode;
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        pb.command(command);

        final Process process = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = br.readLine()) != null) {
            log.debug(line);

            if (line.contains("No such container:path")) {
                return 2;
            } else if (line.contains("Error")) {
                throw new IOException(line);
            }
        }

        exitCode = process.waitFor();
        return exitCode;
    }
}
