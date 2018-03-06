package edu.msudenver.cs.replican;

import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/*
http://www.faqs.org/rfcs/rfc2109.html

set-cookie      =       "Set-Cookie:" cookies
   cookies         =       1#cookie
   cookie          =       NAME "=" VALUE *(";" cookie-av)
   NAME            =       attr
   VALUE           =       value
   cookie-av       =       "Comment" "=" value
                   |       "Domain" "=" value
                   |       "MaxAge" "=" value
                   |       "Path" "=" value
                   |       "Secure"
                   |       "Version" "=" 1*DIGIT
*/

public class Cookies {
    // THREADSAFE_LEVEL_GREY
    private final Map<String, Vector<Cookie>> cookies = new HashMap<>();
    private final Logger logger = REplican.logger;

    public void addCookie(final String hostORdomain, final String path,
                          final String cookieString) {
        // THREADSAFE_LEVEL_GREY
        final String hostAndPath = hostORdomain + path;
        final Cookie newCookie = new Cookie(hostORdomain, path, cookieString);

        if (cookies.containsKey(hostAndPath)) {
            cookies.get(hostAndPath).add(newCookie);
        } else {
            final Vector v = new Vector();
            v.add(newCookie);
            cookies.put(hostAndPath, v);
        }
    }

    public void addCookie(final URL url, final String cookieString) {
        //THREADSAFE_LEVEL_GREY
        String host = url.getHost();

        if ("".equals(host)) {
            host = "localhost";  // only useful for httpfile://
        }
        // THREADSAFE_LEVEL_GREY
        final String path = url.getPath();

        addCookie(host, path, cookieString);
    }

    public void loadSQLCookies(final String file) {
        // /Users/beatys/Library/Application Support/Firefox/Profiles/rnwwcxjq.default/cookies.sqlite
        // THREADSAFE_LEVEL_GREY
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + file);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return;
        }
        // THREADSAFE_LEVEL_GREY
        final String sql = "SELECT * FROM moz_cookies";

        try {
            // THREADSAFE_LEVEL_GREY
            final Statement stmt = conn.createStatement();
            final ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                final String host = rs.getString("host");
                final String path = rs.getString("path");
                final String domain = rs.getString("baseDomain");
                final String name = rs.getString("name");
                final String value = rs.getString("value");
                final int expiry = rs.getInt("expiry");
                final boolean isSecure = rs.getInt("isSecure") != 0;

                final Cookie cookie = new Cookie(domain, path, expiry, isSecure, name, value);
                addCookie(domain, path, cookie.getCookieString());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /*
    DOMAIN - The domain that created AND that can read the variable.
    FLAG - A TRUE/FALSE value indicating if all machines within a given domain
        can access the variable. This value is set automatically by the browser,
        depending on the value you set for domain.
    PATH - The path within the domain that the variable is valid for.
    SECURE - A TRUE/FALSE value indicating if a secure connection with the
        domain is needed to access the variable.
    EXPIRATION - The UNIX time that the variable will expire on. UNIX time is
        defined as the number of seconds since Jan 1, 1970 00:00:00 GMT.
    NAME - The name of the variable.
    VALUE - The value of the variable.

    separated by TABS
    */
    private void doNetscapeLine(final String line) {
        // THREADSAFE_LEVEL_GREY
        logger.traceEntry(line);

        if (line.charAt(0) == '#') {
            return;
        }
        // THREADSAFE_LEVEL_GREY
        final String[] s = line.split("\t");

        if (s.length > 6) {
            // THREADSAFE_LEVEL_GREY
            final Date date = new Date(Long.parseLong(s[4]) * 1000);
            final String expires =
                    new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss zzz").
                            format(date);

            addCookie(s[0], s[2], s[5] + "=" + s[6]
                    + "; Expires=" + expires);
        } else {
            addCookie(s[0], s[2], s[5]);
        }
    }

    // THREADSAFE_LEVEL_GREY
    public void loadNetscapeCookies(final String file) {
        if (file.endsWith(".sqlite")) {
            // THREADSAFE_LEVEL_GREY
            loadSQLCookies(file);
        } else {
            BufferedReader in = null;

            try {
                in = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException fnfe) {
                logger.warn("File not found: " + file);
                return;
            }

            String line;

            try {
                while ((line = in.readLine()) != null) {
                    if (line.length() > 0) {
                        doNetscapeLine(line);
                    }
                }
            } catch (IOException ioe) {
                logger.throwing(ioe);
            }
        }
    }

    // THREADSAFE_LEVEL_GREY
    public void saveNetscapeCookies(String filename) {
        logger.traceEntry(filename);

        try {
            FileWriter fw = new FileWriter(filename);

            fw.write("# HTTP Cookie File\n");
            fw.write("# This is a generated file!  Do not edit.\n");

            logger.trace("" + cookies.size());

            for (String hostAndDomain: cookies.keySet()) {
                for (Cookie cookie: cookies.get(hostAndDomain)) {
                    fw.write(cookie.getSave());
                }
            }

            fw.close();
        } catch (IOException E) {
            logger.throwing(E);
        }
    }

    // THREADSAFE_LEVEL_GREY
    public String getCookieStringsForURL(final URL url) {
        String ret = null;
        final String hostAndPath = url.getHost() + url.getPath();

        if (! cookies.containsKey(hostAndPath)) {
            throw new IllegalArgumentException("No cookie for: " + url);
        }

        for (Cookie cookie: cookies.get(hostAndPath)) {
            if (ret == null) {
                ret = cookie.getCookieString();
            } else {
                ret += "; " + cookie.getCookieString();
            }
        }

        return ret;
    }
}