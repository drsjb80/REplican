package edu.msudenver.cs.replican;

import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.io.*;

import java.util.Map;
import java.util.List;
import java.util.Queue;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class YouAreEll {
    private final Logger logger = LogManager.getLogger(getClass());
    private URLConnection urlConnection;
    @Getter private String ContentType;

    private int ContentLength;
    int getContentLength() {
        String cl = urlConnection.getHeaderField("Content-Length");
        if (cl != null) {
            try {
                return Integer.parseInt(cl);
            } catch (NumberFormatException NFE) {
                return 0;
            }
        }
        return 0;
    }

    @Getter private final InputStream inputStream;
    @Getter private final String url;
    private final Cookies cookies = REplican.cookies;

    public YouAreEll(final String url) {
        this.url = url;
        this.inputStream = createInputStream();
    }

    long getLastModified() {
        return (urlConnection.getLastModified());
    }

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

        if (!REplican.args.FollowRedirects) {
            return;
        }

        String redirected = urlConnection.getHeaderField("Location");
        if (REplican.args.PrintRedirects)
            logger.info("Redirected to: " + redirected);

        URL newURL;

        try {
            newURL = new URL(new URL(url), redirected);
        } catch (MalformedURLException e) {
            logger.throwing(e);
            return;
        }

        REplican.urls.put(newURL.toString(), Boolean.FALSE);
    }

    private void dealWithStopOns(final int code) {
        for (int stopon: REplican.args.StopOn) {
            if (code == stopon) {
                logger.warn("Stopping on return code: " + code);
                System.exit(0);
            }
        }
    }

    private InputStream HUC() {
        ContentLength = getContentLength();
        ContentType = urlConnection.getHeaderField("Content-Type");
        String MIMEAccept[] = REplican.args.MIMEAccept;
        String MIMEReject[] = REplican.args.MIMEReject;

        // here, if MIME has no input on the matter, assume Path has
        // already spoken so return true from blurf.
        if (!Utils.blurf(MIMEAccept, MIMEReject, ContentType, true))
            return (null);

        try {
            return (urlConnection.getInputStream());
        } catch (IOException e) {
            logger.throwing(e);
            return (null);
        }
    }

    private InputStream dealWithReturnCode(int code) {
        logger.traceEntry(Integer.toString(code));

        if (REplican.args.StopOnnull)
            dealWithStopOns(code);

        switch (code) {
            case HttpURLConnection.HTTP_OK:
                return (HUC());
            case HttpURLConnection.HTTP_MOVED_PERM:
            case HttpURLConnection.HTTP_MOVED_TEMP:
                dealWithRedirects();
                return (null);
            default: {
                try {
                    String message = "";

                    if (urlConnection instanceof HttpURLConnection)
                        message = ((HttpURLConnection) urlConnection).getResponseMessage();
                    logger.warn("For: " + url + " server returned: " +
                            code + " " + message);
                } catch (IOException e) {
                    logger.throwing(e);
                }

                return (null);
            }
        }
    }

    private synchronized int connect() throws IOException {
        urlConnection = new URL(url).openConnection();
        addHeaderLines();
        setCookies();

        int returnCode = HttpURLConnection.HTTP_OK;

        if (urlConnection instanceof HttpURLConnection)
            returnCode = ((HttpURLConnection) urlConnection).getResponseCode();

        urlConnection.connect();

        logger.traceExit(returnCode);
        return returnCode;
    }

    private void setCookies() {
        String c = null;
        if (!REplican.args.IgnoreCookies) {
            try {
                Queue<Cookie> cookies = REplican.cookies.getCookiesForUrl(new URL(url));
                for (Cookie cookie: cookies) {
                    c += cookie.getCookieString();
                }
            } catch (MalformedURLException MUE) {
                logger.throwing(MUE);
                return;
            }
        }

        if (c != null) {
            urlConnection.setRequestProperty("Cookie", c);
        }
    }

    private void addHeaderLines() {
        if (REplican.args.Header != null) {
            for (String header: REplican.args.Header) {
                String s[] = header.split(":");
                if (s[0] != null && s[1] != null) {
                    urlConnection.setRequestProperty(s[0], s[1]);
                } else {
                    logger.trace("Couldn't decipher " + header);
                }
            }
        }
    }

    private InputStream getURLInputStream() {
        final int returnCode;

        try {
            returnCode = connect();
        } catch (IOException e) {
            logger.warn(e);
            return (null);
        }

        logger.trace(urlConnection.toString());
        logger.trace(urlConnection.getHeaderFields().toString());

        return (dealWithReturnCode(returnCode));
    }

    private void getCookies() {
        if (!REplican.args.IgnoreCookies) {
            Map<String, List<String>> m = urlConnection.getHeaderFields();
            List<String> list = m.get("Set-Cookie");
            if (list != null) {
                for (String cookie : list) {
                    logger.trace("Adding cookie: " + cookie);
                    try {
                        cookies.addCookie(new URL(url), cookie);
                    } catch (MalformedURLException MUE) {
                        logger.throwing(MUE);
                    }
                }
            }
        }
    }

    /**
     * get an InputStream from either a file: or http: URL.  deals
     * with http redirections.
     */
    private InputStream createInputStream() {
        // http://httpd.apache.org/docs/1.3/mod/mod_dir.html#directoryindex

        logger.traceEntry(url);
        InputStream is = getURLInputStream();
        logger.traceExit(is);
        return (is);
    }
}
