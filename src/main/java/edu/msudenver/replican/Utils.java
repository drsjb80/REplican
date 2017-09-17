package edu.msudenver.replican;

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import org.apache.logging.log4j.Logger;
/*
 * SEVERE
 * WARNING
 * INFO
 * CONFIG
 * FINE
 * FINER        (entering and exiting)
 * FINEST
 */

public class Utils
{
    private static Logger logger = REplican.getLogger();

    /**
     * make a String pattern of, e.g.: [Aa][Bb], from a string, ignoring
     * non-alpha characters.
     *
     * @param        s        the initial string
     * @return                the initial string with the appropriate brackets
     */
    static String both (String s)
    {
        logger.traceEntry (s);
        String r = "";

        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);

            if (Character.isLetter (c))
            {
                r += "[" + Character.toLowerCase (c) +
                     Character.toUpperCase (c) + "]";
            }
            else
            {
                r += c;
            }
        }

        logger.traceExit (r);
        return (r);
    }

    /**
     * Check if a string matches one of an array of regular
     * expressions.  The string must not have the dots escaped, while
     * the regular expression must.
     *
     * @param        s        the string to check
     * @param        re        the array of regular expressions
     */
    private static boolean matches (String re[], String s)
    {
        logger.traceEntry ("re" + java.util.Arrays.toString (re));
        logger.traceEntry (s);

        for (int i = 0; i < re.length; i++)
        {
            logger.trace ("Matching: '" + s + "' and '" + re[i] + "'");
            try
            {
                if (s.matches (re[i]))
                {
                    logger.traceExit ("true");
                    return (true);
                }
            }
            catch (java.util.regex.PatternSyntaxException PSE)
            {
                logger.warn (PSE);
            }
        }

        logger.traceExit ("false");
        return (false);
    }
    
    /*
    ** Use the array pairs as pattern and replacement pairs.  E.g.:
    ** pairs[0] == "\\.wmv.*" and pairs[1] == ".wmv"
    */
    static String replaceAll (String s, String pairs[])
    {
        logger.traceEntry (s);
        logger.traceEntry (Arrays.toString(pairs));

        if (pairs == null)
            return (s);

        if ((pairs.length % 2) != 0)
        {
            logger.error ("pairs not even");
            logger.traceExit (s);
            return (s);
        }

        for (int i = 0; i < pairs.length; i += 2)
        {
            s = s.replaceAll (pairs[i], pairs[i+1]);
        }

        logger.traceExit (s);
        return (s);
    }


    /**
     * Match a string first against an accepting condition; if that
     * matches, check against a rejecting condition.
     *
     * @param        yes        an array of accepting regular expressions
     * @param        no        an array of rejecting regular expressions
     * @param        s        the string to compare
     */
    static boolean blurf (String[] yes, String[] no, String s,
        boolean ifBothNull)
    {
        if (yes != null) logger.trace ("yes" + java.util.Arrays.toString(yes));
        if (no != null) logger.trace ("no" + java.util.Arrays.toString (no));
        if (s != null) logger.trace (s);

        if (yes == null && no == null)
        {
            return (ifBothNull);
        }

        if (yes != null && no != null)
        {
            boolean ret = matches (yes, s) && ! matches (no, s);
            logger.traceExit (new Boolean (ret));
            return (ret);
        }
        else if (yes == null)
        {
            boolean ret = ! matches (no, s);
            logger.traceExit (new Boolean (ret));
            return (ret);
        }
        else // no == null
        {
            boolean ret = matches (yes, s);
            logger.traceExit (new Boolean (ret));
            return (ret);
        }
    }

    private static Level getLevel (String s)
    {
        if (s == null) return (Level.OFF);

        if (s.equals ("")) return (Level.INFO);

        if (s.equalsIgnoreCase ("SEVERE"))  return (Level.SEVERE);
        if (s.equalsIgnoreCase ("WARNING")) return (Level.WARNING);
        if (s.equalsIgnoreCase ("INFO"))    return (Level.INFO);
        if (s.equalsIgnoreCase ("CONFIG"))  return (Level.CONFIG);
        if (s.equalsIgnoreCase ("FINE"))    return (Level.FINE);
        if (s.equalsIgnoreCase ("FINER"))   return (Level.FINER);
        if (s.equalsIgnoreCase ("FINEST"))  return (Level.FINEST);
        if (s.equalsIgnoreCase ("ALL"))     return (Level.ALL);
        if (s.equalsIgnoreCase ("OFF"))     return (Level.OFF);

        return (Level.ALL);
    }

    static String[] combineArrays (String one[], String two[])
    {
        if (one == null && two == null)
            return (null);
        if (two == null)
            return (one);
        if (one == null)
            return (two);

        String ret[] = new String[one.length + two.length];
        System.arraycopy (one, 0, ret, 0, one.length);
        System.arraycopy (two, 0, ret, one.length, two.length);

        return (ret);
    }
}
