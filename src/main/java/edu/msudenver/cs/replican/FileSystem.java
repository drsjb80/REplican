package edu.msudenver.cs.replican;

import java.io.File;
import java.nio.file.FileSystemException;

interface FileSystem {
    boolean exists(File file);

    long lastModified(File file);

    long length(File file);

    boolean mkdirs(File directory);

    String getFileSeparator();

    String getUserHome();
}
