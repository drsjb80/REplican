import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import edu.mscd.cs.javaln.*;

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
    private static JavaLN logger = (JavaLN) JavaLN.getLogger ("REplican");

    /**
     * make a String pattern of, e.g.: [Aa][Bb], from a string, ignoring
     * non-alpha characters.
     *
     * @param        s        the initial string
     * @return                the initial string with the appropriate brackets
     */
    static String both (String s)
    {
        logger.entering (s);
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

        logger.exiting (r);
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
        logger.entering ("re" + java.util.Arrays.toString (re));
        logger.entering (s);

        for (int i = 0; i < re.length; i++)
        {
            logger.finer ("Matching: '" + s + "' and '" + re[i] + "'");
            try
            {
                if (s.matches (re[i]))
                {
                    logger.exiting ("true");
                    return (true);
                }
            }
            catch (java.util.regex.PatternSyntaxException PSE)
            {
                logger.warning (PSE);
            }
        }

        logger.exiting ("false");
        return (false);
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
        if (yes != null) logger.finest ("yes" + java.util.Arrays.toString(yes));
        if (no != null) logger.finest ("no" + java.util.Arrays.toString (no));
        if (s != null) logger.finest (s);

        if (yes == null && no == null)
        {
            return (ifBothNull);
        }

        if (yes != null && no != null)
        {
            boolean ret = matches (yes, s) && ! matches (no, s);
            logger.exiting (new Boolean (ret));
            return (ret);
        }
        else if (yes == null)
        {
            boolean ret = ! matches (no, s);
            logger.exiting (new Boolean (ret));
            return (ret);
        }
        else // no == null
        {
            boolean ret = matches (yes, s);
            logger.exiting (new Boolean (ret));
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

    static JavaLN createLogger (String l)
    {
        Level level = getLevel (l);

        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel (level);
        if (level == Level.INFO)
            ch.setFormatter (new NullFormatter());
        else
            ch.setFormatter (new LineNumberFormatter());

        JavaLN logger = new JavaLN();
        logger.setLevel (level);
        logger.addHandler (ch);
        logger.setUseParentHandlers (false);

        return (logger);
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
