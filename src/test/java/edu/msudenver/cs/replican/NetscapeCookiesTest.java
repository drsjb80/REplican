package edu.msudenver.cs.replican;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;

public class NetscapeCookiesTest {

    private void callDoNetscapeLine(String line) throws Throwable {
        Method m = NetscapeCookies.class.getDeclaredMethod("doNetscapeLine", String.class);
        m.setAccessible(true);
        m.invoke(null, line);
    }

    @Test
    public void doNetscapeLineWithShortLineDoesNotThrow() throws Throwable {
        // Line with only 3 tab-separated fields (should not throw AIOOBE)
        String shortLine = "domain\tincludeSubdomains\tpath";
        callDoNetscapeLine(shortLine);
    }

    @Test
    public void doNetscapeLineWithValidLineDoesNotThrow() throws Throwable {
        // Properly formatted Netscape cookie line: domain, flag, path, secure, expiry, name, value
        String validLine = ".example.com\tTRUE\t/\tTRUE\t1234567890\tSID\t31d4d96e407aad42";
        callDoNetscapeLine(validLine);
    }

    @Test
    public void doNetscapeLineWithCommentDoesNotThrow() throws Throwable {
        // Comment line should be skipped
        String commentLine = "# This is a comment";
        callDoNetscapeLine(commentLine);
    }
}
