package edu.msudenver.cs.replican;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class Cookies {
    private final Logger logger = LogManager.getLogger(getClass());
    private final Map<Pair<String, String>, Cookie> cookies = new ConcurrentHashMap<>();

    List<Cookie> getCookiesForDomainAndPath(final String domain, final String path) {
        logger.traceEntry(domain + path);
        final List<Cookie> toReturn = new ArrayList<>();

        for (Pair<String, String> cookie: cookies.keySet()) {
            if (cookie.getValue0().endsWith(domain)
                    && cookie.getValue1().startsWith(path)) {
                toReturn.add(cookies.get(cookie));
            }
        }
        return toReturn;
    }

    List<Cookie> getCookiesForUrl(final URL url) {
        logger.traceEntry(url.toString());
        List<Cookie> host = getCookiesForDomainAndPath(url.getHost(), url.getPath());
        List<Cookie> domain = getCookiesForDomainAndPath(Utils.hostToDomain(url.getHost()),
                url.getPath());

        host.addAll(domain);
        return host;
    }

    List<Cookie> getAllCookies() {
        return getCookiesForDomainAndPath("", "");
    }

    void removeAllCookies() {
        logger.warn("Clearing all cookies");
        cookies.clear();
    }

    void addCookie(final String URLHost, final String URLPath,
                   final String cookieString) {
        logger.traceEntry(URLHost + URLPath + cookieString);
        final Cookie cookie = new Cookie(URLHost, URLPath, cookieString);
        addCookie(cookie);
    }

    void addCookie(final URL url, final String cookieString) {
        logger.traceEntry(url + cookieString);
        final Cookie cookie = new Cookie(url.getHost(), url.getPath(), cookieString);
        addCookie(cookie);
    }

    void addCookie(Cookie cookie) {
        logger.traceEntry(cookie.toString());
        Pair<String, String> pair = new Pair<>(cookie.getDomain(), cookie.getPath());
        if (cookies.containsKey(pair)) {
            cookies.get(pair).addCookie(cookie);
        } else {
            cookies.put(pair, cookie);
        }
    }
}