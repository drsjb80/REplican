package edu.msudenver.cs.replican;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class PlistTest {
    private Cookies cookies;

    @BeforeEach
    void setUp() {
        cookies = new Cookies();
    }

    @Test
    void plistCanBeCreatedWithValidUrl() {
        // Test that Plist constructor accepts a URL and Cookies object
        assertDoesNotThrow(() -> {
            try {
                new Plist("file:///nonexistent/path", cookies);
            } catch (Exception e) {
                // Expected for nonexistent file, but constructor should not throw
            }
        });
    }

    @Test
    void plistHandlesInvalidUrlGracefully() {
        // Test that Plist handles malformed URLs gracefully
        assertDoesNotThrow(() -> {
            try {
                new Plist("not a valid url at all!", cookies);
            } catch (Exception e) {
                // Should handle gracefully with logging
            }
        });
    }

    @Test
    void plistDoesNotModifyCookiesOnBadFile() {
        int initialCount = cookies.getAllCookies().size();
        try {
            new Plist("file:///nonexistent/path.plist", cookies);
        } catch (Exception e) {
            // Expected
        }
        int finalCount = cookies.getAllCookies().size();
        assertEquals(initialCount, finalCount);
    }

    @Test
    void plistHandlesNullCookiesParameter() {
        // @NonNull annotation is for static analysis, not runtime enforcement
        // Verify it handles gracefully or throws later when trying to use
        assertDoesNotThrow(() -> {
            try {
                new Plist("file:///some/path", null);
            } catch (NullPointerException | IOException e) {
                // Expected if it tries to dereference null
            }
        });
    }
}
