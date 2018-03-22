package edu.msudenver.cs.replican;

import org.junit.Test;

import static org.junit.Assert.*;

public class CookiesTest {
    @Test
    public void getCookiesForDomainAndPath() {
    }

    @Test
    public void getCookiesForUrl() {
    }

    @Test
    public void getAllCookies() {
    }

    @Test
    public void addCookie() {
    }

    @Test
    public void addCookie1() {
    }

    @Test
    public void URLDomainAndPath() {
        final Cookie cookie1 = new Cookie("foo.example.com",
                "/bar/baz",
                "SID=31d4d96e407aad42");
        final Cookie cookie2 = new Cookie("foo.example.com",
                "/bar/baz",
                "foo=bar");
        final Cookies cookies = new Cookies();

        cookies.addCookie(cookie1);
        assertEquals(1, cookies.getAllCookies().size());
        cookies.addCookie(cookie2);
        assertEquals(1, cookies.getAllCookies().size());

        assertEquals("Cookie: foo=bar; SID=31d4d96e407aad42",
                cookies.getCookiesForDomainAndPath("foo.example.com",
                        "/bar/baz").remove().getCookieString());
    }

    @Test
    public void addCookie2() {
        final Cookie cookie = new Cookie("foo.example.com",
                "/bar/baz",
                "SID=31d4d96e407aad42");
        final Cookies cookies = new Cookies();

        cookies.addCookie(cookie);

        assertEquals(1, cookies.getAllCookies().size());

        assertEquals("Cookie: SID=31d4d96e407aad42",
                cookies.getCookiesForDomainAndPath("foo.example.com",
                        "/bar/baz").remove().getCookieString());

        // don't go more generic
        assertEquals(0,
                cookies.getCookiesForDomainAndPath("example.com",
                        "/bar/baz").size());
        assertEquals(0,
                cookies.getCookiesForDomainAndPath("foo.example.com",
                        "/bar").size());


    }

    @Test
    public void subPath() {
        final Cookie cookie = new Cookie("foo.example.com", "/",
                "SID=31d4d96e407aad42");
        final Cookies cookies = new Cookies();

        cookies.addCookie(cookie);

        assertEquals(1, cookies.getAllCookies().size());

        assertEquals("Cookie: SID=31d4d96e407aad42",
                cookies.getCookiesForDomainAndPath("foo.example.com",
                        "/bar/baz").remove().getCookieString());
        assertEquals("Cookie: SID=31d4d96e407aad42",
                cookies.getCookiesForDomainAndPath("foo.example.com",
                        "/bar").remove().getCookieString());
        assertEquals("Cookie: SID=31d4d96e407aad42",
                cookies.getCookiesForDomainAndPath("foo.example.com",
                        "/").remove().getCookieString());
    }

    @Test
    public void specificHost() {
        final Cookie cookie = new Cookie("example.com", "/",
                "SID=31d4d96e407aad42");
        final Cookies cookies = new Cookies();

        cookies.addCookie(cookie);

        assertEquals(1, cookies.getAllCookies().size());

        assertEquals("Cookie: SID=31d4d96e407aad42",
                cookies.getCookiesForDomainAndPath("foo.example.com",
                        "/bar").remove().getCookieString());
        assertEquals("Cookie: SID=31d4d96e407aad42",
                cookies.getCookiesForDomainAndPath("foo.example.com",
                        "/").remove().getCookieString());
    }
}