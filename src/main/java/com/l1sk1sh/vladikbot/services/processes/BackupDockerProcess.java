package com.l1sk1sh.vladikbot.services.processes;

import com.l1sk1sh.vladikbot.settings.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Oliver Johnson
 */
public class BackupDockerProcess implements Callable<Integer> {
    private static final Logger log = LoggerFactory.getLogger(BackupDockerProcess.class);
    private final List<String> command;

    public BackupDockerProcess(String dockerContainerName, Const.BackupFileType format,
                               String beforeDate, String afterDate, String channelId, String token) {
        command = new ArrayList<>();
        command.add("docker");
        command.add("run");
        command.add("--name");
        command.add(dockerContainerName);
        command.add("tyrrrz/discordchatexporter");
        command.add("export");
        command.add("-f");
        command.add(format.getBackupTypeName());
        if (beforeDate != null) {
            command.add("--before");
            command.add(beforeDate);
        }
        if (afterDate != null) {
            command.add("--after");
            command.add(afterDate);
        }
        command.add("--channel");
        command.add(channelId);
        command.add("--token");
        command.add(token);
        command.add("--bot");
        command.add("true");
    }

    @Override
    public Integer call() throws IOException, InterruptedException {
        log.debug("BackupProcess receives command '{}'...", command);
        int exitCode;
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        pb.command(command);

        final Process process = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = br.readLine()) != null) {
            log.debug(line);

            if (line.contains("Error")) {
                throw new IOException(line);
            }
        }

        exitCode = process.waitFor();
        return exitCode;
    }
}
