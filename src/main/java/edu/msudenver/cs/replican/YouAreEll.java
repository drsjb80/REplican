package edu.msudenver.cs.replican;

import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class YouAreEll {
    private final Logger logger;
    private HttpConnection httpConnection;
    private String ContentType;
    private final String url;
    private final Cookies cookies;
    private final ConfigProvider config;
    private final HttpConnectionFactory connectionFactory;

    // Legacy constructors for backward compatibility
    public YouAreEll(final String url) {
        this(url, null);
    }

    public YouAreEll(final String url, Cookies cookies) {
        this(url, cookies, new REplicanConfigProvider(REplican.ARGS),
             new StandardHttpConnectionFactory(), LogManager.getLogger(YouAreEll.class));
    }

    // New constructor with full dependency injection
    public YouAreEll(@NonNull final String url,
                     Cookies cookies,
                     @NonNull ConfigProvider config,
                     @NonNull HttpConnectionFactory connectionFactory,
                     @NonNull Logger logger) {
        this.url = url;
        this.cookies = cookies;
        this.config = config;
        this.connectionFactory = connectionFactory;
        this.logger = logger;
    }

    public String getContentType() {
        return ContentType;
    }

    public String getUrl() {
        return url;
    }

    int getContentLength() {
        if (httpConnection == null) {
            return 0;
        }
        String cl = httpConnection.getHeaderField("Content-Length");
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
        if (httpConnection == null) {
            return 0;
        }
        return (httpConnection.getLastModified());
    }

    private void dealWithRedirects() {
        if (!config.isFollowRedirects()) {
            return;
        }

        String redirected = httpConnection.getHeaderField("Location");
        if (config.isPrintRedirects() && redirected != null) {
            logger.info("Redirected to: " + redirected);
        }
    }

    private void dealWithStopOns(final int code) {
        int[] stopCodes = config.getStopOnStatusCodes();
        if (stopCodes != null) {
            for (int stopOn : stopCodes) {
                if (code == stopOn) {
                    logger.warn("Stopping on return code: " + code);
                    System.exit(code);
                }
            }
        }
    }

    private InputStream HUC() {
        ContentType = httpConnection.getHeaderField("Content-Type");
        String[] MIMEAccept = config.getMIMEAccept();
        String[] MIMEReject = config.getMIMEReject();

        // here, if MIME has no input on the matter, assume Path has
        // already spoken so return true from blurf.
        if (!Utils.blurf(MIMEAccept, MIMEReject, ContentType, true))
            return (null);

        try {
            return (httpConnection.getInputStream());
        } catch (IOException e) {
            logger.throwing(e);
            return (null);
        }
    }

    private InputStream dealWithReturnCode(int code) {
        logger.traceEntry(Integer.toString(code));

        int[] stopCodes = config.getStopOnStatusCodes();
        if (stopCodes != null && stopCodes.length > 0)
            dealWithStopOns(code);

        switch (code) {
            case HttpConnection.HTTP_OK:
                return (HUC());
            case HttpConnection.HTTP_MOVED_PERM:
            case HttpConnection.HTTP_MOVED_TEMP:
                dealWithRedirects();
                return (null);
            default: {
                try {
                    String message = "";
                    try {
                        message = httpConnection.getResponseMessage();
                    } catch (IOException e) {
                        // Ignore
                    }
                    logger.warn("For: " + url + " server returned: " +
                            code + " " + message);
                } catch (Exception e) {
                    logger.throwing(e);
                }

                return (null);
            }
        }
    }

    private int connect() throws IOException {
        httpConnection = connectionFactory.createConnection(url);
        addHeaderLines();
        setCookies();

        int returnCode = httpConnection.getResponseCode();

        httpConnection.connect();

        logger.traceExit(returnCode);
        return returnCode;
    }

    private void setCookies() {
        if (cookies == null || config.isIgnoreCookies()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        try {
            java.util.Queue<Cookie> cookieQueue = cookies.getCookiesForUrl(new URL(url));
            for (Cookie cookie : cookieQueue) {
                sb.append(cookie.getCookieString());
            }
        } catch (MalformedURLException e) {
            logger.throwing(e);
            return;
        }

        if (sb.length() > 0) {
            httpConnection.setRequestProperty("Cookie", sb.toString());
        }
    }

    private void addHeaderLines() {
        String[] headers = config.getHeader();
        if (headers != null) {
            for (String header : headers) {
                String[] s = header.split(":", 2);
                if (s.length == 2) {
                    httpConnection.setRequestProperty(s[0].trim(), s[1].trim());
                } else {
                    logger.trace("Malformed header (no colon): " + header);
                }
            }
        }
    }

    InputStream getInputStream() throws IOException {
        final int returnCode = connect();

        logger.trace(httpConnection.toString());
        logger.trace(httpConnection.getHeaderFields().toString());

        return (dealWithReturnCode(returnCode));
    }

}
