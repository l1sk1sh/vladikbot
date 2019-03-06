package com.multiheaded.disbot.process;

import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class CleanProcess extends AbstractProcess implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CleanProcess.class);

    private List<String> command;

    public CleanProcess(List<String> command) {
        if (!running) {
            thread = new Thread(this, "DOCKER_CLEAN_STREAM");
            pb = new ProcessBuilder();
            pb.redirectErrorStream(true);
            this.command = command;
            thread.start();
        } else {
            logger.warn("Thread is already running.");
        }
    }

    @Override
    public void run() {
        running = true;
        try {
            pb.command(command);

            final Process process = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                logger.info(line);
                if (line.contains("No such container")) {
                    throw new NotFound();
                } else if (line.contains("Error")) {
                    throw new IOException();
                }
            }

            process.waitFor();
            completed = true;
        } catch (NotFound nf) {
            logger.error("Specified container not found.", nf.getMessage(), nf.getMessage());
        } catch (IOException ioe) {
            logger.error("Failed to read output.", ioe.getMessage(), ioe.getCause());
        } catch (InterruptedException ie) {
            logger.error("Thread was interrupted.", ie.getMessage(), ie.getCause());
        } finally {
            running = false;
        }
    }
}
