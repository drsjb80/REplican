package edu.msudenver.cs.replican;

import java.net.URL;

import java.io.File;

import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;

import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class WebFile {
    private final YouAreEll yrl;
    private final Logger logger = REplican.logger;

    WebFile(final YouAreEll yrl) {
        this.yrl = yrl;
    }

    private String getFilePath(@NonNull final String s) throws MalformedURLException {
        logger.traceEntry(s);

        URL url = new URL(s);

        String hostname = url.getHost();
        String filename = url.getFile();

        if (filename.endsWith("/")) {
            filename += REplican.args.IndexName;
        }

        if (hostname == null) {
            hostname = "localhost";
        }

        logger.debug(hostname);
        logger.debug(filename);

        String path = hostname + filename;

        String dir = REplican.args.Directory;
        if (dir != null) {
            String separator = System.getProperty("file.separator");
            if (!dir.endsWith(separator))
                dir += separator;

            path = dir + path;
        }

        path = Utils.replaceAll(path, REplican.args.FilenameRewrite);
        path = path.replaceFirst("^~",System.getProperty("user.home") + "/");

        logger.traceExit(path);
        return (path);
    }

    private File openFile() throws MalformedURLException {
        String path = getFilePath(yrl.getUrl());

        if(REplican.args.PrintSavePath) {
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
        if (REplican.args.IfNewerThan != null) {
            File newerThan = new File(REplican.args.IfNewerThan);
            logger.debug("file time = " + newerThan.lastModified());
            logger.debug("url time = " + yrl.getLastModified());
            if (yrl.getLastModified() < newerThan.lastModified()) {
                if (REplican.args.PrintSkip) {
                    logger.info("Skipping because older than " + REplican.args.IfNewerThan);
                }
                throw new FileAlreadyExistsException(newerThan.toString());
            }
        }
    }

    private void checkIfModifiedSince(final File file, final long LastModified) throws FileAlreadyExistsException {
        logger.traceEntry(String.valueOf(LastModified));

        if (REplican.args.IfModifiedSince) {
            if (LastModified > 0) {
                logger.trace("file: " + file.lastModified());
                logger.trace("URL: " + LastModified);

                if (file.lastModified() <= LastModified) {
                    if (REplican.args.PrintSkip) {
                        logger.info("Not modified: " + file);
                    }
                    throw new FileAlreadyExistsException(file.toString());
                }
            } else {
                logger.info("No last-modified information: " + file);
            }
        }
    }

    private void checkIfLargerOrSmaller(final File file) throws FileAlreadyExistsException {
        if (REplican.args.OverwriteIfLarger || REplican.args.OverwriteIfSmaller) {
            boolean larger = yrl.getContentLength() > file.length();
            boolean smaller = yrl.getContentLength() < file.length();

            if ((REplican.args.OverwriteIfLarger && larger) || (REplican.args.OverwriteIfSmaller && smaller)) {
                logger.info("Overwriting because " +
                        yrl.getContentLength() + " is " +
                        (REplican.args.OverwriteIfLarger ? "larger" : "smaller") +
                        " than " + file.length());
            } else {
                logger.info("Not overwriting because " +
                        yrl.getContentLength() + " is not " +
                        (REplican.args.OverwriteIfLarger ? "larger" : "smaller") +
                        " than " + file.length());
                throw new FileAlreadyExistsException(file.toString());
            }
        }
    }

    File createFile() throws MalformedURLException, FileSystemException {
        File file = openFile();
        File directory = openDirectory();

        logger.debug("file: '" + file + "'");
        logger.debug("directory: " + directory);

        if (!directory.exists()) {
            logger.debug("Attempting to make: " + directory);

            if (!directory.mkdirs()) {
                logger.warn("Couldn't create directory: " + directory);
                throw new FileSystemException(directory.toString());
            }
        } else {
            logger.debug("Directory: " + directory + " already exists");
        }

        // if newer than a particular file, regardless if exists or not
        checkIfNewerThan();

        if (file.exists()) {
            // if one of the three overwrites...
            logger.debug("File exists");
            checkIfLargerOrSmaller(file);

            if (!REplican.args.Overwrite) {
                logger.warn("Not overwriting: " + file);
                throw new FileAlreadyExistsException(file.toString());
            }

            checkIfModifiedSince(file, yrl.getLastModified());
        }

        logger.debug("Opened: " + file);
        return file;
    }
}
