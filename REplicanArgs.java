class REplicanArgs
{
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
    int Tries = 1;

    String LogLevel;
    String UserAgent;
    String Username;
    String Password;
    String Referer;
    String LoadCookies[];
    String PlistCookies[];
    String SaveCookies;
    String IndexName = "index.html";
    boolean IgnoreCookies;
    boolean Overwrite;
    boolean OverwriteIfLarger;
    boolean OverwriteIfSmaller;
    boolean FollowRedirects = true;
    boolean SetLastModified;
    boolean IfModifiedSince;
    String CheckpointFile = "REplican.cp";
    int CheckpointEvery;
    int PauseBetween;
    int PauseAfterSave;
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
