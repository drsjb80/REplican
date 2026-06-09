package edu.msudenver.cs.replican;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystemException;

public interface FileSaver {
    File prepare(YouAreEll metadata) throws FileSystemException;

    void save(File file, InputStream content) throws IOException;

    void setLastModified(File file, long timestamp) throws IOException;

    String getFilePath(String url) throws Exception;
}
