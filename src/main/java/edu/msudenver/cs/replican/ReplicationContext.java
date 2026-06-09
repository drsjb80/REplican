package edu.msudenver.cs.replican;

import lombok.NonNull;
import org.apache.logging.log4j.Logger;

public class ReplicationContext {
    private final ConfigProvider config;
    private final CookieManager cookies;
    private final URLQueue queue;
    private final URLFetcher fetcher;
    private final FileSaver saver;
    private final Logger logger;

    public ReplicationContext(
            @NonNull ConfigProvider config,
            @NonNull CookieManager cookies,
            @NonNull URLQueue queue,
            @NonNull URLFetcher fetcher,
            @NonNull FileSaver saver,
            @NonNull Logger logger) {
        this.config = config;
        this.cookies = cookies;
        this.queue = queue;
        this.fetcher = fetcher;
        this.saver = saver;
        this.logger = logger;
    }

    public ConfigProvider getConfig() {
        return config;
    }

    public CookieManager getCookies() {
        return cookies;
    }

    public URLQueue getQueue() {
        return queue;
    }

    public URLFetcher getFetcher() {
        return fetcher;
    }

    public FileSaver getSaver() {
        return saver;
    }

    public Logger getLogger() {
        return logger;
    }
}
