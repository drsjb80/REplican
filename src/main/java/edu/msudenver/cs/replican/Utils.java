package edu.msudenver.cs.replican;

import lombok.NonNull;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final Logger LOGGER = REplican.LOGGER;

    /**
     * Check if a string matches one of an array of regular
     * expressions.  The string must not have the dots escaped, while
     * the regular expression must.
     *
     * @param s  the string to check
     * @param re the array of regular expressions
     */
    private static boolean matches(String[] re, String s) {
        LOGGER.traceEntry("re" + java.util.Arrays.toString(re));
        LOGGER.traceEntry(s);

        for (String value : re) {
            LOGGER.trace("Matching: '" + s + "' and '" + value + "'");
            try {
                if (s.matches(value)) {
                    LOGGER.traceExit("true");
                    return (true);
                }
            } catch (java.util.regex.PatternSyntaxException PSE) {
                LOGGER.warn(PSE);
            }
        }

        LOGGER.traceExit("false");
        return (false);
    }

    /*
     ** Use the array pairs as pattern and replacement pairs.  E.g.:
     ** pairs[0] == "\\.wmv.*" and pairs[1] == ".wmv"
     */
    static String replaceAll(@NonNull final String string, @NonNull final String[] pairs) {
        LOGGER.traceEntry(string);
        LOGGER.traceEntry(Arrays.toString(pairs));

        if ((pairs.length % 2) != 0) {
            LOGGER.error("pairs not even");
            LOGGER.traceExit(string);
            return (string);
        }

        String ret = string;

        for (int i = 0; i < pairs.length; i += 2) {
            ret = ret.replaceAll(pairs[i], pairs[i + 1]);
        }

        LOGGER.traceExit(ret);
        return ret;
    }


    /**
     * Match a string first against an accepting condition; if that
     * matches, check against a rejecting condition.
     *
     * @param acceptRE an array of accepting regular expressions
     * @param rejectRE an array of rejecting regular expressions
     * @param string   the string to compare
     */
    static boolean blurf(final String[] acceptRE, final String[] rejectRE, final String string, final boolean ifBothNull) {
        if (acceptRE != null) {
            LOGGER.trace("acceptRE" + Arrays.toString(acceptRE));
        }
        if (rejectRE != null) {
            LOGGER.trace("rejectRE" + Arrays.toString(rejectRE));
        }
        if (string != null) {
            LOGGER.trace(string);
        }

        if (acceptRE == null && rejectRE == null) {
            return (ifBothNull);
        }

        if (acceptRE != null && rejectRE != null) {
            boolean ret = matches(acceptRE, string) && !matches(rejectRE, string);
            LOGGER.traceExit(ret);
            return (ret);
        } else if (acceptRE == null) {
            boolean ret = !matches(rejectRE, string);
            LOGGER.traceExit(ret);
            return (ret);
        } else { // rejectRE == null
            boolean ret = matches(acceptRE, string);
            LOGGER.traceExit(ret);
            return (ret);
        }
    }

    static String hostToDomain(@NonNull final String host) {
        return host.replaceFirst("[^.]+.", "");
    }

    //combining two String arrays
    static String[] combineArrays(final String[] one, final String[] two) {
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
        LOGGER.traceEntry(Integer.toString(milliseconds));

        if (milliseconds == 0)
            return;

        LOGGER.info("Sleeping for " + milliseconds + " milliseconds");

        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ie) {
            LOGGER.throwing(ie);
        }
    }

    static String speed(long start, long stop, long read) {
        long seconds = (stop - start) / 1000;
        long BPS = read / (seconds == 0 ? 1 : seconds);

        if (BPS > 1000000000000000L) {
            return BPS / 1000000000000000L + " EBps";
        } else if (BPS > 1000000000000L) {
            return BPS / 1000000000000L + " TBps";
        } else if (BPS > 1000000000) {
            return BPS / 1000000000 + " GBps";
        } else if (BPS >1000000) {
            return BPS /1000000+" MBps";
        } else if (BPS > 1000) {
            return BPS / 1000 + " KBps";
        } else {
            return BPS + " Bps";
        }
    }


    /*
     ** In the given string s, look for pattern.  If found, return the
     ** concatenation of the capturing groups.
     */
    private static String match(String pattern, String s) {
        LOGGER.traceEntry(pattern);
        LOGGER.traceEntry(s);

        String ret = null;

        Matcher matcher = Pattern.compile(pattern).matcher(s);
        if (matcher.find()) {
            final int count = matcher.groupCount();
            if (count == 0) {
                return new String(s.substring(matcher.start(), matcher.end()));
            }

            ret = "";
            for (int i = 1; i <= matcher.groupCount(); i++) {
                ret += (matcher.group(i));
            }
        }

        LOGGER.traceExit(ret);
        return (ret);
    }

    /*
    ** http://www.w3schools.com/tags/tag_base.asp
    ** "The <base> tag specifies the base URL/target for all relative URLs
    ** in a document.
    ** The <base> tag goes inside the <head> element."
    */
    static String newBase(@NonNull final String base) {
        LOGGER.traceEntry(base);

        String b = "<[bB][aA][sS][eE].*[hH][rR][eE][fF]=[\"']?([^\"'# ]*)";
        String ret = match(b, base);

        LOGGER.traceExit(ret);
        return (ret);
    }

    /**
     * look for "interesting" parts of a HTML string.  interesting thus far
     * means href's, src's, img's etc.
     *
     * @param match the string to examine
     * @return the interesting part if any, and null if none
     */
    static List<String> interesting(@NonNull String match) {
        LOGGER.traceEntry(match);

        List<String> ret = new ArrayList<>();

        for (String string : REplican.ARGS.Interesting) {
            String m = match(string, match);
            if (m != null) {
                ret.add(m);
            }
        }
        LOGGER.traceExit(ret);
        return (ret);
    }
}
