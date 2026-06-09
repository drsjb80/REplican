package edu.msudenver.cs.replican;

import lombok.NonNull;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;

class WebFile {
    private final YouAreEll yrl;
    private final Logger logger;
    private final ConfigProvider config;
    private final FileSystem fileSystem;

    // Legacy constructor for backward compatibility
    WebFile(final YouAreEll yrl) {
        this(yrl, new REplicanConfigProvider(REplican.ARGS), LogManager.getLogger(WebFile.class), new LocalFileSystem());
    }

    // New constructor with full dependency injection
    WebFile(@NonNull final YouAreEll yrl,
            @NonNull final ConfigProvider config,
            @NonNull final Logger logger,
            @NonNull final FileSystem fileSystem) {
        this.yrl = yrl;
        this.config = config;
        this.logger = logger;
        this.fileSystem = fileSystem;
    }

    private String getFilePath(@NonNull final String s) throws MalformedURLException {
        logger.traceEntry(s);

        URL url = new URL(s);

        String hostname = url.getHost();
        String filename = url.getFile();

        if (filename.endsWith("/")) {
            filename += config.getIndexName();
        }

        if (hostname == null) {
            hostname = "localhost";
        }

        logger.debug(hostname);
        logger.debug(filename);

        String path = hostname + filename;

        String dir = config.getDirectory();
        if (dir != null) {
            String separator = fileSystem.getFileSeparator();
            if (!dir.endsWith(separator))
                dir += separator;

            path = dir + path;
        }

        String[] filenameRewrite = config.getFilenameRewrite();
        if (filenameRewrite != null) {
            path = Utils.replaceAll(path, filenameRewrite);
        }
        path = path.replaceFirst("^~", fileSystem.getUserHome() + "/");

        logger.traceExit(path);
        return (path);
    }

    File openFile() throws MalformedURLException {
        String path = getFilePath(yrl.getUrl());

        boolean printSavePath = false;
        try {
            // Try to get this from config if available
            printSavePath = REplican.ARGS.PrintSavePath;
        } catch (Exception e) {
            // Ignore if not available
        }

        if (printSavePath) {
            logger.info("Saving to: " + path);
        }

        return new File(path);
    }

    private File openDirectory() throws MalformedURLException {
        String path = getFilePath(yrl.getUrl());

        String directoryPath;
        if (path.indexOf('/') == -1) {
            // just the filename, add relative path
            directoryPath = "./";
        } else {
            // remove the filename
            directoryPath = path.replaceAll("/[^/]*$", "");
        }

        logger.traceExit(directoryPath);
        return new File(directoryPath);
    }

    private void checkIfNewerThan() throws FileAlreadyExistsException {
        String ifNewerThan = config.getIfNewerThan();
        if (ifNewerThan != null) {
            File newerThan = new File(ifNewerThan);
            logger.debug("file time = " + fileSystem.lastModified(newerThan));
            logger.debug("url time = " + yrl.getLastModified());
            if (yrl.getLastModified() < fileSystem.lastModified(newerThan)) {
                if (config.isPrintSkip()) {
                    logger.info("Skipping because older than " + ifNewerThan);
                }
                throw new FileAlreadyExistsException(newerThan.toString());
            }
        }
    }

    private void checkIfModifiedSince(final File file, final long LastModified) throws FileAlreadyExistsException {
        logger.traceEntry(String.valueOf(LastModified));

        if (LastModified > 0) {
            logger.trace("file: " + fileSystem.lastModified(file));
            logger.trace("URL: " + LastModified);

            if (fileSystem.lastModified(file) >= LastModified) {
                if (config.isPrintSkip()) {
                    logger.info("Not modified: " + file);
                }
                throw new FileAlreadyExistsException(file.toString());
            }
        } else {
            logger.info("No last-modified information: " + file);
        }
    }

    private void checkIfLargerOrSmaller(final File file) throws FileAlreadyExistsException {
        boolean larger = yrl.getContentLength() > fileSystem.length(file);
        boolean smaller = yrl.getContentLength() < fileSystem.length(file);

        if ((config.isOverwriteIfLarger() && larger) ||
            (config.isOverwriteIfSmaller() && smaller)) {
                logger.info("Overwriting because " +
                yrl.getContentLength() + " is " +
                (config.isOverwriteIfLarger() ? "larger" : "smaller") +
                " than " + fileSystem.length(file));
        } else {
            logger.info("Not overwriting because " +
                yrl.getContentLength() + " is not " +
                (config.isOverwriteIfLarger() ? "larger" : "smaller") +
                " than " + fileSystem.length(file));
            throw new FileAlreadyExistsException(file.toString());
        }
    }

    File createFile() throws MalformedURLException, FileSystemException {
        File file = openFile();
        File directory = openDirectory();

        logger.debug("file: '" + file + "'");
        logger.debug("directory: " + directory);

        if (!fileSystem.exists(directory)) {
            logger.debug("Attempting to make: " + directory);

            if (!fileSystem.mkdirs(directory)) {
                logger.warn("Couldn't create directory: " + directory);
                throw new FileSystemException(directory.toString());
            }
        } else {
            logger.debug("Directory: " + directory + " already exists");
        }

        // if newer than a particular file, regardless if exists or not
        checkIfNewerThan();

        if (fileSystem.exists(file)) {
            logger.debug("File exists");

            if (config.isOverwriteIfLarger() ||
                config.isOverwriteIfSmaller()) {
                checkIfLargerOrSmaller(file);
            }

            if (!config.isOverwrite()) {
                logger.warn("Not overwriting: " + file);
                throw new FileAlreadyExistsException(file.toString());
            }

            if (config.isIfModifiedSince()) {
                checkIfModifiedSince(file, yrl.getLastModified());
            }
        }

        logger.debug("Opened: " + file);
        return file;
    }
}
