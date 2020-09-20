package edu.msudenver.cs.replican;

enum LogLevels {OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL}

class REplicanArgs {
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
    String SaveCookies;
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
