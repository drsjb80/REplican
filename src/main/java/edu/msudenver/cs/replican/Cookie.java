package edu.msudenver.cs.replican;

import lombok.Getter;
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
class Cookie {
    private static final Date now = new Date();
    private static final Date BEGINNINGOFTIME = new Date(0);

    private static final long serialVersionUID = 1L;
    private final Logger logger = REplican.logger;
    // THREADSAFE_LEVEL_GREY
    @Getter private String domain;
    // THREADSAFE_LEVEL_GREY
    @Getter private String path;
    // THREADSAFE_LEVEL_GREY
    private Date maxAge = BEGINNINGOFTIME;
    long getMaxAge() { return maxAge.getTime(); }
    @Getter private boolean secure;
    @Getter private boolean httponly;
    /*
    hash domains -> paths
    hash paths -> keyValuePairs
     */

    Cookie getCookie(final String domain, final String path) {
        for (String d: domains.keySet()) {
            if (d.endsWith(domain)) {
                AbstractMap<String, AbstractMap> paths = domains.get(d);
                for (String p: paths.keySet()) {
                    if (p.endsWith(path)) {
                        // do something here?
                    }
                }
            }
        }
        return null;
    }
    private final AbstractMap<String, String> keyValuePairs = new ConcurrentHashMap<>();
    private final AbstractMap<String, AbstractMap> paths = new ConcurrentHashMap<>();
    private final AbstractMap<String, AbstractMap> domains = new ConcurrentHashMap<>();

    // THREADSAFE_LEVEL_GREY
    // needs locking?
    Cookie(final String domain, final String path, final long maxAge,
           final boolean secure, final String key, final String value) {
        setDomainAndPath(domain, path);
        this.maxAge = new Date(maxAge);
        this.secure = secure;
        keyValuePairs.put(key, value);
    }

    // THREADSAFE_LEVEL_GREY
    // needs locking?
    private void setDomainAndPath(String domain, final String path) {
        setDomain(domain);
        setPath(path);
    }

    //THREADSAFE_LEVEL_GREY
    // needs locking?
    private void setDomain(String domain)
    {
        /*
        if (domain.startsWith(".")) {
            domain = domain.replaceFirst("\\.", "");
        }

        AbstractMap d = domains.get(domain);
        if (d == null) {
            AbstractMap <String, AbstractMap> paths = new ConcurrentHashMap<>();
            d.put(domain, paths);
        }

        if (paths == null) {
            AbstractMap <String, AbstractMap> paths = new ConcurrentHashMap<>();
        }

        // FIXME: what should be done when additions don't agree with orginals?
        if (this.domain != null) {
            if (! domain.equals(this.domain)) {
                throw (new IllegalArgumentException("Attempted cookie domain reset from: "
                        + this.domain + " to: " + domain));
            }
        }

        checkDomain(domain);
        this.domain = domain;
        */
    }

    // THREADSAFE_LEVEL_BLACK
    // sets global variable, needs locking..?
    private void setPath (String path) {
        if (this.path != null) {
            if (! this.path.equals(path)) {
                throw (new IllegalArgumentException("Attempted cookie path reset from: "
                        + this.path + " to: " + path));
            }
        }

        if (! path.startsWith("/")) {
            throw (new IllegalArgumentException("Path: " + path + "does not begin with /"));
        }

        this.path = path;
    }

    // THREADSAFE_LEVEL_GREY
    // set global, needs locking
    public Cookie(String domain, final String path, final String cookieString)
            throws IllegalArgumentException {
        setDomainAndPath(domain, path);
        addToCookie(cookieString);
    }

    // THREADSAFE_LEVEL_GREY
    // string splitting may not be thread safe
     void addToCookie(final String cookieString) {
        logger.traceEntry(cookieString);

        final String[] b = cookieString.split(";");

        for (String pair: b) {
            final String[] c = pair.split("=", 2);
            final String key = c[0].trim();
            String value = "";

            // are there both a key and a value?
            if (c.length == 2) {
                value = c[1].trim();
            }

            if (! setIfRFCKey(key, value)) {
                keyValuePairs.put(key, value);
            }
        }
    }

	/*
   The value for the path attribute is not a prefix of the request-
   URI.

   The value for the domain attribute contains no embedded dots or
   does not start with a dot.

   The value for the request-host does not domain-match the domain
   attribute.

   The request-host is a FQDN (not IP address) and has the form HD,
   where D is the value of the domain attribute, and H is a string
   that contains one or more dots.
	 */

    /*
	    called if soemthing goes wrong when parsing entire line.
	*/
    // THREADSAFE_LEVEL_BLACK
    // needs to aquire a lock on global vars
    private void reset() {
        domain = null;
        path = null;
        maxAge = BEGINNINGOFTIME;
        secure = false;
    }

    private void checkDomain(final String domain) {
        if (domain.indexOf(".", 1) == -1) {
            throw new IllegalArgumentException(domain + " does not have embedded dots");
        }
    }

    private void checkHost(String host) {
        if (!host.endsWith(this.domain)) {
            throw new IllegalArgumentException(host
                    + " does not end with " + this.domain);
        }

        final String tmpHost = host.replaceFirst("\\..*", "") + domain;
        if (!tmpHost.equals(host)) {
            throw new IllegalArgumentException(host
                    + " is longer than " + domain);
        }
    }

    /*
    **  Check to see if this is one of the usual cookie values and set it
    **  appropriately if so. see: https://tools.ietf.org/html/rfc6265#section-5.2.2
    */
    // THREADSAFE_LEVEL_GREY
    private boolean setIfRFCKey(final String key, final String value) {
        logger.traceEntry(key);
        logger.traceEntry(value);

        switch (key.toLowerCase()) {
            case "max-age":
                setMaxAge(value);
                return true;
            case "expires":
                setExpiry(value);
                return true;
            case ("domain"):
                setDomain(value);
                return true;
            case ("path"):
                setPath(value);
                return true;
            case ("secure"):
                secure = true;
                return true;
            case ("httponly"):
                httponly = true;
                return true;
        }

        return false;
    }

    // THREADSAFE_LEVEL_BLACK
    // needs lock aquire
    private void setMaxAge (final String value) {
        // FIXME: go with whatever we see most recently. i'm not sure this is the best
        // approach, but i'm not sure whether using newer or older dates is correct. same
        // expiry below.
        try {
            maxAge = new Date(now.getTime() + Long.parseLong(value));
        } catch (NumberFormatException NFE) {
            maxAge = BEGINNINGOFTIME;
        }
    }


    // THREADSAFE_LEVEL_BLACK
    // needs lock aquire
    private void setExpiry (final String value) {
        Date d = null;
        try {
            d = new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss zzz").parse(value);
        } catch (ParseException PE1) {
            try {
                d = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss zzz").parse(value);
            } catch (ParseException PE2) {
                maxAge = BEGINNINGOFTIME;
                return;
            }
        }

        maxAge = d;
    }

    /**
     * Get a string of the correct form to save in a Netscape file.
     *
     * @return the formatted string
     */
    // THREADSAFE_LEVEL_BLACK
    // critical section, string building
    public String getSave() {
        if (now.after(maxAge)) {
            logger.debug(this.toString() + " has expired");
            return (null);
        }

        String ret = "";

        for (String key : keyValuePairs.keySet()) {

            ret += domain + "\t";
            ret += domain.startsWith(".") ? "TRUE\t" : "FALSE\t";
            ret += path + "\t";
            ret += secure ? "TRUE\t" : "FALSE\t";
            ret += maxAge + "\t";

            final String value = keyValuePairs.get(key);
            if (value != null) {
                ret += key + "\t" + value + "\n";
            } else {
                ret += key + "\n";
            }
        }

        logger.traceExit(ret);
        return ret;
    }

    // THREADSAFE_LEVEL_BLACK
    // critical section, ret
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

    // THREADSAFE_LEVEL_BLACK
    // critical section, string building
    public String toString() {
        return "domain = " + domain
                + ", path = " + path
                + ", maxAge = " + maxAge
                + ", secure = " + secure
                + ", getCookieString() = " + getCookieString();
    }
}
