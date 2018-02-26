package edu.msudenver.cs.replican;

import org.junit.Test;

import static org.junit.Assert.*;

public class CookieTest {
    private final String firstTestCookieString = "SID=31d4d96e407aad42";
    private final Cookie firstTestCookie = new Cookie("example.com", "/", firstTestCookieString);
    private final String secondTestCookieString ="SID=31d4d96e407aad42; Path=/; Domain=example.com";
    private final Cookie secondTestCookie = new Cookie("example.com", "/", secondTestCookieString);
    private final String thirdTestCookieString ="SID=31d4d96e407aad42; Path=/; Secure; HttpOnly";
    private final String fourthTestCookieString = "lang=en-US; Path=/; Domain=example.com";
    private final Cookie thirdTestCookie = new Cookie("example.com", "/", thirdTestCookieString);
    private final Cookie fifthTestCookie = new Cookie(".example.com", "/", firstTestCookieString);

    @Test
    public void getSave() throws Exception {
    }

    @Test
    public void getCookieString() throws Exception {
        assertEquals("Cookie: SID=31d4d96e407aad42", firstTestCookie.getCookieString());
        assertEquals("Cookie: SID=31d4d96e407aad42", secondTestCookie.getCookieString());
        thirdTestCookie.addToCookie(fourthTestCookieString);
        assertEquals("Cookie: lang=en-US; SID=31d4d96e407aad42", thirdTestCookie.getCookieString());

    }

    @Test
    public void testToString() throws Exception {
    }

    @Test
    public void getDomain() throws Exception {
        /*
        assertEquals("example.com", firstTestCookie.getDomain());
        assertEquals("example.com", secondTestCookie.getDomain());
        assertEquals("example.com", thirdTestCookie.getDomain());
        assertEquals("example.com", fifthTestCookie.getDomain());
        */
    }

    @Test
    public void getPath() throws Exception {
        assertEquals("/", firstTestCookie.getPath());
        assertEquals("/", secondTestCookie.getPath());
        assertEquals("/", thirdTestCookie.getPath());
    }

    @Test
    public void getMaxAge() throws Exception {
        assertEquals(0, firstTestCookie.getMaxAge());
        assertEquals(0, secondTestCookie.getMaxAge());
        assertEquals(0, thirdTestCookie.getMaxAge());
    }

    @Test
    public void isSecure() throws Exception {
        assertEquals(false, firstTestCookie.isSecure());
        assertEquals(false, secondTestCookie.isSecure());
        assertEquals(true, thirdTestCookie.isSecure());
    }
    @Test
    public void isHttponly() throws Exception {
        assertEquals(false, firstTestCookie.isHttponly());
        assertEquals(false, secondTestCookie.isHttponly());
        assertEquals(true, thirdTestCookie.isHttponly());
    }
}