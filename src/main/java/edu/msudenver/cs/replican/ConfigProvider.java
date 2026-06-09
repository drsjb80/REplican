package edu.msudenver.cs.replican;

public interface ConfigProvider {
    String[] getPathAccept();

    String[] getPathReject();

    String[] getPathExamine();

    String[] getPathIgnore();

    String[] getPathSave();

    String[] getPathRefuse();

    String[] getMIMEAccept();

    String[] getMIMEReject();

    String[] getMIMEExamine();

    String[] getMIMEIgnore();

    String[] getMIMESave();

    String[] getMIMERefuse();

    String[] getInteresting();

    String[] getURLFixUp();

    String[] getURLRewrite();

    String[] getFilenameRewrite();

    boolean isOverwrite();

    boolean isSetLastModified();

    boolean isIfModifiedSince();

    boolean isPrintAccept();

    boolean isPrintReject();

    boolean isPrintExamine();

    boolean isPrintIgnore();

    boolean isPrintSave();

    boolean isPrintRefuse();

    boolean isPrintAdd();

    boolean isPrintAll();

    boolean isIgnoreCookies();

    boolean isFollowRedirects();

    boolean isSaveProgress();

    int getPauseBetween();

    int getPauseAfterSave();

    int getCheckpointEvery();

    String getCheckpointFile();

    String getIndexName();

    String getDirectory();

    String getUsername();

    String getPassword();

    String[] getAdditionalURLs();

    String[] getNetscapeCookieFiles();

    String[] getPlistCookieFiles();

    String[] getFirefoxCookieFiles();

    int[] getStopOnStatusCodes();

    LogLevels getLogLevel();

    String[] getHeader();

    boolean isPrintRedirects();
}
