package edu.msudenver.cs.replican;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class URLQueueTest {
    private URLQueue queue;

    @BeforeEach
    void setUp() {
        queue = new URLQueue();
    }

    @Test
    void addIfNewReturnsTrueForNewURL() {
        assertTrue(queue.addIfNew("http://example.com"));
    }

    @Test
    void addIfNewReturnsFalseForDuplicate() {
        queue.addIfNew("http://example.com");
        assertFalse(queue.addIfNew("http://example.com"));
    }

    @Test
    void sizeReflectsAddedURLs() {
        assertEquals(0, queue.size());
        queue.add("http://example.com");
        assertEquals(1, queue.size());
        queue.add("http://example2.com");
        assertEquals(2, queue.size());
    }

    @Test
    void getFetchedCountStartsAtZero() {
        queue.add("http://example.com");
        queue.add("http://example2.com");
        assertEquals(0, queue.getFetchedCount());
    }

    @Test
    void markFetchedUpdatesCount() {
        queue.add("http://example.com");
        queue.add("http://example2.com");
        queue.markFetched("http://example.com");
        assertEquals(1, queue.getFetchedCount());
        assertEquals(1, queue.getUnfetchedCount());
    }

    @Test
    void getUnfetchedURLsReturnsOnlyUnfetched() {
        queue.add("http://example.com");
        queue.add("http://example2.com");
        queue.add("http://example3.com");
        queue.markFetched("http://example.com");

        assertTrue(queue.getUnfetchedURLs().contains("http://example2.com"));
        assertTrue(queue.getUnfetchedURLs().contains("http://example3.com"));
        assertFalse(queue.getUnfetchedURLs().contains("http://example.com"));
        assertEquals(2, queue.getUnfetchedURLs().size());
    }

    @Test
    void isFetchedReturnsFalseForUnfetched() {
        queue.add("http://example.com");
        assertFalse(queue.isFetched("http://example.com"));
    }

    @Test
    void isFetchedReturnsTrueAfterMark() {
        queue.add("http://example.com");
        queue.markFetched("http://example.com");
        assertTrue(queue.isFetched("http://example.com"));
    }

    @Test
    void clearRemovesAllURLs() {
        queue.add("http://example.com");
        queue.add("http://example2.com");
        queue.clear();
        assertEquals(0, queue.size());
    }
}
