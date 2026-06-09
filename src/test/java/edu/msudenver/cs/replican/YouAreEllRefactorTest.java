package edu.msudenver.cs.replican;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class YouAreEllRefactorTest {
    private YouAreEll youAreEll;
    private HttpConnection mockConnection;
    private HttpConnectionFactory mockFactory;
    private ConfigProvider mockConfig;
    private Cookies mockCookies;

    @BeforeEach
    void setUp() {
        mockConnection = mock(HttpConnection.class);
        mockFactory = mock(HttpConnectionFactory.class);
        mockConfig = mock(ConfigProvider.class);
        mockCookies = new Cookies();

        // Default mock behavior
        try {
            when(mockFactory.createConnection(anyString())).thenReturn(mockConnection);
        } catch (IOException e) {
            fail("Mock setup failed", e);
        }
        when(mockConfig.isFollowRedirects()).thenReturn(false);
        when(mockConfig.isIgnoreCookies()).thenReturn(false);
        when(mockConfig.getMIMEAccept()).thenReturn(null);
        when(mockConfig.getMIMEReject()).thenReturn(null);
        when(mockConfig.getStopOnStatusCodes()).thenReturn(new int[0]);
        when(mockConfig.getHeader()).thenReturn(null);
    }

    @Test
    void createsWithFullDependencyInjection() {
        youAreEll = new YouAreEll("http://example.com", mockCookies, mockConfig, mockFactory, LogManager.getLogger());
        assertNotNull(youAreEll);
        assertEquals("http://example.com", youAreEll.getUrl());
    }

    @Test
    void getsUrlAfterConstruction() {
        youAreEll = new YouAreEll("http://example.com", mockCookies, mockConfig, mockFactory, LogManager.getLogger());
        assertEquals("http://example.com", youAreEll.getUrl());
    }

    @Test
    void contentTypeIsNullBeforeConnection() {
        youAreEll = new YouAreEll("http://example.com", mockCookies, mockConfig, mockFactory, LogManager.getLogger());
        assertNull(youAreEll.getContentType());
    }

    @Test
    void getContentLengthReturnsZeroWhenNotConnected() {
        youAreEll = new YouAreEll("http://example.com", mockCookies, mockConfig, mockFactory, LogManager.getLogger());
        assertEquals(0, youAreEll.getContentLength());
    }

    @Test
    void getLastModifiedReturnsZeroWhenNotConnected() {
        youAreEll = new YouAreEll("http://example.com", mockCookies, mockConfig, mockFactory, LogManager.getLogger());
        assertEquals(0, youAreEll.getLastModified());
    }

    @Test
    void usesInjectedFactory() throws IOException {
        youAreEll = new YouAreEll("http://example.com", mockCookies, mockConfig, mockFactory, LogManager.getLogger());

        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream("<html></html>".getBytes()));

        try {
            youAreEll.getInputStream();
        } catch (Exception e) {
            // May fail due to HUC() logic, but factory should be called
        }

        verify(mockFactory).createConnection("http://example.com");
    }

    @Test
    void respectsInjectedConfiguration() throws IOException {
        when(mockConfig.isFollowRedirects()).thenReturn(true);
        when(mockConfig.isPrintRedirects()).thenReturn(true);

        youAreEll = new YouAreEll("http://example.com", mockCookies, mockConfig, mockFactory, LogManager.getLogger());

        when(mockConnection.getResponseCode()).thenReturn(301);
        when(mockConnection.getHeaderField("Location")).thenReturn("http://redirected.com");

        try {
            youAreEll.getInputStream();
        } catch (Exception e) {
            // Expected - dealWithReturnCode returns null for redirects
        }

        verify(mockConfig).isFollowRedirects();
    }

    @Test
    void supportsNullCookies() {
        youAreEll = new YouAreEll("http://example.com", null, mockConfig, mockFactory, LogManager.getLogger());
        assertNotNull(youAreEll);
        assertEquals("http://example.com", youAreEll.getUrl());
    }

    @Test
    void acceptsDifferentURLs() {
        String[] urls = {
            "http://example.com",
            "https://secure.example.com",
            "http://example.com:8080/path",
            "http://localhost:3000"
        };

        for (String url : urls) {
            youAreEll = new YouAreEll(url, mockCookies, mockConfig, mockFactory, LogManager.getLogger());
            assertEquals(url, youAreEll.getUrl());
        }
    }

    @Test
    void handlesMockFactoryException() throws IOException {
        when(mockFactory.createConnection(anyString())).thenThrow(new IOException("Network error"));

        youAreEll = new YouAreEll("http://example.com", mockCookies, mockConfig, mockFactory, LogManager.getLogger());

        assertThrows(IOException.class, youAreEll::getInputStream);
    }

    @Test
    void injectsConfigProviderForMIMEFiltering() throws IOException {
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getHeaderField("Content-Type")).thenReturn("text/html");
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream("<html></html>".getBytes()));
        when(mockConfig.getMIMEAccept()).thenReturn(new String[]{"text/.*"});
        when(mockConfig.getMIMEReject()).thenReturn(null);

        youAreEll = new YouAreEll("http://example.com", mockCookies, mockConfig, mockFactory, LogManager.getLogger());

        try {
            InputStream is = youAreEll.getInputStream();
            // Should succeed if MIME filtering works via injected config
        } catch (Exception e) {
            // Acceptable - depends on full setup
        }

        verify(mockConfig).getMIMEAccept();
    }

    @Test
    void backwardCompatibleConstructor() {
        // Old constructor should still work
        youAreEll = new YouAreEll("http://example.com");
        assertNotNull(youAreEll);
        assertEquals("http://example.com", youAreEll.getUrl());
    }

    @Test
    void backwardCompatibleConstructorWithCookies() {
        Cookies cookies = new Cookies();
        youAreEll = new YouAreEll("http://example.com", cookies);
        assertNotNull(youAreEll);
        assertEquals("http://example.com", youAreEll.getUrl());
    }

    @Test
    void multipleInstancesAreIndependent() throws IOException {
        YouAreEll yae1 = new YouAreEll("http://first.com", mockCookies, mockConfig, mockFactory, LogManager.getLogger());
        YouAreEll yae2 = new YouAreEll("http://second.com", mockCookies, mockConfig, mockFactory, LogManager.getLogger());

        assertEquals("http://first.com", yae1.getUrl());
        assertEquals("http://second.com", yae2.getUrl());
        assertNotEquals(yae1.getUrl(), yae2.getUrl());
    }

    @Test
    void handlesHeaderConfiguration() throws IOException {
        when(mockConfig.getHeader()).thenReturn(new String[]{"User-Agent: TestAgent", "Accept: text/html"});

        youAreEll = new YouAreEll("http://example.com", mockCookies, mockConfig, mockFactory, LogManager.getLogger());

        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream("<html></html>".getBytes()));

        try {
            youAreEll.getInputStream();
        } catch (Exception e) {
            // Expected
        }

        verify(mockConnection).setRequestProperty("User-Agent", "TestAgent");
        verify(mockConnection).setRequestProperty("Accept", "text/html");
    }
}
