package edu.msudenver.cs.replican;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.*;

public class WebFileSaverTest {
    private FileSaver saver;

    @BeforeEach
    void setUp() {
        saver = new WebFileSaver("/tmp", "index.html");
    }

    @Test
    void getFilePathWithNormalURL() throws Exception {
        String path = saver.getFilePath("http://example.com/path/to/file.html");
        assertTrue(path.contains("example.com"));
        assertTrue(path.contains("path"));
        assertTrue(path.contains("file.html"));
    }

    @Test
    void getFilePathWithRootURL() throws Exception {
        String path = saver.getFilePath("http://example.com/");
        assertTrue(path.contains("example.com"));
        assertTrue(path.endsWith("index.html"));
    }

    @Test
    void getFilePathWithoutTrailingSlash() throws Exception {
        String path = saver.getFilePath("http://example.com");
        assertTrue(path.contains("example.com"));
        assertTrue(path.endsWith("index.html"));
    }

    @Test
    void getFilePathWithBaseDirectory() throws Exception {
        FileSaver saverWithBase = new WebFileSaver("/custom/base", "index.html");
        String path = saverWithBase.getFilePath("http://example.com/file");
        assertTrue(path.contains("/custom/base"));
    }

    @Test
    void getFilePathWithNullDirectory() throws Exception {
        FileSaver saverNullDir = new WebFileSaver(null, "index.html");
        String path = saverNullDir.getFilePath("http://example.com/file");
        assertNotNull(path);
    }

    @Test
    void getFilePathWithCustomIndexName() throws Exception {
        FileSaver saverCustomIndex = new WebFileSaver("/tmp", "default.html");
        String path = saverCustomIndex.getFilePath("http://example.com/");
        assertTrue(path.endsWith("default.html"));
    }

    @Test
    void saverImplementsFileSaverInterface() {
        assertTrue(saver instanceof FileSaver);
    }
}
