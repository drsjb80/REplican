package edu.msudenver.cs.replican;

import lombok.NonNull;

public class REplicanConfigProvider implements ConfigProvider {
    private final REplicanArgs args;

    public REplicanConfigProvider(@NonNull REplicanArgs args) {
        this.args = args;
    }

    @Override
    public String[] getPathAccept() {
        return args.PathAccept;
    }

    @Override
    public String[] getPathReject() {
        return args.PathReject;
    }

    @Override
    public String[] getPathExamine() {
        return args.PathExamine;
    }

    @Override
    public String[] getPathIgnore() {
        return args.PathIgnore;
    }

    @Override
    public String[] getPathSave() {
        return args.PathSave;
    }

    @Override
    public String[] getPathRefuse() {
        return args.PathRefuse;
    }

    @Override
    public String[] getMIMEAccept() {
        return args.MIMEAccept;
    }

    @Override
    public String[] getMIMEReject() {
        return args.MIMEReject;
    }

    @Override
    public String[] getMIMEExamine() {
        return args.MIMEExamine;
    }

    @Override
    public String[] getMIMEIgnore() {
        return args.MIMEIgnore;
    }

    @Override
    public String[] getMIMESave() {
        return args.MIMESave;
    }

    @Override
    public String[] getMIMERefuse() {
        return args.MIMERefuse;
    }

    @Override
    public String[] getInteresting() {
        return args.Interesting;
    }

    @Override
    public String[] getURLFixUp() {
        return args.URLFixUp;
    }

    @Override
    public String[] getURLRewrite() {
        return args.URLRewrite;
    }

    @Override
    public String[] getFilenameRewrite() {
        return args.FilenameRewrite;
    }

    @Override
    public boolean isOverwrite() {
        return args.Overwrite;
    }

    @Override
    public boolean isSetLastModified() {
        return args.SetLastModified;
    }

    @Override
    public boolean isIfModifiedSince() {
        return args.IfModifiedSince;
    }

    @Override
    public boolean isPrintAccept() {
        return args.PrintAccept;
    }

    @Override
    public boolean isPrintReject() {
        return args.PrintReject;
    }

    @Override
    public boolean isPrintExamine() {
        return args.PrintExamine;
    }

    @Override
    public boolean isPrintIgnore() {
        return args.PrintIgnore;
    }

    @Override
    public boolean isPrintSave() {
        return args.PrintSave;
    }

    @Override
    public boolean isPrintRefuse() {
        return args.PrintRefuse;
    }

    @Override
    public boolean isPrintAdd() {
        return args.PrintAdd;
    }

    @Override
    public boolean isPrintAll() {
        return args.PrintAll;
    }

    @Override
    public boolean isIgnoreCookies() {
        return args.IgnoreCookies;
    }

    @Override
    public boolean isFollowRedirects() {
        return args.FollowRedirects;
    }

    @Override
    public boolean isSaveProgress() {
        return args.SaveProgress;
    }

    @Override
    public int getPauseBetween() {
        return args.PauseBetween;
    }

    @Override
    public int getPauseAfterSave() {
        return args.PauseAfterSave;
    }

    @Override
    public int getCheckpointEvery() {
        return args.CheckpointEvery;
    }

    @Override
    public String getCheckpointFile() {
        return args.CheckpointFile;
    }

    @Override
    public String getIndexName() {
        return args.IndexName;
    }

    @Override
    public String getDirectory() {
        return args.Directory;
    }

    @Override
    public String getUsername() {
        return args.Username;
    }

    @Override
    public String getPassword() {
        return args.Password;
    }

    @Override
    public String[] getAdditionalURLs() {
        return args.additional;
    }

    @Override
    public String[] getNetscapeCookieFiles() {
        return args.NetscapeCookies;
    }

    @Override
    public String[] getPlistCookieFiles() {
        return args.PlistCookies;
    }

    @Override
    public String[] getFirefoxCookieFiles() {
        return args.FirefoxCookies;
    }

    @Override
    public int[] getStopOnStatusCodes() {
        return args.StopOn;
    }

    @Override
    public LogLevels getLogLevel() {
        return args.LogLevel;
    }

    @Override
    public String[] getHeader() {
        return args.Header;
    }

    @Override
    public boolean isPrintRedirects() {
        return args.PrintRedirects;
    }
}
