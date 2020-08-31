package com.l1sk1sh.vladikbot.services.backup;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.DownloadUtils;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import com.l1sk1sh.vladikbot.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Oliver Johnson
 */
public class BackupMediaService implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(BackupMediaService.class);

    private final Bot bot;
    private final String[] args;
    private final File textBackupFile;
    private File attachmentsHtmlFile;
    private File attachmentsTxtFile;
    private File zipWithAttachmentsFile;
    private final String localAttachmentsListName;
    private final String localAttachmentsListPath;
    private final String localAttachmentsPath;
    private final String channelId;
    private String failMessage = "Failed due to unexpected error";
    private String attachmentsFolderPath;
    private boolean doZip = false;
    private boolean doDownload = false;
    private boolean downloadCompleted = false;
    private boolean ignoreExistingFiles = true;
    private boolean hasFailed = true;
    private Set<String> setOfAllAttachmentsUrls;
    private Set<String> setOfSupportedAttachmentsUrls;

    public BackupMediaService(Bot bot, String channelId, File textBackupFile, String localAttachmentsListPath, String[] args) {
        this.bot = bot;
        this.args = args;
        this.channelId = channelId;
        this.textBackupFile = textBackupFile;
        this.localAttachmentsListName = textBackupFile.getName().replace("." + Const.FileType.html.name(), "") + " - media list";
        this.localAttachmentsListPath = localAttachmentsListPath + "media-lists/"; /* Always moving list files to separate folder */
        this.localAttachmentsPath = localAttachmentsListPath + "attachments/"; /* Always moving attachments to separate folder */
    }

    @Override
    public void run() {
        try {
            FileUtils.createFolderIfAbsent(localAttachmentsListPath);
            FileUtils.createFolderIfAbsent(localAttachmentsPath);

            bot.setLockedBackup(true);
            processArguments(args);

            attachmentsTxtFile = FileUtils.getFileByChannelIdAndExtension(localAttachmentsListPath, channelId, Const.FileType.txt);
            attachmentsHtmlFile = FileUtils.getFileByChannelIdAndExtension(localAttachmentsListPath, channelId, Const.FileType.html);

            /* If file is present or was made less than 24 hours ago - exit */
            if ((attachmentsTxtFile != null && ((System.currentTimeMillis() - attachmentsTxtFile.lastModified()) < Const.DAY_IN_MILLISECONDS))
                    && ignoreExistingFiles) {
                log.info("Media TXT list has already been made [{}].", attachmentsTxtFile.getAbsolutePath());
                hasFailed = false;

                return;
            }

            String textFromTextBackupFile = FileUtils.readFile(textBackupFile, StandardCharsets.UTF_8);

            Matcher urlAttachmentsMatcher = Pattern.compile("(?<=(href=\"))https://cdn.discordapp.com/attachments/.+?(?=\")").matcher(textFromTextBackupFile);
            setOfAllAttachmentsUrls = new HashSet<>();
            setOfSupportedAttachmentsUrls = new HashSet<>();

            while (urlAttachmentsMatcher.find()) {
                String matchedUrl = urlAttachmentsMatcher.group();

                if (StringUtils.stringContainsItemFromList(DownloadUtils.getFilenameFromUrl(matchedUrl), Const.NAME_INVALID_CHARS)) {
                    continue;
                }

                if (StringUtils.stringContainsItemFromList(matchedUrl, Const.FileType.getRawSupportedMediaFormatsAsArray())) {
                    setOfSupportedAttachmentsUrls.add(matchedUrl);
                }
                setOfAllAttachmentsUrls.add(matchedUrl);
            }

            log.info("Writing media URLs into a TXT file...");
            writeAttachmentsListTxtFile();

            log.info("Writing media URLs into a HTML file...");
            writeAttachmentsListHtmlFile();

            if (doDownload) {
                log.info("Downloading media files from Discord CDN...");
                downloadAttachments();
            }

            if (doZip) {
                if (!downloadCompleted) {
                    log.info("Downloading media files from Discord CDN for archiving...");
                    downloadAttachments();
                }

                log.info("Archiving media into .zip...");
                archiveAttachments();
            }

            log.debug("Media Backup Service has finished its execution.");
            hasFailed = false;

        } catch (IOException e) {
            failMessage = String.format("Something bad with files happened... [%1$s]", e.getLocalizedMessage());
            log.error("Failed to read exported file, to write local file or to download media. {}", e.getLocalizedMessage());
        } finally {
            bot.setLockedBackup(false);
        }
    }

    private void writeAttachmentsListTxtFile() throws IOException {
        String pathToTxtFile = localAttachmentsListPath + localAttachmentsListName + "." + Const.FileType.txt.name();

        BufferedWriter out = new BufferedWriter(new FileWriter(pathToTxtFile));

        for (String raw : setOfAllAttachmentsUrls) {
            out.write(raw);
            out.newLine();
        }

        out.close();

        attachmentsTxtFile = new File(pathToTxtFile);
    }

    private void writeAttachmentsListHtmlFile() throws IOException {
        String pathToHtmlFile = localAttachmentsListPath + localAttachmentsListName + "." + Const.FileType.html.name();

        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!doctype html><html lang=\"en\"><head>");
        htmlContent.append(String.format("<title>%1$s</title>", localAttachmentsListName));
        htmlContent.append("</head><style>img {border: 1px solid #ddd;border-radius: 4px;");
        htmlContent.append("padding: 5px;width: 150px;}img:hover {");
        htmlContent.append("box-shadow: 0 0 2px 1px rgba(0, 140, 186, 0.5);}</style><body>");
        for (String url : setOfSupportedAttachmentsUrls) {
            htmlContent.append(String.format("<a target=\"_blank\" href=\"%1$s\"><img src=\"%2$s\"></a>", url, url));
        }
        htmlContent.append("</body></html>");
        Files.write(Paths.get(pathToHtmlFile), htmlContent.toString().getBytes());
        attachmentsHtmlFile = new File(pathToHtmlFile);
    }

    private void archiveAttachments() throws IOException {
        String zipWithAttachmentsName = channelId + "." + Const.FileType.zip.name();
        String zipFolderPath = attachmentsFolderPath + "../archives"; /* Placing archives into parent folder */

        FileUtils.createFolderIfAbsent(zipFolderPath);
        File attachmentsFolderToZip = new File(attachmentsFolderPath);

        if (FileUtils.isDirEmpty(attachmentsFolderToZip.toPath())) {
            log.warn("Target directory '{}' is empty.", attachmentsFolderToZip.getPath());
            return;
        }

        zipWithAttachmentsFile = new File(zipFolderPath + "/" + zipWithAttachmentsName);
        if (zipWithAttachmentsFile.exists() && zipWithAttachmentsFile.delete()) {
            log.info("ZIP file has been located and removed before writing.");
        }

        if (!zipWithAttachmentsFile.createNewFile()) {
            log.error("Failed to create new file!");
            throw new IOException("Failed to create zip file at " + zipWithAttachmentsFile.getAbsolutePath());
        }

        ZipUtil.pack(attachmentsFolderToZip, zipWithAttachmentsFile);
    }

    private void downloadAttachments() throws IOException {
        attachmentsFolderPath = localAttachmentsPath + channelId + "/";

        FileUtils.createFolderIfAbsent(attachmentsFolderPath);

        String[] existingFiles = (new File(attachmentsFolderPath)).list();
        boolean doSearch = existingFiles != null && existingFiles.length > 0;
        Set<String> existingAttachments = new HashSet<>();
        if (doSearch) {
            Collections.addAll(existingAttachments, existingFiles);
        }

        for (String attachmentUrl : setOfAllAttachmentsUrls) {
            String remoteFileName = DownloadUtils.getFilenameFromUrl(attachmentUrl);

            if (remoteFileName.isEmpty()) {
                remoteFileName = System.currentTimeMillis() + "." + Const.FileType.jpg.name();
            }

            if (doSearch && existingAttachments.contains(remoteFileName)) {
                continue;
            }

            try {
                if (!DownloadUtils.downloadAndSaveToFile(new URL(attachmentUrl), attachmentsFolderPath + remoteFileName)) {
                    log.warn("Failed to save attachment file [{}].", remoteFileName);
                }
            } catch (InvalidPathException e) {
                log.warn("Failed to save attachment file [{}] due to invalid path.", remoteFileName, e);
            }
        }

        downloadCompleted = true;
    }

    private void processArguments(String[] args) {
        if (args == null || args.length == 0) {
            return;
        }

        for (String arg : args) {
            switch (arg) {
                case "-z":
                case "--zip":
                    doZip = true;

                    /* Falls through */
                case "-d":
                case "--download":
                    doDownload = true;
                    break;
                case "-f":
                case "--force":
                    ignoreExistingFiles = false;
                    break;
            }
        }
    }

    public final boolean doZip() {
        return doZip;
    }

    public final File getAttachmentHtmlFile() {
        return attachmentsHtmlFile;
    }

    public File getAttachmentsTxtFile() {
        return attachmentsTxtFile;
    }

    public final String getFailMessage() {
        return failMessage;
    }

    public final boolean hasFailed() {
        return hasFailed;
    }

    public File getZipWithAttachmentsFile() {
        return zipWithAttachmentsFile;
    }
}
