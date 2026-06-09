package edu.msudenver.cs.replican;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class URLQueue {
    private final ConcurrentHashMap<String, Boolean> urls = new ConcurrentHashMap<>();
    private final AtomicInteger count = new AtomicInteger(0);

    public boolean addIfNew(String url) {
        if (urls.putIfAbsent(url, false) == null) {
            count.incrementAndGet();
            return true;
        }
        return false;
    }

    public void add(String url) {
        addIfNew(url);
    }

    public void markFetched(String url) {
        urls.put(url, true);
    }

    public List<String> getUnfetchedURLs() {
        List<String> unfetched = new ArrayList<>();
        for (String url : urls.keySet()) {
            if (!urls.get(url)) {
                unfetched.add(url);
            }
        }
        return unfetched;
    }

    public int size() {
        return urls.size();
    }

    public int getFetchedCount() {
        return (int) urls.values().stream().filter(v -> v).count();
    }

    public int getUnfetchedCount() {
        return size() - getFetchedCount();
    }

    public boolean isFetched(String url) {
        return urls.getOrDefault(url, false);
    }

    public void clear() {
        urls.clear();
        count.set(0);
    }
}
