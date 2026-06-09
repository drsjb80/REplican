package edu.msudenver.cs.replican;

import java.io.File;

class LocalFileSystem implements FileSystem {
    @Override
    public boolean exists(File file) {
        return file.exists();
    }

    @Override
    public long lastModified(File file) {
        return file.lastModified();
    }

    @Override
    public long length(File file) {
        return file.length();
    }

    @Override
    public boolean mkdirs(File directory) {
        return directory.mkdirs();
    }

    @Override
    public String getFileSeparator() {
        return System.getProperty("file.separator");
    }

    @Override
    public String getUserHome() {
        return System.getProperty("user.home");
    }
}
