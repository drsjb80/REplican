package edu.msudenver.cs.replican;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class REplicanConfigProviderTest {
    private ConfigProvider provider;

    @BeforeEach
    void setUp() {
        REplicanArgs args = REplicanArgs.createDefault();
        provider = new REplicanConfigProvider(args);
    }

    @Test
    void providesDefaultValues() {
        assertNull(provider.getPathAccept());
        assertNull(provider.getPathReject());
        assertEquals("index.html", provider.getIndexName());
        assertEquals("REplican.cp", provider.getCheckpointFile());
    }

    @Test
    void providesBooleanFlags() {
        assertTrue(provider.isFollowRedirects());
        assertFalse(provider.isSaveProgress());
        assertFalse(provider.isOverwrite());
    }

    @Test
    void providesIntegerValues() {
        assertEquals(0, provider.getPauseBetween());
        assertEquals(0, provider.getCheckpointEvery());
    }

    @Test
    void providesStringValues() {
        assertNull(provider.getDirectory());
        assertEquals("index.html", provider.getIndexName());
    }

    @Test
    void providesAdditionalURLs() {
        assertNull(provider.getAdditionalURLs());
    }

    @Test
    void returnsDefaultLogLevel() {
        assertEquals(LogLevels.OFF, provider.getLogLevel());
    }
}
