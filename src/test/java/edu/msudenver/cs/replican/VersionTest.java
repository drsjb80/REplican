package edu.msudenver.cs.replican;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VersionTest {
    @Test
    void getVersionReturnsString() {
        String version = Version.getVersion();
        assertNotNull(version);
    }

    @Test
    void getVersionReturnsSemanticVersion() {
        String version = Version.getVersion();
        String[] parts = version.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    void getVersionIs200() {
        assertEquals("2.0.0", Version.getVersion());
    }

    @Test
    void getVersionReturnsConsistentValue() {
        String v1 = Version.getVersion();
        String v2 = Version.getVersion();
        assertEquals(v1, v2);
    }
}
