package edu.msudenver.cs.replican;

import java.net.URL;

import java.io.File;

import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;

import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class WebFile {
    private final REplicanArgs args;
    private final YouAreEll yrl;
    private final Logger logger = LogManager.getLogger(getClass());

    WebFile(final YouAreEll yrl, final REplicanArgs args) {
        this.yrl = yrl;
        this.args = args;
    }

    private boolean dealWithExistingFile(final File file, final long LastModified) {
        logger.traceEntry(String.valueOf(LastModified));

        if (!args.Overwrite) {
            logger.warn("Not overwriting: " + file);
            return false;
        }

        if (args.IfModifiedSince) {
            if (LastModified > 0) {
                logger.trace("file: " + file.lastModified());
                logger.trace("URL: " + LastModified);

                if (file.lastModified() <= LastModified) {
                    if (args.PrintSkip)
                        logger.info("Not modified: " + file);
                    return (false);
                }
            } else {
                logger.info("No last-modified information: " + file);
            }
        }

        return (true);
    }

    private String getFilePath(@NonNull final String s) throws MalformedURLException {
        logger.traceEntry(s);

        URL url = new URL(s);

        String hostname = url.getHost();
        String filename = url.getFile();

        if (filename.endsWith("/")) {
            filename += args.IndexName;
        }

        if (hostname == null) {
            hostname = "localhost";
        }

        logger.debug(hostname);
        logger.debug(filename);

        String path = hostname + filename;

        String dir = args.Directory;
        if (dir != null) {
            String separator = System.getProperty("file.separator");
            if (!dir.endsWith(separator))
                dir += separator;

            path = dir + path;
        }

        logger.traceExit(path);
        return (path);
    }

    private File openFile() throws MalformedURLException {
        String path = getFilePath(yrl.getUrl());

        path = Utils.replaceAll(path,args.FilenameRewrite);
        path = path.replaceFirst("^~",System.getProperty("user.home"));

        if(args.PrintSavePath) {
            logger.info("Saving to: " + path);
        }

        return new File(path);
    }

    private File openDirectory() throws MalformedURLException {
        String path = getFilePath(yrl.getUrl());

        String directoryPath;
        if (path.indexOf('/') == -1) {
            directoryPath = "./";
        } else {
            directoryPath = path.replaceAll("/[^/]*$", "");
        }

        return new File(directoryPath);
    }

    // return true if we don't need to reread
    private boolean checkIfNewerThan() {
        if (args.IfNewerThan != null) {
            File newerThan = new File(args.IfNewerThan);
            logger.debug("file time = " + newerThan.lastModified());
            logger.debug("url time = " + yrl.getLastModified());
            if (yrl.getLastModified() < newerThan.lastModified()) {
                if (args.PrintSkip) {
                    logger.info("Skipping becauser older than " + args.IfNewerThan);
                }
                return (true);
            }
        }
        return (false);
    }

    // return true if we don't need to reread
    private boolean checkExistingFile(final File file) {
        if (args.OverwriteIfLarger || args.OverwriteIfSmaller) {
            boolean larger = yrl.getContentLength() > file.length();
            boolean smaller = yrl.getContentLength() < file.length();

            if ((args.OverwriteIfLarger && larger) || (args.OverwriteIfSmaller && smaller)) {
                logger.info("Overwriting because " +
                        yrl.getContentLength() + " is " +
                        (args.OverwriteIfLarger ? "larger" : "smaller") +
                        " than " + file.length());
                return (false);
            } else {
                logger.info("Not overwriting because " +
                        yrl.getContentLength() + " is not " +
                        (args.OverwriteIfLarger ? "larger" : "smaller") +
                        " than " + file.length());
                return (true);
            }
        } else if (!dealWithExistingFile(file, yrl.getLastModified())) {
            return (true);
        }

        return (false);
    }

    File createFile() throws MalformedURLException {
        File file = openFile();
        File directory = openDirectory();

        logger.debug("file: '" + file + "'");
        logger.debug("directory: " + directory);

        if (!directory.exists()) {
            logger.debug("Attempting to make: " + directory);

            if (!directory.mkdirs()) {
                logger.warn("Couldn't create directory: " + directory);
                return null;
            }
        } else {
            logger.debug("Directory: " + directory + " already exists");
        }

        if (checkIfNewerThan()) {
            return null;
        }

        if (file.exists()) {
            logger.debug("File exists");
            if (checkExistingFile(file)) {
                return null;
            }
        }

        logger.debug("Opened: " + file);
        return file;
    }
}
