package edu.msudenver.cs.replican;

import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Cookies {
    private final Logger logger = LogManager.getLogger(getClass());
    private final Map<Pair<String, String>, Cookie> cookies = new ConcurrentHashMap<>();

    Queue<Cookie> getCookiesForDomainAndPath(@NonNull final String domain, @NonNull final String path) {
        logger.traceEntry(domain);
        logger.traceEntry(path);

        final Queue<Cookie> toReturn = new ConcurrentLinkedQueue<>();

        for (Pair<String, String> cookie: cookies.keySet()) {
            if (domain.endsWith(cookie.getValue0())
                    && path.startsWith(cookie.getValue1())) {
                toReturn.add(cookies.get(cookie));
            }
        }

        logger.traceExit(toReturn);
        return toReturn;
    }

    Queue<Cookie> getCookiesForUrl(@NonNull final URL url) {
        logger.traceEntry(url.toString());

        Queue<Cookie> host = getCookiesForDomainAndPath(url.getHost(), url.getPath());
        Queue<Cookie> domain = getCookiesForDomainAndPath(Utils.hostToDomain(url.getHost()),
                url.getPath());

        host.addAll(domain);
        return host;
    }

    Queue<Cookie> getAllCookies() {
        final Queue<Cookie> toReturn = new ConcurrentLinkedQueue<>();

        for (Pair<String, String> cookie: cookies.keySet()) {
            toReturn.add(cookies.get(cookie));
        }

        return toReturn;
    }

    void removeAllCookies() {
        logger.warn("Clearing all cookies");

        cookies.clear();
    }

    void addCookie(@NonNull final String URLHost, @NonNull final String URLPath,
                   @NonNull final String cookieString) {
        logger.traceEntry(URLHost);
        logger.traceEntry(URLPath);
        logger.traceEntry(cookieString);

        final Cookie cookie = new Cookie(URLHost, URLPath, cookieString);
        addCookie(cookie);
    }

    void addCookie(@NonNull final URL url, @NonNull final String cookieString) {
        logger.traceEntry(url.toString());
        logger.traceEntry(cookieString);

        final Cookie cookie = new Cookie(url.getHost(), url.getPath(), cookieString);
        addCookie(cookie);
    }

    void addCookie(@NonNull final Cookie cookie) {
        logger.traceEntry(cookie.toString());
        System.out.println(cookie.toString());

        Pair<String, String> pair = new Pair<>(cookie.getDomain(), cookie.getPath());
        if (cookies.containsKey(pair)) {
            cookies.get(pair).addCookie(cookie);
        } else {
            cookies.put(pair, cookie);
        }
    }
}
