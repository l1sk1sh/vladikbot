package com.multiheaded.disbot.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BackupProcess extends AbstractProcess implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(BackupProcess.class);

    private String token;
    private String channelId;
    private String containerName;
    private String format;

    public BackupProcess(String token, String channelId, String containerName, String format) {
        if (!running) {
            thread = new Thread(this, "DOCKER_CLI_STREAM");
            pb = new ProcessBuilder();
            pb.redirectErrorStream(true);
            this.token = token;
            this.channelId = channelId;
            this.containerName = containerName;
            this.format = format;
            thread.start();
        } else {
            logger.warn("Thread is already running.");
        }
    }

    @Override
    public void run() {
        running = true;
        try {
            pb.command("docker", "run", "--name", containerName,"tyrrrz/discordchatexporter", "export",
                    "-f", format, "--channel", channelId, "--token", token, "--bot", "true");

            final Process process = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                logger.info(line);
                if (line.contains("Completed ✓")) {
                    completed = true;
                }
            }

            process.waitFor();
        } catch (IOException ioe) {
            logger.error("Failed to read output.", ioe.getMessage(), ioe.getCause());
        } catch (InterruptedException ie) {
            logger.error("Thread was interrupted.", ie.getMessage(), ie.getCause());
        } finally {
            running = false;
        }
    }
}
