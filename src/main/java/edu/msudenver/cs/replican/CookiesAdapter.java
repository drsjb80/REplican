package edu.msudenver.cs.replican;

import lombok.NonNull;

import java.net.URL;
import java.util.Queue;

public class CookiesAdapter implements CookieManager {
    private final Cookies cookies;

    public CookiesAdapter(@NonNull Cookies cookies) {
        this.cookies = cookies;
    }

    public Cookies getCookies() {
        return cookies;
    }

    @Override
    public Queue<Cookie> getCookiesForUrl(URL url) {
        return cookies.getCookiesForUrl(url);
    }

    @Override
    public void addCookie(Cookie cookie) {
        cookies.addCookie(cookie);
    }

    @Override
    public void addCookie(String host, String path, String cookieString) {
        cookies.addCookie(new Cookie(host, path, cookieString));
    }

    @Override
    public int getCookieCount() {
        return cookies.getAllCookies().size();
    }
}
