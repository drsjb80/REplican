package edu.msudenver.cs.replican;

import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.io.*;

import java.util.Map;
import java.util.List;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class YouAreEll {
    private final Logger logger = LogManager.getLogger(getClass());
    private URLConnection urlConnection;
    private String ContentType;
    private final String url;
    private Cookies cookies;

    public YouAreEll(final String url) {
        this.url = url;
        this.cookies = null;
    }

    public YouAreEll(final String url, Cookies cookies) {
        this.url = url;
        this.cookies = cookies;
    }

    public String getContentType() {
        return ContentType;
    }

    public String getUrl() {
        return url;
    }

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

    long getLastModified() {
        return (urlConnection.getLastModified());
    }

    private void dealWithRedirects() {
        if (!REplican.ARGS.FollowRedirects) {
            return;
        }

        String redirected = urlConnection.getHeaderField("Location");
        if (REplican.ARGS.PrintRedirects && redirected != null) {
            logger.info("Redirected to: " + redirected);
        }
    }

    private void dealWithStopOns(final int code) {
        for (int stopOn: REplican.ARGS.StopOn) {
            if (code == stopOn) {
                logger.warn("Stopping on return code: " + code);
                System.exit(code);
            }
        }
    }

    private InputStream HUC() {
        ContentType = urlConnection.getHeaderField("Content-Type");
        String[] MIMEAccept = REplican.ARGS.MIMEAccept;
        String[] MIMEReject = REplican.ARGS.MIMEReject;

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

        if (REplican.ARGS.StopOn.length > 0)
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

    private int connect() throws IOException {
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
        if (cookies == null || REplican.ARGS.IgnoreCookies) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        try {
            Queue<Cookie> cookieQueue = cookies.getCookiesForUrl(new URL(url));
            for (Cookie cookie : cookieQueue) {
                sb.append(cookie.getCookieString());
            }
        } catch (MalformedURLException e) {
            logger.throwing(e);
            return;
        }

        if (sb.length() > 0) {
            urlConnection.setRequestProperty("Cookie", sb.toString());
        }
    }

    private void addHeaderLines() {
        if (REplican.ARGS.Header != null) {
            for (String header: REplican.ARGS.Header) {
                String[] s = header.split(":", 2);
                if (s.length == 2) {
                    urlConnection.setRequestProperty(s[0].trim(), s[1].trim());
                } else {
                    logger.trace("Malformed header (no colon): " + header);
                }
            }
        }
    }

    InputStream getInputStream() throws IOException {
        final int returnCode = connect();

        logger.trace(urlConnection.toString());
        logger.trace(urlConnection.getHeaderFields().toString());

        return (dealWithReturnCode(returnCode));
    }

}
