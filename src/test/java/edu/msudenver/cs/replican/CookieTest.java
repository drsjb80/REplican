package edu.msudenver.cs.replican;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

public class CookieTest {
    // if there aren't a path and domain in the cookie, use the URL host and path
    private final Cookie firstTestCookie = new Cookie("foo.example.com", "/bar.baz",
            "SID=31d4d96e407aad42");

    private final Cookie secondTestCookie = new Cookie("foo.example.com", "/",
            "SID=31d4d96e407aad42; Path=/; Domain=example.com");

    private final Cookie thirdTestCookie = new Cookie("foo.example.com", "/",
            "SID=31d4d96e407aad42; Path=/; Secure; HttpOnly");

    private final Cookie fourthTestCookie = new Cookie("foo.example.com", "/",
            "lang=en-US; Path=/; Domain=example.com");



    @Test
    public void testURL() throws MalformedURLException{
        final Cookie fifthTestCookie = new Cookie("foo.example.com", "",
                "SID=31d4d96e407aad42");
        assertEquals("", new URL("http://example.com").getPath());
        assertEquals("/", fifthTestCookie.getPath());
    }

    @Test(expected = NullPointerException.class)
    public void testNull() {
        new Cookie(null, null, null);
        new Cookie("foo.bar", null, null);

    }

    @Test
    public void getCookieString() {
        assertEquals("Cookie: SID=31d4d96e407aad42", firstTestCookie.getCookieString());
        assertEquals("Cookie: SID=31d4d96e407aad42", secondTestCookie.getCookieString());
    }

    @Test
    public void testToString() {
        // Verify toString() returns a string (Lombok @ToString generates the implementation)
        String str = firstTestCookie.toString();
        assertNotNull(str);
        assertTrue(str.length() > 0);
    }

    @Test
    public void maxAgeIsInSeconds() {
        Cookie cookieWithMaxAge = new Cookie("foo.example.com", "/", "SID=x; Max-Age=3600");
        long maxTime = cookieWithMaxAge.getMaxTime();
        long now = System.currentTimeMillis();
        long expectedMin = now + 3_500_000L; // 3500 seconds in millis, accounting for small delays
        long expectedMax = now + 3_700_000L; // 3700 seconds in millis, allowing some slop
        assertTrue("maxTime should be roughly 1 hour in the future",
            maxTime >= expectedMin && maxTime <= expectedMax);
    }

    @Test
    public void testAddCookieString() {
        thirdTestCookie.addCookieString("lang=en-US; Path=/; Domain=example.com");
        assertEquals("Cookie: lang=en-US; SID=31d4d96e407aad42", thirdTestCookie.getCookieString());
    }

    @Test
    public void getDomain() {
        assertEquals("foo.example.com", firstTestCookie.getDomain());
        assertEquals("example.com", secondTestCookie.getDomain());
        assertEquals("example.com", fourthTestCookie.getDomain());

        final Cookie leadingDotDomain = new Cookie("foo.example.com", "/",
                "lang=en-US; Path=/; Domain=.example.com");
        assertEquals("example.com", leadingDotDomain.getDomain());
    }

    @Test
    public void getPath() {
        assertEquals("/bar.baz", firstTestCookie.getPath());
        assertEquals("/", secondTestCookie.getPath());
        assertEquals("/", thirdTestCookie.getPath());
    }

    @Test
    public void getMaxAge() {
        assertEquals(0, firstTestCookie.getMaxTime());
        assertEquals(0, secondTestCookie.getMaxTime());
        assertEquals(0, thirdTestCookie.getMaxTime());
    }

    @Test
    public void isSecure() {
        assertEquals(false, firstTestCookie.isSecure());
        assertEquals(false, secondTestCookie.isSecure());
        assertEquals(true, thirdTestCookie.isSecure());
    }

    @Test
    public void isHttponly() {
        assertEquals(false, firstTestCookie.isHttponly());
        assertEquals(false, secondTestCookie.isHttponly());
        assertEquals(true, thirdTestCookie.isHttponly());
    }

    @Test(expected = IllegalArgumentException.class)
    public void badDomain() {
        new Cookie("foo.example.com", "/","lang=en-US; Path=/; Domain=bad.com");
        new Cookie("baz", "foo", "/bar", 0, true, "key", "value");
        new Cookie("baz", "foo.bar", "/bar", 0, true, "domain", "fu.bar");
        new Cookie("foo.bar", "/bar", "domain=fu.bar");
    }
}