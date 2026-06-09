package edu.msudenver.cs.replican;

enum LogLevels {OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL}

// Mutable class for JCLO initialization
class REplicanArgsMutable {
    String[] PathAccept;
    String[] PathReject;
    String[] PathSave;
    String[] PathRefuse;
    String[] PathExamine;
    String[] PathIgnore;

    String[] MIMEAccept;
    String[] MIMEReject;
    String[] MIMESave;
    String[] MIMERefuse;
    String[] MIMEExamine;
    String[] MIMEIgnore;

    boolean PrintAccept;
    boolean PrintReject;
    boolean PrintSave = true;
    boolean PrintSavePath;
    boolean PrintRefuse;
    boolean PrintExamine;
    boolean PrintIgnore;
    boolean PrintSkip;
    boolean PrintAll;
    boolean PrintRedirects;

    boolean PrintAdd;

    boolean SaveProgress;

    int[] StopOn;
    // int Tries = 1;

    LogLevels LogLevel = LogLevels.OFF;
    String Username;
    String Password;
    String[] NetscapeCookies;
    String[] PlistCookies;
    String[] FirefoxCookies;
    final String IndexName = "index.html";
    boolean IgnoreCookies;
    boolean Overwrite;
    boolean OverwriteIfLarger;
    boolean OverwriteIfSmaller;
    boolean FollowRedirects = true;
    boolean SetLastModified;
    boolean IfModifiedSince;
    final String CheckpointFile = "REplican.cp";
    int CheckpointEvery;
    int PauseBetween;
    int PauseAfterSave;
    int Threads = 1;
    String Directory;
    String IfNewerThan;
    String[] FilenameRewrite;
    String[] URLRewrite;
    String[] URLFixUp;
    String[] Interesting;
    String[] Header;

    boolean Version;
    boolean Help;

    String[] additional;
}

// Immutable record created from mutable args after initialization
record REplicanArgs(
    String[] pathAccept,
    String[] pathReject,
    String[] pathSave,
    String[] pathRefuse,
    String[] pathExamine,
    String[] pathIgnore,
    String[] mimeAccept,
    String[] mimeReject,
    String[] mimeSave,
    String[] mimeRefuse,
    String[] mimeExamine,
    String[] mimeIgnore,
    boolean printAccept,
    boolean printReject,
    boolean printSave,
    boolean printSavePath,
    boolean printRefuse,
    boolean printExamine,
    boolean printIgnore,
    boolean printSkip,
    boolean printAll,
    boolean printRedirects,
    boolean printAdd,
    boolean saveProgress,
    int[] stopOn,
    LogLevels logLevel,
    String username,
    String password,
    String[] netscapeCookies,
    String[] plistCookies,
    String[] firefoxCookies,
    String indexName,
    boolean ignoreCookies,
    boolean overwrite,
    boolean overwriteIfLarger,
    boolean overwriteIfSmaller,
    boolean followRedirects,
    boolean setLastModified,
    boolean ifModifiedSince,
    String checkpointFile,
    int checkpointEvery,
    int pauseBetween,
    int pauseAfterSave,
    int threads,
    String directory,
    String ifNewerThan,
    String[] filenameRewrite,
    String[] urlRewrite,
    String[] urlFixUp,
    String[] interesting,
    String[] header,
    boolean version,
    boolean help,
    String[] additional) {

    static REplicanArgs fromMutable(REplicanArgsMutable m) {
        return new REplicanArgs(
            m.PathAccept,
            m.PathReject,
            m.PathSave,
            m.PathRefuse,
            m.PathExamine,
            m.PathIgnore,
            m.MIMEAccept,
            m.MIMEReject,
            m.MIMESave,
            m.MIMERefuse,
            m.MIMEExamine,
            m.MIMEIgnore,
            m.PrintAccept,
            m.PrintReject,
            m.PrintSave,
            m.PrintSavePath,
            m.PrintRefuse,
            m.PrintExamine,
            m.PrintIgnore,
            m.PrintSkip,
            m.PrintAll,
            m.PrintRedirects,
            m.PrintAdd,
            m.SaveProgress,
            m.StopOn,
            m.LogLevel,
            m.Username,
            m.Password,
            m.NetscapeCookies,
            m.PlistCookies,
            m.FirefoxCookies,
            m.IndexName,
            m.IgnoreCookies,
            m.Overwrite,
            m.OverwriteIfLarger,
            m.OverwriteIfSmaller,
            m.FollowRedirects,
            m.SetLastModified,
            m.IfModifiedSince,
            m.CheckpointFile,
            m.CheckpointEvery,
            m.PauseBetween,
            m.PauseAfterSave,
            m.Threads,
            m.Directory,
            m.IfNewerThan,
            m.FilenameRewrite,
            m.URLRewrite,
            m.URLFixUp,
            m.Interesting,
            m.Header,
            m.Version,
            m.Help,
            m.additional);
    }

    static REplicanArgs createDefault() {
        return new REplicanArgs(
            null, null, null, null, null, null,
            null, null, null, null, null, null,
            false, false, true, false, false, false, false, false,
            false, false, false, false,
            null,
            LogLevels.OFF,
            null, null,
            null, null, null,
            "index.html",
            false, false, false, false,
            true,
            false, false,
            "REplican.cp",
            0, 0, 0, 1,
            null, null,
            null, null, null,
            null, null,
            false, false,
            null);
    }
}
