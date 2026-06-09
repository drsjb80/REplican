package edu.msudenver.cs.replican;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.mock;

public class WebFileTest {
    private YouAreEll yrl = new YouAreEll("http://localhost:3000");
    private WebFile webFile = new WebFile(yrl);

    @Test
    public void createFile() throws Throwable {
        File file = (File) P38.call("openFile", webFile);
        assertNotNull(file);
        assertTrue(file.getName().startsWith("localhost") || file.getName().contains("3000"));
    }

    @Test
    public void getFilePath() throws Throwable {
        assertEquals ("localhost", P38.call("getFilePath", webFile, new Object[]{"http://localhost:3000"}));
        assertEquals ("localhost", P38.call("getFilePath", webFile, new Object[]{"http://localhost"}));
        assertEquals ("localhost/foo", P38.call("getFilePath", webFile, new Object[]{"http://localhost/foo"}));
        assertEquals ("localhost/" + REplican.ARGS.IndexName, P38.call("getFilePath", webFile, new Object[]{"http://localhost/"}));
        REplican.ARGS.Directory = "/tmp";
        assertEquals ("/tmp/localhost", P38.call("getFilePath", webFile, new Object[]{"http://localhost"}));
    }

    @Test
    public void getFilePathBadURL() throws Throwable {
        assertThrows(MalformedURLException.class,
            () -> P38.call("getFilePath", webFile, new Object[]{"bad://localhost:3000"}));
    }

    @Test
    public void openFile() throws Throwable {
        File file = (File) P38.call("openFile", new WebFile(new YouAreEll("file:pom.xml")));
        assertEquals("pom.xml", file.toString());
        file = (File) P38.call("openFile", new WebFile(new YouAreEll("file:~pom.xml")));
        assertEquals(System.getProperty("user.home") + "/pom.xml", file.toString());
    }

    @Test
    public void webFileCanBeConstructed() {
        YouAreEll yrl = new YouAreEll("http://example.com");
        assertNotNull(new WebFile(yrl));
    }

    @Test
    public void webFileWithDifferentURLs() {
        YouAreEll yrl1 = new YouAreEll("http://example.com");
        YouAreEll yrl2 = new YouAreEll("https://other.com:8080/path");
        YouAreEll yrl3 = new YouAreEll("http://localhost:3000");

        assertNotNull(new WebFile(yrl1));
        assertNotNull(new WebFile(yrl2));
        assertNotNull(new WebFile(yrl3));
    }

    @Test
    public void webFileThrowsOnBadURL() throws Throwable {
        YouAreEll yrl = new YouAreEll("not a valid url");
        WebFile wf = new WebFile(yrl);
        assertThrows(MalformedURLException.class, () -> P38.call("openFile", wf));
    }

    @Test
    public void webFileOpenFileWithValidURL() throws Throwable {
        YouAreEll yrl = new YouAreEll("http://example.com/path/to/file.html");
        WebFile wf = new WebFile(yrl);
        File file = (File) P38.call("openFile", wf);
        assertNotNull(file);
        assertTrue(file.getPath().contains("example.com"));
    }

    @Test
    public void webFileGetFilePathWithRootPath() throws Throwable {
        YouAreEll yrl = new YouAreEll("http://example.com/");
        WebFile wf = new WebFile(yrl);
        String path = (String) P38.call("getFilePath", wf, new Object[]{"http://example.com/"});
        assertTrue(path.contains("index.html"));
    }

    @Test
    public void webFileGetFilePathWithQueryString() throws Throwable {
        YouAreEll yrl = new YouAreEll("http://example.com");
        WebFile wf = new WebFile(yrl);
        String path = (String) P38.call("getFilePath", wf, new Object[]{"http://example.com/page?id=123"});
        assertTrue(path.contains("example.com"));
    }

    @Test
    public void webFileGetFilePathWithFragment() throws Throwable {
        YouAreEll yrl = new YouAreEll("http://example.com");
        WebFile wf = new WebFile(yrl);
        String path = (String) P38.call("getFilePath", wf, new Object[]{"http://example.com/page#section"});
        assertTrue(path.contains("example.com"));
    }

    @Test
    public void webFileGetFilePathWithIPAddress() throws Throwable {
        YouAreEll yrl = new YouAreEll("http://192.168.1.1");
        WebFile wf = new WebFile(yrl);
        String path = (String) P38.call("getFilePath", wf, new Object[]{"http://192.168.1.1/data.html"});
        assertTrue(path.contains("192.168.1.1"));
    }

    @Test
    public void webFileGetFilePathWithPort() throws Throwable {
        YouAreEll yrl = new YouAreEll("http://example.com:8080");
        WebFile wf = new WebFile(yrl);
        String path = (String) P38.call("getFilePath", wf, new Object[]{"http://example.com:8080/file.html"});
        assertTrue(path.contains("example.com"));
    }

    @Test
    public void webFileGetFilePathPreservesDirectoryStructure() throws Throwable {
        YouAreEll yrl = new YouAreEll("http://example.com");
        WebFile wf = new WebFile(yrl);
        String path = (String) P38.call("getFilePath", wf, new Object[]{"http://example.com/deep/path/to/file.html"});
        assertTrue(path.contains("/deep/path/to/"));
    }

    @Test
    public void webFileGetFilePathWithNestedDirectories() throws Throwable {
        YouAreEll yrl = new YouAreEll("http://example.com");
        WebFile wf = new WebFile(yrl);
        String path = (String) P38.call("getFilePath", wf, new Object[]{"http://example.com/a/b/c/d/e/f/file.html"});
        assertTrue(path.contains("a/b/c/d/e/f"));
    }

    @Test
    public void webFileHandlesUrlsWithoutProtocol() throws Throwable {
        YouAreEll yrl = new YouAreEll("http://example.com");
        WebFile wf = new WebFile(yrl);
        assertThrows(Exception.class, () -> P38.call("getFilePath", wf, new Object[]{"example.com/file.html"}));
    }

    @Test
    public void webFileGetFilePathWithSpecialCharacterFilename() throws Throwable {
        YouAreEll yrl = new YouAreEll("http://example.com");
        WebFile wf = new WebFile(yrl);
        String path = (String) P38.call("getFilePath", wf, new Object[]{"http://example.com/file-with-dashes.html"});
        assertTrue(path.contains("file-with-dashes"));
    }

    @Test
    public void webFileOpenFileReturnsFileObject() throws Throwable {
        YouAreEll yrl = new YouAreEll("http://localhost/test.html");
        WebFile wf = new WebFile(yrl);
        File file = (File) P38.call("openFile", wf);
        assertNotNull(file);
        assertEquals(File.class, file.getClass());
    }
}