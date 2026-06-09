package edu.msudenver.cs.replican;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HttpURLFetcherTest {
    private final HttpURLFetcher fetcher = new HttpURLFetcher();

    @Test
    void fetcherCanBeInstantiated() {
        assertNotNull(fetcher);
    }

    @Test
    void fetcherImplementsFetcherInterface() {
        assertTrue(fetcher instanceof URLFetcher);
    }
}
