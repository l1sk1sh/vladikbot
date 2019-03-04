package com.multiheaded.disbot.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class CopyProcess extends AbstractProcess implements Runnable {
    private String pathToDockerFile;
    private String pathToLocalFile;

    private static final Logger logger = LoggerFactory.getLogger(CopyProcess.class);

    public CopyProcess(String containerName, String pathToDockerFile, String pathToLocalFile) {
        if (!running) {
            thread = new Thread(this, "DOCKER_COPY_STREAM");
            pb = new ProcessBuilder();
            pb.redirectErrorStream(true);
            this.pathToDockerFile = containerName + ":" + pathToDockerFile;
            this.pathToLocalFile = pathToLocalFile;
            thread.start();
        } else {
            logger.warn("Thread is already running.");
        }
    }

    @Override
    public void run() {
        running = true;
        try {
            pb.command("docker", "cp", pathToDockerFile, pathToLocalFile);

            final Process process = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                logger.info(line);
                if (line.contains("No such container:path")) {
                    throw new FileNotFoundException();
                } else if(line.contains("Error")) {
                    throw new IOException();
                }
            }

            process.waitFor();
            completed = true;
        } catch (FileNotFoundException fnf) {
            logger.error("Specified file or directory wasn't found.", fnf.getMessage(), fnf.getCause());
        } catch (IOException ioe) {
            logger.error("Failed to read output.", ioe.getMessage(), ioe.getCause());
        } catch (InterruptedException ie) {
            logger.error("Thread was interrupted.", ie.getMessage(), ie.getCause());
        } finally {
            running = false;
        }
    }
}
