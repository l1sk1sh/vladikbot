package com.multiheaded.vladikbot.services;

import com.multiheaded.vladikbot.models.LockService;
import com.multiheaded.vladikbot.settings.Constants;
import com.multiheaded.vladikbot.utils.FileUtils;
import com.multiheaded.vladikbot.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

import static com.multiheaded.vladikbot.utils.FileUtils.*;

/**
 * @author Oliver Johnson
 */
public class BackupMediaService {
    private static final Logger logger = LoggerFactory.getLogger(BackupMediaService.class);

    private boolean doZip = false;
    private boolean useSupportedMedia = true;
    private boolean downloadComplete;
    private File resultFile;

    public BackupMediaService(File exportedFile, String channelId, String localTmpPath, String localMediaPath,
                              String genericFileName, String[] args, LockService lock)
            throws IOException {

        try {
            lock.setAvailable(false);
            processArguments(args);

            String input = FileUtils.readFile(exportedFile, StandardCharsets.UTF_8);

            Matcher urlAttachmentsMatcher = Pattern.compile("https://cdn.discordapp.com/attachments/.*").matcher(input);
            Set<String> setOfMediaUrls = new HashSet<>();

            while (urlAttachmentsMatcher.find()) {
                if (useSupportedMedia) {
                    if (StringUtils.notInArray(
                            urlAttachmentsMatcher.group(), Constants.SUPPORTED_MEDIA_FORMATS)) {
                        continue;
                    }
                }
                setOfMediaUrls.add(urlAttachmentsMatcher.group());
            }

            logger.info("Writing media URLs into a file.");
            if (useSupportedMedia) {
                StringBuilder htmlContent = new StringBuilder();
                htmlContent.append("<!doctype html><html lang=\"en\"><head>");
                htmlContent.append(String.format("<title>%s</title>", genericFileName));
                htmlContent.append("</head><style>img {border: 1px solid #ddd;border-radius: 4px;");
                htmlContent.append("padding: 5px;width: 150px;}img:hover {");
                htmlContent.append("box-shadow: 0 0 2px 1px rgba(0, 140, 186, 0.5);}</style><body>");
                for (String url : setOfMediaUrls) {
                    htmlContent.append(String.format("<a target=\"_blank\" href=\"%s\"><img src=\"%s\"></a>", url, url));
                }
                htmlContent.append("</body></html>");
                String pathToHtmlFile = localTmpPath + genericFileName + Constants.HTML_EXTENSION;
                Files.write(Paths.get(pathToHtmlFile), htmlContent.toString().getBytes());
                resultFile = new File(pathToHtmlFile);
            } else {
                String pathToTxtFile = localTmpPath + genericFileName + Constants.TXT_EXTENSION;
                FileUtils.writeSetToFile(pathToTxtFile, setOfMediaUrls);
                resultFile = new File(pathToTxtFile);
            }

            if (doZip) {
                logger.info("Downloading media files from Discord CDN.");
                String mediaFolderPath = localMediaPath + "/" + channelId + "/";

                if (fileOrFolderIsAbsent(mediaFolderPath)) {
                    logger.info("Creating [{}] directory.", mediaFolderPath);
                    createFolders(mediaFolderPath);
                }

                for (String mediaUrl : setOfMediaUrls) {
                    if (useSupportedMedia) {
                        if (StringUtils.notInArray(mediaUrl, Constants.SUPPORTED_MEDIA_FORMATS)) continue;
                    }

                    String tempUrl = StringUtils.replaceLast(mediaUrl, "/", "_"); /* Replacing last '/' */
                    Matcher urlNameMatcher = Pattern.compile("[^/]+$").matcher(tempUrl); /* Getting exact file name */
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
            logger.error("Failed to read exported file, to write local file or to download media. {}", e.getLocalizedMessage());
            throw e;
        } finally {
            lock.setAvailable(true);
        }
    }

    private void downloadFile(URL url, String localFileNamePath) throws IOException {
        if (fileOrFolderIsAbsent(localFileNamePath)) {
            logger.info("Downloading file [{}]", localFileNamePath);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
            ReadableByteChannel readableByteChannel = Channels.newChannel(connection.getInputStream());
            FileOutputStream fileOutputStream = new FileOutputStream(localFileNamePath);
            FileChannel writeChannel = fileOutputStream.getChannel();
            writeChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
    }

    private void processArguments(String[] args) {
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

    public boolean doZip() {
        return doZip;
    }

    public boolean isDownloadComplete() {
        return downloadComplete;
    }

    public File getMediaUrlsFile() {
        return resultFile;
    }
}
