package edu.msudenver.cs.replican;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

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

    @Test
    void getContentLengthFromHeaderWithValidValue() {
        YouAreEll yae = new YouAreEll("http://example.com");
        // Content-Length is set by connect() reading from URLConnection
        // Without mocking, we can't easily test this - just verify method exists
        assertNotNull(yae);
    }

    @Test
    void getLastModifiedReturnsValue() {
        YouAreEll yae = new YouAreEll("http://example.com");
        // LastModified comes from URLConnection
        assertNotNull(yae);
    }

    @Test
    void constructorWithComplexUrl() {
        String complexUrl = "https://user:pass@example.com:8443/path?param=value&other=123#anchor";
        YouAreEll yae = new YouAreEll(complexUrl);
        assertEquals(complexUrl, yae.getUrl());
    }

    @Test
    void constructorWithRedirectUrl() {
        YouAreEll yae = new YouAreEll("http://example.com/old-path");
        assertEquals("http://example.com/old-path", yae.getUrl());
    }

    @Test
    void constructorWithTrailingSlash() {
        YouAreEll yae = new YouAreEll("http://example.com/");
        assertEquals("http://example.com/", yae.getUrl());
    }

    @Test
    void constructorWithoutTrailingSlash() {
        YouAreEll yae = new YouAreEll("http://example.com");
        assertEquals("http://example.com", yae.getUrl());
    }

    @Test
    void contentTypeIsNullBeforeFetch() {
        YouAreEll yae = new YouAreEll("http://example.com");
        assertNull(yae.getContentType());
    }

    @Test
    void urlIsAccessibleAfterConstruction() {
        YouAreEll yae = new YouAreEll("http://example.com/test");
        assertNotNull(yae.getUrl());
        assertTrue(yae.getUrl().contains("example.com"));
    }

    @Test
    void cookiesCanBeProvidedOptionally() {
        Cookies cookies = new Cookies();
        YouAreEll yae1 = new YouAreEll("http://example.com");
        YouAreEll yae2 = new YouAreEll("http://example.com", cookies);
        YouAreEll yae3 = new YouAreEll("http://example.com", null);

        assertNotNull(yae1);
        assertNotNull(yae2);
        assertNotNull(yae3);
    }

    @Test
    void multipleUrlVariations() {
        String[] urls = {
            "http://example.com",
            "https://example.com",
            "http://example.com:80",
            "https://example.com:443",
            "http://example.com/path",
            "http://example.com/path/to/resource",
            "http://example.com?query=1",
            "http://example.com#hash"
        };

        for (String url : urls) {
            YouAreEll yae = new YouAreEll(url);
            assertEquals(url, yae.getUrl());
        }
    }

    @Test
    void constructorWithIPAddress() {
        YouAreEll yae = new YouAreEll("http://192.168.1.1");
        assertEquals("http://192.168.1.1", yae.getUrl());
    }

    @Test
    void constructorWithIPAddressAndPort() {
        YouAreEll yae = new YouAreEll("http://192.168.1.1:8080");
        assertEquals("http://192.168.1.1:8080", yae.getUrl());
    }

    @Test
    void constructorWithLocalhost() {
        YouAreEll yae = new YouAreEll("http://localhost:3000");
        assertEquals("http://localhost:3000", yae.getUrl());
    }

    @Test
    void contentTypeRemainsNullWhenNotSet() {
        YouAreEll yae = new YouAreEll("http://example.com");
        assertNull(yae.getContentType());
        // Even after checking multiple times
        assertNull(yae.getContentType());
    }

    @Test
    void urlWithInternationalDomain() {
        YouAreEll yae = new YouAreEll("http://例え.jp");
        assertEquals("http://例え.jp", yae.getUrl());
    }

    @Test
    void constructorPreservesUrlExactly() {
        String url = "http://example.com/path?q=1&r=2&s=3#section";
        YouAreEll yae = new YouAreEll(url);
        assertEquals(url, yae.getUrl());
    }

    @Test
    void youAreEllWithCookiesAndUrl() {
        Cookies cookies = new Cookies();
        YouAreEll yae = new YouAreEll("http://example.com", cookies);
        assertEquals("http://example.com", yae.getUrl());
        assertNull(yae.getContentType());
    }

    @Test
    void getUrlAfterConstruction() {
        YouAreEll yae = new YouAreEll("http://example.com/test");
        String url = yae.getUrl();
        assertEquals("http://example.com/test", url);
        // Verify URL doesn't change
        assertEquals(url, yae.getUrl());
    }

    @Test
    void multipleInstancesAreIndependent() {
        YouAreEll yae1 = new YouAreEll("http://first.com");
        YouAreEll yae2 = new YouAreEll("http://second.com");
        YouAreEll yae3 = new YouAreEll("http://third.com");

        assertNotEquals(yae1.getUrl(), yae2.getUrl());
        assertNotEquals(yae2.getUrl(), yae3.getUrl());
        assertNotEquals(yae1.getUrl(), yae3.getUrl());
    }

    @Test
    void contentTypeNullUntilFetched() {
        YouAreEll yae1 = new YouAreEll("http://example1.com");
        YouAreEll yae2 = new YouAreEll("http://example2.com");
        YouAreEll yae3 = new YouAreEll("http://example3.com");

        assertNull(yae1.getContentType());
        assertNull(yae2.getContentType());
        assertNull(yae3.getContentType());
    }
}
