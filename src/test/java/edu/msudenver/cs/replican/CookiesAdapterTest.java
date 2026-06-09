package edu.msudenver.cs.replican;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CookiesAdapterTest {
    private CookieManager manager;
    private Cookies cookies;

    @BeforeEach
    void setUp() {
        cookies = new Cookies();
        manager = new CookiesAdapter(cookies);
    }

    @Test
    void addCookieStoresCookie() {
        Cookie cookie = new Cookie("example.com", "/", "SID=xyz");
        manager.addCookie(cookie);
        assertEquals(1, manager.getCookieCount());
    }

    @Test
    void addCookieWithStringStoresCookie() {
        manager.addCookie("example.com", "/", "SID=xyz");
        assertEquals(1, manager.getCookieCount());
    }

    @Test
    void getCookieCountReflectsAddedCookies() {
        assertEquals(0, manager.getCookieCount());
        manager.addCookie("example.com", "/", "SID=xyz");
        manager.addCookie("other.com", "/", "foo=bar");
        assertEquals(2, manager.getCookieCount());
    }
}
