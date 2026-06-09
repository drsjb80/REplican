package edu.msudenver.cs.replican;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class REplicanConfigProviderTest {
    private ConfigProvider provider;
    private REplicanArgs args;

    @BeforeEach
    void setUp() {
        args = new REplicanArgs();
        provider = new REplicanConfigProvider(args);
    }

    @Test
    void providesPathAccept() {
        args.PathAccept = new String[]{".*\\.html"};
        assertArrayEquals(new String[]{".*\\.html"}, provider.getPathAccept());
    }

    @Test
    void providesPathReject() {
        args.PathReject = new String[]{".*\\.pdf"};
        assertArrayEquals(new String[]{".*\\.pdf"}, provider.getPathReject());
    }

    @Test
    void providesBooleanFlags() {
        args.Overwrite = true;
        args.SaveProgress = false;
        assertTrue(provider.isOverwrite());
        assertFalse(provider.isSaveProgress());
    }

    @Test
    void providesIntegerValues() {
        args.PauseBetween = 100;
        args.CheckpointEvery = 50;
        assertEquals(100, provider.getPauseBetween());
        assertEquals(50, provider.getCheckpointEvery());
    }

    @Test
    void providesStringValues() {
        args.Directory = "/tmp";
        assertEquals("/tmp", provider.getDirectory());
        assertEquals("index.html", provider.getIndexName());
    }

    @Test
    void providesAdditionalURLs() {
        args.additional = new String[]{"http://example.com", "http://example2.com"};
        assertArrayEquals(new String[]{"http://example.com", "http://example2.com"},
            provider.getAdditionalURLs());
    }

    @Test
    void providesAllPathPatterns() {
        args.PathExamine = new String[]{".*\\.html"};
        args.PathIgnore = new String[]{".*\\.exe"};
        args.PathSave = new String[]{".*"};
        args.PathRefuse = new String[]{".*\\.tmp"};

        assertArrayEquals(new String[]{".*\\.html"}, provider.getPathExamine());
        assertArrayEquals(new String[]{".*\\.exe"}, provider.getPathIgnore());
        assertArrayEquals(new String[]{".*"}, provider.getPathSave());
        assertArrayEquals(new String[]{".*\\.tmp"}, provider.getPathRefuse());
    }

    @Test
    void providesAllMIMEPatterns() {
        args.MIMEAccept = new String[]{"text/*"};
        args.MIMEReject = new String[]{"image/*"};
        args.MIMEExamine = new String[]{"text/html"};
        args.MIMEIgnore = new String[]{"application/*"};
        args.MIMESave = new String[]{"text/*"};
        args.MIMERefuse = new String[]{"audio/*"};

        assertArrayEquals(new String[]{"text/*"}, provider.getMIMEAccept());
        assertArrayEquals(new String[]{"image/*"}, provider.getMIMEReject());
        assertArrayEquals(new String[]{"text/html"}, provider.getMIMEExamine());
        assertArrayEquals(new String[]{"application/*"}, provider.getMIMEIgnore());
        assertArrayEquals(new String[]{"text/*"}, provider.getMIMESave());
        assertArrayEquals(new String[]{"audio/*"}, provider.getMIMERefuse());
    }

    @Test
    void providesInterestingPatterns() {
        args.Interesting = new String[]{"href", "src"};
        assertArrayEquals(new String[]{"href", "src"}, provider.getInteresting());
    }

    @Test
    void providesURLRewritePatterns() {
        args.URLFixUp = new String[]{"\\s+", " "};
        args.URLRewrite = new String[]{"old", "new"};

        assertArrayEquals(new String[]{"\\s+", " "}, provider.getURLFixUp());
        assertArrayEquals(new String[]{"old", "new"}, provider.getURLRewrite());
    }

    @Test
    void providesFilenameRewrite() {
        args.FilenameRewrite = new String[]{"spaces", "-"};
        assertArrayEquals(new String[]{"spaces", "-"}, provider.getFilenameRewrite());
    }

    @Test
    void providesAllBooleanFlags() {
        args.SetLastModified = true;
        args.IfModifiedSince = false;
        args.PrintAccept = true;
        args.PrintReject = false;
        args.PrintExamine = true;
        args.PrintIgnore = false;
        args.PrintSave = true;
        args.PrintRefuse = false;
        args.PrintAdd = true;
        args.PrintAll = false;
        args.IgnoreCookies = true;
        args.FollowRedirects = false;

        assertTrue(provider.isSetLastModified());
        assertFalse(provider.isIfModifiedSince());
        assertTrue(provider.isPrintAccept());
        assertFalse(provider.isPrintReject());
        assertTrue(provider.isPrintExamine());
        assertFalse(provider.isPrintIgnore());
        assertTrue(provider.isPrintSave());
        assertFalse(provider.isPrintRefuse());
        assertTrue(provider.isPrintAdd());
        assertFalse(provider.isPrintAll());
        assertTrue(provider.isIgnoreCookies());
        assertFalse(provider.isFollowRedirects());
    }

    @Test
    void providesAllIntegerValues() {
        args.PauseAfterSave = 200;
        args.CheckpointEvery = 500;

        assertEquals(200, provider.getPauseAfterSave());
        assertEquals(500, provider.getCheckpointEvery());
    }

    @Test
    void providesAllStringValues() {
        args.Directory = "/home/user";
        args.Username = "admin";
        args.Password = "secret";

        assertEquals("REplican.cp", provider.getCheckpointFile());
        assertEquals("index.html", provider.getIndexName());
        assertEquals("/home/user", provider.getDirectory());
        assertEquals("admin", provider.getUsername());
        assertEquals("secret", provider.getPassword());
    }

    @Test
    void providesCookieFiles() {
        args.NetscapeCookies = new String[]{"cookies.txt"};
        args.PlistCookies = new String[]{"cookies.plist"};
        args.FirefoxCookies = new String[]{"cookies.sqlite"};

        assertArrayEquals(new String[]{"cookies.txt"}, provider.getNetscapeCookieFiles());
        assertArrayEquals(new String[]{"cookies.plist"}, provider.getPlistCookieFiles());
        assertArrayEquals(new String[]{"cookies.sqlite"}, provider.getFirefoxCookieFiles());
    }

    @Test
    void providesStopOnStatusCodes() {
        args.StopOn = new int[]{401, 403, 404};
        assertArrayEquals(new int[]{401, 403, 404}, provider.getStopOnStatusCodes());
    }

    @Test
    void providesLogLevel() {
        args.LogLevel = LogLevels.DEBUG;
        assertEquals(LogLevels.DEBUG, provider.getLogLevel());
    }

    @Test
    void returnsNullValuesWhenNotSet() {
        assertNull(provider.getPathAccept());
        assertNull(provider.getPathExamine());
        assertNull(provider.getMIMEExamine());
        assertNull(provider.getInteresting());
        assertNull(provider.getUsername());
        assertNull(provider.getPlistCookieFiles());
    }

    @Test
    void returnsDefaultLogLevel() {
        assertEquals(LogLevels.OFF, provider.getLogLevel());
    }
}
