package edu.msudenver.cs.replican;

import lombok.NonNull;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystemException;

public class WebFileSaver implements FileSaver {
    private final String baseDirectory;
    private final String indexName;
    private final Logger logger = LogManager.getLogger(getClass());

    public WebFileSaver(String baseDirectory, String indexName) {
        this.baseDirectory = baseDirectory != null ? baseDirectory : ".";
        this.indexName = indexName != null ? indexName : "index.html";
    }

    @Override
    public File prepare(@NonNull YouAreEll metadata) throws FileSystemException {
        logger.traceEntry(metadata.getUrl());

        try {
            String filePath = getFilePath(metadata.getUrl());
            File file = new File(filePath);
            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new FileSystemException("Failed to create directory: " + parent.getAbsolutePath());
                }
            }

            logger.traceExit(file);
            return file;
        } catch (MalformedURLException e) {
            throw new FileSystemException("Invalid URL: " + e.getMessage());
        }
    }

    @Override
    public void save(@NonNull File file, @NonNull InputStream content) throws IOException {
        logger.traceEntry(file.getAbsolutePath());

        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = content.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        logger.traceExit();
    }

    @Override
    public void setLastModified(@NonNull File file, long timestamp) throws IOException {
        logger.traceEntry(file.getAbsolutePath() + ", " + timestamp);

        if (!file.setLastModified(timestamp)) {
            logger.warn("Failed to set last modified time for: " + file.getAbsolutePath());
        }

        logger.traceExit();
    }

    @Override
    public String getFilePath(@NonNull String url) throws MalformedURLException {
        logger.traceEntry(url);

        URL u = new URL(url);
        String hostname = u.getHost();
        String filepath = u.getFile();

        if (filepath == null || filepath.isEmpty() || filepath.equals("/")) {
            filepath = indexName;
        }

        String result = baseDirectory + File.separator + hostname + filepath;
        logger.traceExit(result);
        return result;
    }
}
