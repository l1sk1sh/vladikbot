package com.multiheaded.vladikbot.conductors.services;

import com.multiheaded.vladikbot.settings.Constants;
import com.multiheaded.vladikbot.settings.LockdownInterface;
import com.multiheaded.vladikbot.utils.FileUtils;
import com.multiheaded.vladikbot.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.multiheaded.vladikbot.settings.Constants.TMP_MEDIA_FOLDER;

/**
 * @author Oliver Johnson
 */
public class BackupMediaService {
    private static final Logger logger = LoggerFactory.getLogger(BackupMediaService.class);

    private final String[] args;
    private boolean doZip = false;
    private boolean useSupportedMedia = true;
    private boolean downloadComplete;
    private File txtFile;

    public BackupMediaService(String channelId, File exportedFile, String localTmpPath, String genericFileName,
                              String[] args, LockdownInterface lock)
            throws IOException {
        this.args = args;

        try {
            lock.setLockdown(true);
            processArguments();

            String input = FileUtils.readFile(exportedFile, StandardCharsets.UTF_8);

            Matcher urlAttachmentsMatcher = Pattern.compile("https://cdn.discordapp.com/attachments/.*").matcher(input);
            Set<String> setOfMediaUrls = new HashSet<>();

            while (urlAttachmentsMatcher.find()) {
                setOfMediaUrls.add(urlAttachmentsMatcher.group());
            }

            logger.info("Writing media URLs into .txt file.");
            String pathToTxtFile = localTmpPath + genericFileName + ".txt";
            FileUtils.writeSetToFile(pathToTxtFile, setOfMediaUrls);
            txtFile = new File(pathToTxtFile);

            if (doZip) {
                logger.info("Downloading media files from Discord CDN.");
                String mediaFolderPath = localTmpPath + TMP_MEDIA_FOLDER + "/" + channelId + "/";

                if (!new File(mediaFolderPath).exists()) {
                    logger.info("Creating [{}] directory.", mediaFolderPath);
                    Files.createDirectories(Paths.get(mediaFolderPath));
                }

                for (String mediaUrl : setOfMediaUrls) {
                    if (useSupportedMedia) {
                        if (!StringUtils.containsStringFromArray(mediaUrl, Constants.SUPPORTED_MEDIA_FORMATS)) continue;
                    }

                    String tempUrl = StringUtils.replaceLast(mediaUrl, "/", "_"); //Replacing last '/'
                    Matcher urlNameMatcher = Pattern.compile("[^/]+$").matcher(tempUrl); //Getting exact file name
                    if (urlNameMatcher.find()) {
                        String remoteFileName = urlNameMatcher.group();
                        downloadFile(new URL(mediaUrl),
                                mediaFolderPath + remoteFileName);
                    }
                }

                FileOutputStream fos = new FileOutputStream(mediaFolderPath + System.currentTimeMillis() + ".zip");
                ZipOutputStream zipOut = new ZipOutputStream(fos);
                File fileToZip = new File(mediaFolderPath);

                zipFile(fileToZip, fileToZip.getName(), zipOut);
                zipOut.close();
                fos.close();

                downloadComplete = true;
            }

        } catch (IOException e) {
            logger.error("Failed to read exported file, to write local file or to download media. {}", e.getMessage());
            throw e;
        } finally {
            lock.setLockdown(false);
        }
    }

    private void downloadFile(URL url, String localFileNamePath) throws IOException {
        if (!new File(localFileNamePath).exists()) {
            logger.info("Downloading file [{}]", localFileNamePath);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
            ReadableByteChannel readableByteChannel = Channels.newChannel(connection.getInputStream());
            FileOutputStream fileOutputStream = new FileOutputStream(localFileNamePath);
            FileChannel writeChannel = fileOutputStream.getChannel();
            writeChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
    }

    private void processArguments() {
        if (args.length > 0) {
            for (String arg : args) {
                switch (arg) {
                    case "-z":
                    case "--zip":
                        doZip = true;
                        break;
                    case "-a":
                    case "--all":
                        useSupportedMedia = false;
                        break;
                }
            }
        }
    }

    private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : Objects.requireNonNull(children)) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    public boolean doZip() {
        return doZip;
    }

    public boolean isDownloadComplete() {
        return downloadComplete;
    }

    public File getTxtMediaSet() {
        return txtFile;
    }
}
