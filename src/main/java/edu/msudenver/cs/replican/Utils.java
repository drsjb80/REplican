package edu.msudenver.cs.replican;

import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final Logger logger = LogManager.getLogger("edu.msudenver.cs.replican.Utils");

    /**
     * make a String pattern of, e.g.: [Aa][Bb], from a string, ignoring
     * non-alpha characters.
     *
     * @param s the initial string
     * @return the initial string with the appropriate brackets
     */
    static String both(String s) {
        logger.traceEntry(s);
        String r = "";

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (Character.isLetter(c)) {
                r += "[" + Character.toLowerCase(c) +
                        Character.toUpperCase(c) + "]";
            } else {
                r += c;
            }
        }

        logger.traceExit(r);
        return (r);
    }

    /**
     * Check if a string matches one of an array of regular
     * expressions.  The string must not have the dots escaped, while
     * the regular expression must.
     *
     * @param s  the string to check
     * @param re the array of regular expressions
     */
    private static boolean matches(String[] re, String s) {
        logger.traceEntry("re" + java.util.Arrays.toString(re));
        logger.traceEntry(s);

        for (String value : re) {
            logger.trace("Matching: '" + s + "' and '" + value + "'");
            try {
                if (s.matches(value)) {
                    logger.traceExit("true");
                    return (true);
                }
            } catch (java.util.regex.PatternSyntaxException PSE) {
                logger.warn(PSE);
            }
        }

        logger.traceExit("false");
        return (false);
    }
    
    /*
    ** Use the array pairs as pattern and replacement pairs.  E.g.:
    ** pairs[0] == "\\.wmv.*" and pairs[1] == ".wmv"
    */
    static String replaceAll(String s, String[] pairs) {
        logger.traceEntry(s);
        logger.traceEntry(Arrays.toString(pairs));

        if (pairs == null)
            return (s);

        if ((pairs.length % 2) != 0) {
            logger.error("pairs not even");
            logger.traceExit(s);
            return (s);
        }

        for (int i = 0; i < pairs.length; i += 2) {
            s = s.replaceAll(pairs[i], pairs[i + 1]);
        }

        logger.traceExit(s);
        return (s);
    }


    /**
     * Match a string first against an accepting condition; if that
     * matches, check against a rejecting condition.
     *
     * @param yes an array of accepting regular expressions
     * @param no  an array of rejecting regular expressions
     * @param s   the string to compare
     */
    static boolean blurf(String[] yes, String[] no, String s,
                         boolean ifBothNull) {
        if (yes != null) logger.trace("yes" + java.util.Arrays.toString(yes));
        if (no != null) logger.trace("no" + java.util.Arrays.toString(no));
        if (s != null) logger.trace(s);

        if (yes == null && no == null) {
            return (ifBothNull);
        }

        if (yes != null && no != null) {
            boolean ret = matches(yes, s) && !matches(no, s);
            logger.traceExit(ret);
            return (ret);
        } else if (yes == null) {
            boolean ret = !matches(no, s);
            logger.traceExit(ret);
            return (ret);
        } else { // no == null
            boolean ret = matches(yes, s);
            logger.traceExit(ret);
            return (ret);
        }
    }

    static String hostToDomain(String host) {
        return host.replaceFirst("[^.]+.", "");
    }

    //combining two String arrays
    static String[] combineArrays(String[] one, String[] two) {
        if (one == null && two == null) {
            return (null);
        }
        if (two == null) {
            return (one);
        }
        if (one == null) {
            return (two);
        }

        String[] ret = new String[one.length + two.length];
        System.arraycopy(one, 0, ret, 0, one.length);
        System.arraycopy(two, 0, ret, one.length, two.length);

        return (ret);
    }

    static void snooze(int milliseconds) {
        logger.traceEntry(Integer.toString(milliseconds));

        if (milliseconds == 0)
            return;

        logger.info("Sleeping for " + milliseconds + " milliseconds");

        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ie) {
            logger.throwing(ie);
        }
    }

    static String speed(long start, long stop, long read) {
        long seconds = (stop - start) / 1000;
        long BPS = read / (seconds == 0 ? 1 : seconds);

        if (BPS > 1000000000000000L)
            return (BPS / 1000000000000000L + " EBps");
        else if (BPS > 1000000000000L)
            return (BPS / 1000000000000L + " TBps");
        else if (BPS > 1000000000)
            return (BPS / 1000000000 + " GBps");
        else if (BPS > 1000000)
            return (BPS / 1000000 + " MBps");
        else if (BPS > 1000)
            return (BPS / 1000 + " KBps");
        else
            return (BPS + " Bps");
    }


    /*
     ** In the given string s, look for pattern.  If found, return the
     ** concatenation of the capturing groups.
     */
    private static String match(String pattern, String s) {
        logger.traceEntry(pattern);
        logger.traceEntry(s);

        String ret = null;

        Matcher matcher = Pattern.compile(pattern).matcher(s);
        if (matcher.find()) {
            ret = "";
            for (int i = 1; i <= matcher.groupCount(); i++) {
                ret += (matcher.group(i));
            }
        }

        logger.traceExit(ret);
        return (ret);
    }

    /*
    ** http://www.w3schools.com/tags/tag_base.asp
    ** "The <base> tag specifies the base URL/target for all relative URLs
    ** in a document.
    ** The <base> tag goes inside the <head> element."
    */
    static String newBase(String base) {
        logger.traceEntry(base);

        if (base == null || base.equals("")) {
            return (null);
        }

        String b = "<[bB][aA][sS][eE].*[hH][rR][eE][fF]=[\"']?([^\"'# ]*)";
        String ret = match(b, base);

        logger.traceExit(ret);
        return (ret);
    }

    /**
     * look for "interesting" parts of a HTML string.  interesting thus far
     * means href's, src's, img's etc.
     *
     * @param s the string to examine
     * @return the interesting part if any, and null if none
     */
    static String[] interesting(@NonNull String s) {
        logger.traceEntry(s);

        String[] m = new String[REplican.args.Interesting.length];

        for (int i = 0; i < REplican.args.Interesting.length; i++) {
            m[i] = match(REplican.args.Interesting[i], s);
        }

        return (m);
    }
}
