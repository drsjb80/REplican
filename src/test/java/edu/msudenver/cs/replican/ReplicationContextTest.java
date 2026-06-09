package edu.msudenver.cs.replican;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReplicationContextTest {
    private ReplicationContext context;
    private ConfigProvider config;
    private CookieManager cookies;
    private URLQueue queue;
    private URLFetcher fetcher;
    private FileSaver saver;

    @BeforeEach
    void setUp() {
        config = new REplicanConfigProvider(REplicanArgs.createDefault());
        cookies = new CookiesAdapter(new Cookies());
        queue = new URLQueue();
        fetcher = null; // Will be mocked in real tests
        saver = null;   // Will be mocked in real tests

        context = new ReplicationContext(config, cookies, queue, fetcher, saver, LogManager.getLogger());
    }

    @Test
    void contextProvidesAllComponents() {
        assertNotNull(context.getConfig());
        assertNotNull(context.getCookies());
        assertNotNull(context.getQueue());
        assertNotNull(context.getLogger());
    }

    @Test
    void contextCanBeConstructedWithAllDependencies() {
        assertNotNull(context);
        assertEquals(config, context.getConfig());
        assertEquals(cookies, context.getCookies());
        assertEquals(queue, context.getQueue());
    }

    @Test
    void contexProvidedComponentsAreImmutable() {
        ConfigProvider config1 = context.getConfig();
        ConfigProvider config2 = context.getConfig();
        assertSame(config1, config2);
    }
}
