package edu.msudenver.cs.replican;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReplicatorTest {
    private Replicator replicator;
    private ReplicationContext context;
    private URLQueue queue;
    private ConfigProvider config;
    private CookieManager cookies;

    @BeforeEach
    void setUp() {
        queue = new URLQueue();
        config = new REplicanConfigProvider(REplicanArgs.createDefault());
        cookies = new CookiesAdapter(new Cookies());

        URLFetcher mockFetcher = new URLFetcher() {
            @Override
            public FetchResult fetch(String url) {
                return new FetchResult() {
                    @Override
                    public InputStream getInputStream() {
                        return new ByteArrayInputStream("<html></html>".getBytes());
                    }

                    @Override
                    public String getContentType() {
                        return "text/html";
                    }

                    @Override
                    public int getContentLength() {
                        return 14;
                    }

                    @Override
                    public long getLastModified() {
                        return 0;
                    }
                };
            }
        };

        FileSaver mockSaver = new FileSaver() {
            @Override
            public java.io.File prepare(YouAreEll metadata) {
                return null;
            }

            @Override
            public void save(java.io.File file, InputStream content) {
            }

            @Override
            public void setLastModified(java.io.File file, long timestamp) {
            }

            @Override
            public String getFilePath(String url) throws Exception {
                return "/tmp/" + url.hashCode();
            }
        };

        context = new ReplicationContext(config, cookies, queue, mockFetcher, mockSaver, LogManager.getLogger());
        replicator = new Replicator(context);
    }

    @Test
    void replicatorCanBeInstantiated() {
        assertNotNull(replicator);
    }

    @Test
    void addURLIncrementsQueueSize() {
        assertEquals(0, replicator.getQueueSize());
        replicator.addURL("http://example.com");
        assertEquals(1, replicator.getQueueSize());
    }

    @Test
    void addURLIgnoresDuplicates() {
        replicator.addURL("http://example.com");
        replicator.addURL("http://example.com");
        assertEquals(1, replicator.getQueueSize());
    }

    @Test
    void addMultipleURLs() {
        replicator.addURL("http://example.com");
        replicator.addURL("http://example2.com");
        replicator.addURL("http://example3.com");
        assertEquals(3, replicator.getQueueSize());
    }

    @Test
    void getQueueSizeReflectsAddedURLs() {
        replicator.addURL("http://example.com");
        replicator.addURL("http://example2.com");
        assertEquals(2, replicator.getQueueSize());
    }

    @Test
    void getFetchedCountStartsAtZero() {
        replicator.addURL("http://example.com");
        assertEquals(0, replicator.getFetchedCount());
    }

    @Test
    void getUnfetchedCountReflectsAddedURLs() {
        replicator.addURL("http://example.com");
        replicator.addURL("http://example2.com");
        assertEquals(2, replicator.getUnfetchedCount());
    }

    @Test
    void addURLsFromHTMLProcessesLines() {
        List<String> lines = new ArrayList<>();
        lines.add("<a href=\"http://example.com/page1\">Link</a>");
        lines.add("<a href=\"http://example.com/page2\">Link</a>");

        replicator.addURLsFromHTML("http://example.com", lines);
        // Note: Since we don't have patterns configured, interesting() won't find URLs
        // but the method should not throw
        assertDoesNotThrow(() -> replicator.addURLsFromHTML("http://example.com", lines));
    }

    @Test
    void addURLsFromHTMLWithEmptyListDoesNotThrow() {
        List<String> lines = new ArrayList<>();
        assertDoesNotThrow(() -> replicator.addURLsFromHTML("http://example.com", lines));
    }

    @Test
    void fetchAllReturnsWhenQueueEmpty() throws Exception {
        replicator.fetchAll();
        assertEquals(0, replicator.getFetchedCount());
    }

    @Test
    void fetchAllFetchesAllURLs() throws Exception {
        replicator.addURL("http://example.com");
        replicator.addURL("http://example2.com");

        replicator.fetchAll();

        assertEquals(2, replicator.getFetchedCount());
        assertEquals(0, replicator.getUnfetchedCount());
    }

    @Test
    void getQueueSizeReturnsAccurateCount() {
        assertEquals(0, replicator.getQueueSize());
        replicator.addURL("http://example.com");
        replicator.addURL("http://example2.com");
        replicator.addURL("http://example3.com");
        assertEquals(3, replicator.getQueueSize());
    }

    @Test
    void getFetchedCountStartsAtZeroAfterInit() {
        replicator.addURL("http://example.com");
        replicator.addURL("http://example2.com");
        assertEquals(0, replicator.getFetchedCount());
    }

    @Test
    void getUnfetchedCountIncludesAllNewURLs() {
        replicator.addURL("http://example.com");
        replicator.addURL("http://example2.com");
        replicator.addURL("http://example3.com");
        assertEquals(3, replicator.getUnfetchedCount());
    }

    @Test
    void addURLWithNullURLThrowsException() {
        assertThrows(NullPointerException.class, () -> replicator.addURL(null));
    }

    @Test
    void addURLWithEmptyStringAddsToQueue() {
        replicator.addURL("");
        assertEquals(1, replicator.getQueueSize());
    }

    @Test
    void addURLsFromHTMLWithNullBaseURL() {
        List<String> lines = new ArrayList<>();
        lines.add("<a href=\"http://example.com\">Link</a>");
        assertDoesNotThrow(() -> replicator.addURLsFromHTML(null, lines));
    }

    @Test
    void addURLsFromHTMLWithMultipleLines() {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            lines.add("<a href=\"http://example.com/page" + i + "\">Link</a>");
        }
        assertDoesNotThrow(() -> replicator.addURLsFromHTML("http://example.com", lines));
    }

    @Test
    void replicatorMaintainsQueueConsistency() throws Exception {
        replicator.addURL("http://example.com");
        assertEquals(1, replicator.getQueueSize());
        assertEquals(1, replicator.getUnfetchedCount());
        assertEquals(0, replicator.getFetchedCount());

        replicator.fetchAll();

        assertEquals(1, replicator.getQueueSize());
        assertEquals(0, replicator.getUnfetchedCount());
        assertEquals(1, replicator.getFetchedCount());
    }

    @Test
    void addURLPreservesDuplicateDetection() {
        replicator.addURL("http://example.com");
        replicator.addURL("http://example.com");
        replicator.addURL("http://example.com");
        assertEquals(1, replicator.getQueueSize());
    }

    @Test
    void fetchAllWithMixedURLs() throws Exception {
        replicator.addURL("http://example.com");
        replicator.addURL("http://other.com");
        replicator.addURL("http://third.org");

        assertEquals(3, replicator.getQueueSize());
        replicator.fetchAll();
        assertEquals(3, replicator.getFetchedCount());
    }
}
