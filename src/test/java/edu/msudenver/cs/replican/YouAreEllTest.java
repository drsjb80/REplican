package edu.msudenver.cs.replican;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class YouAreEllTest {

    @Test
    void constructorWithUrlOnly() {
        YouAreEll yae = new YouAreEll("http://example.com");
        assertNotNull(yae);
    }

    @Test
    void constructorWithUrlAndCookies() {
        Cookies cookies = new Cookies();
        YouAreEll yae = new YouAreEll("http://example.com", cookies);
        assertNotNull(yae);
    }

    @Test
    void getUrlReturnsConstructorUrl() {
        String testUrl = "http://example.com/path";
        YouAreEll yae = new YouAreEll(testUrl);
        assertEquals(testUrl, yae.getUrl());
    }

    @Test
    void getUrlWithDifferentUrls() {
        YouAreEll yae1 = new YouAreEll("http://example.com");
        YouAreEll yae2 = new YouAreEll("https://different.org");
        assertEquals("http://example.com", yae1.getUrl());
        assertEquals("https://different.org", yae2.getUrl());
    }

    @Test
    void getContentTypeInitiallyNull() {
        YouAreEll yae = new YouAreEll("http://example.com");
        assertNull(yae.getContentType());
    }

    @Test
    void constructorAcceptsNull() {
        assertDoesNotThrow(() -> {
            YouAreEll yae = new YouAreEll(null);
            assertNull(yae.getUrl());
        });
    }

    @Test
    void constructorWithNullUrl() {
        YouAreEll yae = new YouAreEll(null);
        assertNull(yae.getUrl());
    }

    @Test
    void constructorWithEmptyUrl() {
        YouAreEll yae = new YouAreEll("");
        assertEquals("", yae.getUrl());
    }

    @Test
    void constructorWithCookiesCanBeNullInSecondVersion() {
        YouAreEll yae = new YouAreEll("http://example.com", null);
        assertNotNull(yae);
    }

    @Test
    void getUrlWithSpecialCharacters() {
        String urlWithSpecialChars = "http://example.com/path?q=test&x=y#fragment";
        YouAreEll yae = new YouAreEll(urlWithSpecialChars);
        assertEquals(urlWithSpecialChars, yae.getUrl());
    }

    @Test
    void getUrlWithPortNumber() {
        String urlWithPort = "http://example.com:8080/path";
        YouAreEll yae = new YouAreEll(urlWithPort);
        assertEquals(urlWithPort, yae.getUrl());
    }

    @Test
    void multipleInstancesHaveIndependentUrls() {
        YouAreEll yae1 = new YouAreEll("http://first.com");
        YouAreEll yae2 = new YouAreEll("http://second.com");
        YouAreEll yae3 = new YouAreEll("http://third.com");

        assertEquals("http://first.com", yae1.getUrl());
        assertEquals("http://second.com", yae2.getUrl());
        assertEquals("http://third.com", yae3.getUrl());
    }

    @Test
    void constructorWithHttpsUrl() {
        YouAreEll yae = new YouAreEll("https://secure.example.com");
        assertEquals("https://secure.example.com", yae.getUrl());
    }

    @Test
    void constructorWithLocalFileUrl() {
        YouAreEll yae = new YouAreEll("file:///path/to/file.html");
        assertEquals("file:///path/to/file.html", yae.getUrl());
    }
}
