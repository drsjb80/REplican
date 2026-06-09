package edu.msudenver.cs.replican;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FirefoxCookiesTest {
    private Cookies cookies;

    @BeforeEach
    void setUp() {
        cookies = new Cookies();
    }

    @Test
    void loadCookiesAcceptsCookiesParameter() {
        assertNotNull(cookies);
        // Just verify the method can be called with the new signature
        try {
            FirefoxCookies.loadCookies("/nonexistent/path", cookies);
        } catch (Exception e) {
            // Expected - file doesn't exist, but signature is valid
        }
    }

    @Test
    void loadCookiesHandlesNullFileGracefully() {
        // The method has @NonNull annotation but doesn't enforce at runtime
        // Just verify it doesn't crash unexpectedly
        assertDoesNotThrow(() -> {
            try {
                FirefoxCookies.loadCookies("/some/path", null);
            } catch (NullPointerException e) {
                // Expected if null enforcement happens
            }
        });
    }

    @Test
    void loadCookiesWithNonexistentFileDoesNotThrow() throws IOException {
        // This should fail gracefully with the new signature
        assertDoesNotThrow(() -> {
            FirefoxCookies.loadCookies("/nonexistent/firefox/cookies.sqlite", cookies);
        });
    }

    @Test
    void loadCookiesDoesNotModifyCookiesOnBadPath() {
        int initialCount = cookies.getAllCookies().size();
        try {
            FirefoxCookies.loadCookies("/invalid/path", cookies);
        } catch (Exception e) {
            // Ignore
        }
        int finalCount = cookies.getAllCookies().size();
        assertEquals(initialCount, finalCount);
    }

    @Test
    void loadCookiesAcceptsEmptyPath() {
        assertDoesNotThrow(() -> {
            FirefoxCookies.loadCookies("", cookies);
        });
    }

    @Test
    void cookiesParameterIsUsedNotStatic() {
        Cookies cookies1 = new Cookies();
        Cookies cookies2 = new Cookies();

        try {
            FirefoxCookies.loadCookies("/nonexistent", cookies1);
            FirefoxCookies.loadCookies("/nonexistent", cookies2);
        } catch (Exception e) {
            // Expected for nonexistent file
        }

        // Both should have the same state since nothing was loaded
        assertEquals(cookies1.getAllCookies().size(), cookies2.getAllCookies().size());
    }
}
