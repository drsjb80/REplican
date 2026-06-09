package edu.msudenver.cs.replican;

import java.net.URL;
import java.util.Queue;

public interface CookieManager {
    Queue<Cookie> getCookiesForUrl(URL url);

    void addCookie(Cookie cookie);

    void addCookie(String host, String path, String cookieString);

    int getCookieCount();
}
