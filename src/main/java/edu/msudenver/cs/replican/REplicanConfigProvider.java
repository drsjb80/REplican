package edu.msudenver.cs.replican;

import lombok.NonNull;

public class REplicanConfigProvider implements ConfigProvider {
    private final REplicanArgs args;

    public REplicanConfigProvider(@NonNull REplicanArgs args) {
        this.args = args;
    }

    @Override
    public String[] getPathAccept() {
        return args.pathAccept();
    }

    @Override
    public String[] getPathReject() {
        return args.pathReject();
    }

    @Override
    public String[] getPathExamine() {
        return args.pathExamine();
    }

    @Override
    public String[] getPathIgnore() {
        return args.pathIgnore();
    }

    @Override
    public String[] getPathSave() {
        return args.pathSave();
    }

    @Override
    public String[] getPathRefuse() {
        return args.pathRefuse();
    }

    @Override
    public String[] getMIMEAccept() {
        return args.mimeAccept();
    }

    @Override
    public String[] getMIMEReject() {
        return args.mimeReject();
    }

    @Override
    public String[] getMIMEExamine() {
        return args.mimeExamine();
    }

    @Override
    public String[] getMIMEIgnore() {
        return args.mimeIgnore();
    }

    @Override
    public String[] getMIMESave() {
        return args.mimeSave();
    }

    @Override
    public String[] getMIMERefuse() {
        return args.mimeRefuse();
    }

    @Override
    public String[] getInteresting() {
        return args.interesting();
    }

    @Override
    public String[] getURLFixUp() {
        return args.urlFixUp();
    }

    @Override
    public String[] getURLRewrite() {
        return args.urlRewrite();
    }

    @Override
    public String[] getFilenameRewrite() {
        return args.filenameRewrite();
    }

    @Override
    public boolean isOverwrite() {
        return args.overwrite();
    }

    @Override
    public boolean isSetLastModified() {
        return args.setLastModified();
    }

    @Override
    public boolean isIfModifiedSince() {
        return args.ifModifiedSince();
    }

    @Override
    public boolean isPrintAccept() {
        return args.printAccept();
    }

    @Override
    public boolean isPrintReject() {
        return args.printReject();
    }

    @Override
    public boolean isPrintExamine() {
        return args.printExamine();
    }

    @Override
    public boolean isPrintIgnore() {
        return args.printIgnore();
    }

    @Override
    public boolean isPrintSave() {
        return args.printSave();
    }

    @Override
    public boolean isPrintRefuse() {
        return args.printRefuse();
    }

    @Override
    public boolean isPrintAdd() {
        return args.printAdd();
    }

    @Override
    public boolean isPrintAll() {
        return args.printAll();
    }

    @Override
    public boolean isIgnoreCookies() {
        return args.ignoreCookies();
    }

    @Override
    public boolean isFollowRedirects() {
        return args.followRedirects();
    }

    @Override
    public boolean isSaveProgress() {
        return args.saveProgress();
    }

    @Override
    public int getPauseBetween() {
        return args.pauseBetween();
    }

    @Override
    public int getPauseAfterSave() {
        return args.pauseAfterSave();
    }

    @Override
    public int getCheckpointEvery() {
        return args.checkpointEvery();
    }

    @Override
    public String getCheckpointFile() {
        return args.checkpointFile();
    }

    @Override
    public String getIndexName() {
        return args.indexName();
    }

    @Override
    public String getDirectory() {
        return args.directory();
    }

    @Override
    public String getUsername() {
        return args.username();
    }

    @Override
    public String getPassword() {
        return args.password();
    }

    @Override
    public String[] getAdditionalURLs() {
        return args.additional();
    }

    @Override
    public String[] getNetscapeCookieFiles() {
        return args.netscapeCookies();
    }

    @Override
    public String[] getPlistCookieFiles() {
        return args.plistCookies();
    }

    @Override
    public String[] getFirefoxCookieFiles() {
        return args.firefoxCookies();
    }

    @Override
    public int[] getStopOnStatusCodes() {
        return args.stopOn();
    }

    @Override
    public LogLevels getLogLevel() {
        return args.logLevel();
    }

    @Override
    public String[] getHeader() {
        return args.header();
    }

    @Override
    public boolean isPrintRedirects() {
        return args.printRedirects();
    }

    @Override
    public String getIfNewerThan() {
        return args.ifNewerThan();
    }

    @Override
    public boolean isPrintSkip() {
        return args.printSkip();
    }

    @Override
    public boolean isPrintSavePath() {
        return args.printSavePath();
    }

    @Override
    public boolean isOverwriteIfLarger() {
        return args.overwriteIfLarger();
    }

    @Override
    public boolean isOverwriteIfSmaller() {
        return args.overwriteIfSmaller();
    }
}
