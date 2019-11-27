package edu.msudenver.cs.replican;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NetscapeCookies {
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
    static private void doNetscapeLine(final String line) {
        if (line.charAt(0) == '#') {
            return;
        }

        final String[] s = line.split("\t");

        if (s.length > 6) {
            final Date date = new Date(Long.parseLong(s[4]) * 1000);
            final String expires =
                    new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss zzz").
                            format(date);

            REplican.COOKIES.addCookie(s[0], s[2], s[5] + "=" + s[6]
                    + "; Expires=" + expires);
        } else {
            REplican.COOKIES.addCookie(s[0], s[2], s[5]);
        }
    }

    static void loadCookies(final String file) throws IOException {
        final BufferedReader in = new BufferedReader(new FileReader(file));

        String line;
        while ((line = in.readLine()) != null) {
            if (line.length() > 0) {
                doNetscapeLine(line);
            }
        }
    }

    static void saveCookies(String filename) throws IOException {
        FileWriter fw = new FileWriter(filename);

        fw.write("# HTTP Cookie File\n");
        fw.write("# This is a generated file!  Do not edit.\n");

        // FIXME
        /*
        for (Cookie cookie: REplican.cookies.getAllCookies()) {
            fw.write(getSave(cookie));
        }
        */

        fw.close();
    }

    /**
     * Get a string of the correct form to save in a Netscape file.
     *
     * @return the formatted string
     */
    static String getSave(final Cookie cookie) {
        final Date now = new Date();
        /* FIXME
        if (now.after(cookie.getMaxAge())) {
            return "";
        }
        */

        String ret = "";
        ret += cookie.getDomain() + "\t";
        ret += cookie.getDomain().startsWith(".") ? "TRUE\t" : "FALSE\t";
        ret += cookie.getPath() + "\t";
        ret += cookie.isSecure() ? "TRUE\t" : "FALSE\t";
        // FIXME ret += cookie.getMaxTime() + "\t";

        /* FIXME
        for (String key : cookie.getKeyValuePairs().keySet()) {
            final String value = cookie.getKeyValuePairs().get(key);
            if (value != null) {
                ret += key + "\t" + value + "\n";
            } else {
                ret += key + "\n";
            }
        }
        */

        return ret;
    }

    /*
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
    */
}