package edu.msudenver.cs.replican;

enum LogLevels {OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL}

class REplicanArgs
{
    /*
    The Arrays in this file will not be thread safe
    Is it safe since each thread will have its own task?
     */
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
    boolean StopOnnull;
    final int Tries = 1;

    LogLevels logLevel = LogLevels.OFF;
    String UserAgent;
    String Username;
    String Password;
    String Referer;
    String LoadCookies[];
    String PlistCookies[];
    String FirefoxCookies[];
    String SaveCookies;
    final String IndexName = "index.html";
    boolean IgnoreCookies;
    boolean Overwrite;
    boolean OverwriteIfLarger;
    boolean OverwriteIfSmaller;
    final boolean FollowRedirects = true;
    boolean SetLastModified;
    boolean IfModifiedSince;
    final String CheckpointFile = "REplican.cp";
    int CheckpointEvery;
    int PauseBetween;
    int PauseAfterSave;
    int Threads = 1;
    String Directory;
    String IfNewerThan;
    String FilenameRewrite[];
    String URLRewrite[];
    String URLFixUp[];
    String Interesting[];
    String Header[];

    boolean Version;
    boolean Help;

    String[] additional;
}
