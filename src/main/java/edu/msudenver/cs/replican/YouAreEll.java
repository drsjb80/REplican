package edu.msudenver.cs.replican;

import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.io.*;

import java.util.Map;
import java.util.List;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class YouAreEll {
    private final Logger logger = LogManager.getLogger(getClass());
    private URLConnection uc;
    @Getter private String ContentType;
    @Getter private int ContentLength;
    private final InputStream inputstream;

    @Getter private final String url;
    private final Cookies cookies;

    public YouAreEll(String url, Cookies cookies) {
        this.url = url;
        this.cookies = cookies;
        inputstream = createInputStream();
    }
    //THREADSAFE_LEVEL_BLACK
    String getContentType() { return (ContentType); }
    //THREADSAFE_LEVEL_BLACK
    int getContentLength() { return (ContentLength); }
    //THREADSAFE_LEVEL_BLACK
    String getURL() { return (url); }
    //THREADSAFE_LEVEL_BLACK
    InputStream getInputStream() { return (inputstream); }
    //THREADSAFE_LEVEL_BLACK
    long getLastModified() { return (uc.getLastModified()); }


    //THREADSAFE_LEVEL_BLACK
    private void dealWithRedirects() {
        /*
        HTTP/1.1 301 Moved Permanently
        Date: Wed, 16 May 2007 19:18:57 GMT
        Server: Apache/2.0.53 (Fedora)
        Location: http://emess.mscd.edu/~beaty/Pictures/
        Content-Length: 326
        Connection: close
        Content-Type: text/html; charset=iso-8859-1
        */

        // mark the original fetched

        REplican.urls.put(url, Boolean.TRUE);

        if (!REplican.args.FollowRedirects)
            return;

        String redirected = uc.getHeaderField("Location");
        if (REplican.args.PrintRedirects)
            logger.info("Redirected to: " + redirected);

        URL newURL = null;

        try {
            newURL = new URL(new URL(url), redirected);
        } catch (MalformedURLException e) {
            logger.throwing(e);
            return;
        }

        REplican.urls.put(newURL.toString(), Boolean.FALSE);
    }
    //THREADSAFE_LEVEL_GREY
    private void dealWithStopOns(int code) {
        int[] stopon = REplican.args.StopOn;

        for (int i = 0; i < stopon.length; i++) {
            if (code == stopon[i]) {
                logger.warn("Stopping on return code: " + code);
                System.exit(0);
            }
        }
    }
    //THREADSAFE_LEVEL_BLACK
    private InputStream HUC() {
        ContentType = uc.getHeaderField("Content-Type");
        String cl = uc.getHeaderField("Content-Length");
        if (cl != null) {
            try {
                ContentLength = Integer.parseInt(cl);
            } catch (NumberFormatException NFE) {
                ContentLength = 0;
            }
        }

        String MIMEAccept[] = REplican.args.MIMEAccept;
        String MIMEReject[] = REplican.args.MIMEReject;

        // here, if MIME has no input on the matter, assume Path has
        // already spoken so return true from blurf.
        if (!Utils.blurf(MIMEAccept, MIMEReject, ContentType, true))
            return (null);

        try {
            return (uc.getInputStream());
        } catch (IOException e) {
            logger.throwing(e);
            return (null);
        }
    }

    //THREADSAFE_LEVEL_BLACK
    private InputStream dealWithReturnCode(int code) {
        logger.traceEntry(Integer.toString(code));

        if (REplican.args.StopOnnull)
            dealWithStopOns(code);

        switch (code) {
            case 200:
                return (HUC());
            case 301:
            case 302: {
                dealWithRedirects();
                return (null);
            }
            default: {
                try {
                    String message = "";

                    if (uc instanceof HttpURLConnection)
                        message = ((HttpURLConnection) uc).getResponseMessage();
                    logger.warn("For: " + url + " server returned: " +
                            code + " " + message);
                } catch (IOException e) {
                    logger.throwing(e);
                }

                return (null);
            }
        }
    }

    //THREADSAFE_LEVEL_GREY
    private int connect() throws IOException {
        uc = new URL(url).openConnection();

        try {
            if (REplican.args.UserAgent != null)
                uc.setRequestProperty("User-Agent", REplican.args.UserAgent);

            if (REplican.args.Header != null) {
                for (int i = 0; i < REplican.args.Header.length; i++) {
                    String s[] = REplican.args.Header[i].split(":");
                    if (s[0] != null && s[1] != null) {
                        uc.setRequestProperty(s[0], s[1]);
                    } else {
                        logger.trace("Couldn't decipher " + REplican.args.Header[i]);
                    }
                }
            }

            String Referer = REplican.args.Referer;
            if (Referer != null)
                uc.setRequestProperty("Referer", Referer);

            if (!REplican.args.IgnoreCookies) {
                String c = cookies.getCookieStringsForURL(new URL(url));

                if (c == null)
                    logger.trace("No cookie");
                else {
                    logger.trace(c);
                    uc.setRequestProperty("Cookie", c);
                }
            }
        } catch (IllegalStateException ise) {
            logger.throwing(ise);
        }

        int rc = 200;

        if (uc instanceof HttpURLConnection)
            rc = ((HttpURLConnection) uc).getResponseCode();

        uc.connect();

        logger.traceExit(rc);
        return (rc);
    }
    //THREADSAFE_LEVEL_BLACK
    private InputStream getURLInputStream() {
        int code;

        try {
            code = connect();
        } catch (IOException e) {
            logger.warn(e);
            return (null);
        }

        logger.trace(uc.toString());
        logger.trace(uc.getHeaderFields().toString());

        if (!REplican.args.IgnoreCookies) {
            Map<String, List<String>> m = uc.getHeaderFields();
            List<String> l = m.get("Set-Cookie");
            if (l != null) {
                for (String cookie : l) {
                    logger.trace("Adding cookie: " + cookie);
                    try {
                        cookies.addCookie(new URL(url), cookie);
                    } catch (MalformedURLException MUE) {
                        logger.throwing(MUE);
                    }
                }
            }
        }

        return (dealWithReturnCode(code));
    }

    /**
     * get an InputStream from either a file: or http: URL.  deals
     * with http redirections.
     */

    //THREADSAFE_LEVEL_BLACK
    private InputStream createInputStream() {
        // http://httpd.apache.org/docs/1.3/mod/mod_dir.html#directoryindex

        logger.traceEntry(url);
        InputStream is = getURLInputStream();
        logger.traceExit(is);
        return (is);
    }
}
