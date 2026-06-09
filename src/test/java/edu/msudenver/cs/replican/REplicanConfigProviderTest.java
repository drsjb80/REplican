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
}
