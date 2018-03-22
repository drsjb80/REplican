package edu.msudenver.cs.replican;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

// Newest cookie specification https://tools.ietf.org/html/rfc6265

// Cookies revolve around domains and paths, not URLs.
@ToString
@EqualsAndHashCode
class Cookie {
    private static final Date now = new Date();
    private static final Date BEGINNINGOFTIME = new Date(0);
    private final Logger logger = LogManager.getLogger(getClass());
    @Getter private final AbstractMap<String, String> keyValuePairs = new ConcurrentHashMap<>();
    @Getter private boolean secure;
    @Getter private boolean httponly;

    // host comes in via the URL, not used unless there is no domain
    // a path comes in via the URL, not used unless there is no path
    @Getter private String URLHost = null;
    @Getter private String URLPath = null;

    private Date maxAge = BEGINNINGOFTIME;
    long getMaxTime() {
        return maxAge.getTime();
    }

    private String path;
    // if path hasn't been set by a cookie, use the URLPath. if that hasn't been
    // set, return a slash.
    String getPath() {
        if (this.path == null || this.path.equals(""))  {
            if (this.URLPath == null || this.URLPath.equals("")) {
                return "/";
            }
            return URLPath;
        }

        return this.path;
    }

    private String domain;
    String getDomain() {
        if (this.domain == null) {
            return this.URLHost;
        }
        return this.domain;
    }

    // used from e.g.: FirefoxCookies
    Cookie(@NonNull final String URLHost, @NonNull final String domain, @NonNull final String path,
           final long maxAge, final boolean secure,
           @NonNull final String key, @NonNull final String value) {
        this.URLHost = URLHost;
        this.domain = domain;
        this.path = path;
        this.maxAge = new Date(maxAge);
        this.secure = secure;
        keyValuePairs.put(key, value);
    }

    Cookie(@NonNull final String URLHost, @NonNull final String URLPath,
           @NonNull final String cookieString) throws IllegalArgumentException {
        this.URLHost = URLHost;
        setURLPath(URLPath);
        addCookieString(cookieString);
    }

    private void setURLPath(@NonNull final String URLPath) {
        if (URLPath.equals("")) {
            this.URLPath = "/";
        } else {
            this.URLPath = URLPath;
        }
    }

    void addCookieString(@NonNull final String cookieString) {
        final String[] b = cookieString.split(";");

        for (String pair: b) {
            final String[] c = pair.split("=", 2);
            final String key = c[0].trim();
            String value = "";

            // are there both a key and a value?
            if (c.length == 2) {
                value = c[1].trim();
            }

            setKeyValue(key, value);
        }
    }

    void addCookie(@NonNull final Cookie cookie) {
        assert cookie.getPath().equals(this.getPath());
        assert cookie.getDomain().equals(this.getDomain());
        for (AbstractMap.Entry<String, String> entry : cookie.keyValuePairs.entrySet()) {
            setKeyValue(entry.getKey(), entry.getValue());
        }
    }

    /*
**  Check to see if this is one of the usual cookie values and set it
**  appropriately if so. see: https://tools.ietf.org/html/rfc6265#section-5.2.2
*/
    private void setKeyValue(@NonNull final String key, @NonNull final String value) {
        logger.traceEntry(key);
        logger.traceEntry(value);

        switch (key.toLowerCase()) {
            case "max-age": setMaxAge(value); break;
            case "expires": setExpiry(value); break;
            case "domain": setDomain(value); break;
            case "path": setPath(value); break;
            case "secure": secure = true; break;
            case "httponly": httponly = true; break;
            default: keyValuePairs.put(key, value); break;
        }
    }

    private void setMaxAge (@NonNull final String value) {
        final long seconds = Long.parseLong(value);

        // reset; should check for this in getCookie*
        if (seconds <= 0) {
            maxAge = BEGINNINGOFTIME;
            return;
        }

        Date newMaxAge = new Date(now.getTime() + seconds);

        // allow extensions to the time
        if (maxAge.before(newMaxAge)) {
            maxAge = newMaxAge;
        }
    }

    private void setExpiry (@NonNull final String value) {
        Date date = null;

        // try it both ways; if neither work, give up. go with the newest on we see.
        try {
            maxAge = new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss zzz").parse(value);
        } catch (ParseException PE1) {
            try {
                maxAge = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss zzz").parse(value);
            } catch (ParseException PE2) {
                return;
            }
        }
    }

    private void setDomain(@NonNull String domain) {
        if (domain.startsWith(".")) {
            domain = domain.replaceFirst("\\.", "");
        }

        if (domain.indexOf(".", 1) == -1) {
            throw new IllegalArgumentException(domain + " does not have embedded dots");
        }

        if (this.domain != null && !this.domain.equals(domain)) {
            throw new IllegalArgumentException(this.domain + " != " + domain);
        }

        if (! this.URLHost.endsWith(domain)) {
            throw new IllegalArgumentException(this.URLHost + " does not end with "
                    + domain);
        }

        this.domain = domain;
    }

    private void setPath(@NonNull final String path) {
        assert path.startsWith("/");
        assert this.URLPath.startsWith(path);

        this.path = path;
    }

    public String getCookieString() {
        String ret = "Cookie: ";
        boolean first = true;

        for (String key : keyValuePairs.keySet()) {
            ret += (first ? "" : "; ") + key;

            final String value = keyValuePairs.get(key);

            // booleans such as secure don't have values...
            if (!"".equals(value)) {
                ret += "=" + value;
            }

            first = false;
        }

        logger.traceExit(ret);
        return ret;
    }
}
