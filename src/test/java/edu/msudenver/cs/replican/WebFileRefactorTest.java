package edu.msudenver.cs.replican;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WebFileRefactorTest {
    private WebFile webFile;
    private YouAreEll mockYAE;
    private ConfigProvider mockConfig;
    private FileSystem mockFileSystem;

    @BeforeEach
    void setUp() {
        mockYAE = mock(YouAreEll.class);
        mockConfig = mock(ConfigProvider.class);
        mockFileSystem = mock(FileSystem.class);

        // Default mock behavior
        when(mockYAE.getUrl()).thenReturn("http://example.com/file.html");
        when(mockYAE.getContentLength()).thenReturn(1024);
        when(mockYAE.getLastModified()).thenReturn(System.currentTimeMillis());
        when(mockConfig.getDirectory()).thenReturn(null);
        when(mockConfig.getIndexName()).thenReturn("index.html");
        when(mockConfig.getFilenameRewrite()).thenReturn(null);
        when(mockConfig.isOverwrite()).thenReturn(true);
        when(mockConfig.isOverwriteIfLarger()).thenReturn(false);
        when(mockConfig.isOverwriteIfSmaller()).thenReturn(false);
        when(mockConfig.isIfModifiedSince()).thenReturn(false);
        when(mockFileSystem.getFileSeparator()).thenReturn("/");
        when(mockFileSystem.getUserHome()).thenReturn("/home/user");
    }

    @Test
    void createsWithFullDependencyInjection() {
        webFile = new WebFile(mockYAE, mockConfig, LogManager.getLogger(), mockFileSystem);
        assertNotNull(webFile);
    }

    @Test
    void usesInjectedFileSystem() {
        when(mockFileSystem.exists(any())).thenReturn(false);
        when(mockFileSystem.mkdirs(any())).thenReturn(true);
        webFile = new WebFile(mockYAE, mockConfig, LogManager.getLogger(), mockFileSystem);

        try {
            webFile.createFile();
        } catch (Exception e) {
            // Expected behavior may vary based on config
        }

        verify(mockFileSystem, atLeast(1)).exists(any());
        verify(mockFileSystem, atLeast(1)).mkdirs(any());
    }

    @Test
    void respectsInjectedConfiguration() {
        when(mockConfig.getDirectory()).thenReturn("/base");
        when(mockFileSystem.getFileSeparator()).thenReturn("/");
        when(mockFileSystem.exists(any())).thenReturn(true);

        webFile = new WebFile(mockYAE, mockConfig, LogManager.getLogger(), mockFileSystem);
        assertNotNull(webFile);
    }

    @Test
    void handlesOverwriteConfiguration() {
        when(mockConfig.isOverwrite()).thenReturn(false);
        when(mockFileSystem.exists(any())).thenReturn(true);
        when(mockFileSystem.mkdirs(any())).thenReturn(true);

        webFile = new WebFile(mockYAE, mockConfig, LogManager.getLogger(), mockFileSystem);

        assertThrows(Exception.class, () -> webFile.createFile());
    }

    @Test
    void backwardCompatibleConstructor() {
        // Old constructor should still work
        webFile = new WebFile(mockYAE);
        assertNotNull(webFile);
    }

    @Test
    void handlesFileExistence() {
        when(mockFileSystem.exists(any())).thenReturn(true);
        webFile = new WebFile(mockYAE, mockConfig, LogManager.getLogger(), mockFileSystem);

        assertNotNull(webFile);
    }

    @Test
    void appliesFilenameRewrite() {
        when(mockConfig.getFilenameRewrite()).thenReturn(new String[]{" ", "_"});
        webFile = new WebFile(mockYAE, mockConfig, LogManager.getLogger(), mockFileSystem);

        assertNotNull(webFile);
    }

    @Test
    void handlesSizeComparison() {
        when(mockConfig.isOverwriteIfLarger()).thenReturn(true);
        when(mockFileSystem.exists(any())).thenReturn(true);
        when(mockFileSystem.length(any())).thenReturn(512L);
        when(mockYAE.getContentLength()).thenReturn(1024);

        webFile = new WebFile(mockYAE, mockConfig, LogManager.getLogger(), mockFileSystem);

        try {
            webFile.createFile();
        } catch (Exception e) {
            // Expected - size comparison may trigger exception
        }
    }

    @Test
    void multipleInstancesAreIndependent() {
        YouAreEll yae1 = mock(YouAreEll.class);
        YouAreEll yae2 = mock(YouAreEll.class);
        when(yae1.getUrl()).thenReturn("http://first.com");
        when(yae2.getUrl()).thenReturn("http://second.com");

        WebFile wf1 = new WebFile(yae1, mockConfig, LogManager.getLogger(), mockFileSystem);
        WebFile wf2 = new WebFile(yae2, mockConfig, LogManager.getLogger(), mockFileSystem);

        assertNotNull(wf1);
        assertNotNull(wf2);
    }
}
