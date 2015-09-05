import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Enumeration;
import edu.mscd.cs.javaln.JavaLN;

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

public class Cookies
{
    private Vector cookies = new Vector();
    private JavaLN logger = REplican.logger;

    public void addCookie (String hostORdomain, String path,
        String cookieString)
    {
        logger.entering (hostORdomain);
        logger.entering (path);
        logger.entering (cookieString);

        Cookie newCookie = new Cookie ();

        if (! newCookie.addToValues (hostORdomain, path, cookieString))
        {
            logger.fine ("Not adding to cookie jar: " + cookieString);
            return;
        }

        String newDomain = newCookie.getDomain();
        String newPath = newCookie.getPath();
        String newDomainAndPath = newDomain + newPath;
        logger.finest (newDomainAndPath);

        // see if there is already a cookie for this domain and path
        for (int i = 0; i < cookies.size(); i++)
        {
            Cookie cookie = (Cookie) cookies.elementAt (i);
            String oldDomain = cookie.getDomain();
            String oldPath = cookie.getPath();
            String oldDomainAndPath = oldDomain + oldPath;

            if (oldDomainAndPath.equals (newDomainAndPath))
            {
                logger.finest ("Using: " + oldDomainAndPath);
                if (! cookie.addToValues (oldDomain, oldPath, cookieString))
                {
                    logger.fine ("Removing from cookie jar");
                    cookies.remove (i);
                }
                return;
            }
        }

        logger.finest ("Created new cookie: " + newCookie);
        cookies.add (newCookie);
    }

    public void addCookie (URL url, String cookieString)
    {
        String host = url.getHost();

        if (host.equals (""))
            host = "localhost";  // only useful for httpfile://

        String path = url.getPath();

        addCookie (host, path, cookieString);
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
    private void doLine (String line)
    {
        logger.entering (line);

        if (line.charAt (0) == '#')
            return;

        String s[] = line.split ("\t");

        if (s.length > 6)
        {
            Date date = new Date (Long.parseLong (s[4]) * 1000);
            String expires =
                new SimpleDateFormat ("EEE, dd-MMM-yyyy hh:mm:ss zzz").
                format (date);

            addCookie (s[0], s[2], s[5] + "=" + s[6] +
                "; Expires=" + expires);
        }
        else
            addCookie (s[0], s[2], s[5]);
    }

    public void loadCookies (String file)
    {
        BufferedReader in = null;

        try
        {
            in = new BufferedReader (new FileReader (file));
        }
        catch (FileNotFoundException fnfe)
        {
            logger.warning ("File not found: " + file);
            return;
        }

        String line;

        try
        {
            while ((line = in.readLine()) != null)
            {
                if (line.length() > 0)
                    doLine (line);
            }
        }
        catch (IOException ioe)
        {
            logger.throwing (ioe);
        }
    }

    public void saveCookies (String filename)
    {
        logger.entering (filename);

        try
        {
            FileWriter fw = new FileWriter (filename);

            fw.write ("# HTTP Cookie File\n");
            fw.write ("# This is a generated file!  Do not edit.\n");

            logger.finest ("" + cookies.size());

            for (int i = 0; i < cookies.size(); i++)
            {
                Cookie cookie = (Cookie) cookies.elementAt (i);
                String s = cookie.getSave();
                if (s != null)
                    fw.write (s);
            }

            fw.close();
        }
        catch (IOException E)
        {
            logger.throwing (E);
        }
    }

    public String findCookies (URL url)
    {
        logger.entering (url);

        String ret = null;
        String urlDomain = url.getHost();
        String urlPath = url.getPath();

        for (int i = 0; i < cookies.size(); i++)
        {
            Cookie cookie = (Cookie) cookies.elementAt (i);

            String cookieDomain = cookie.getDomain();
            String cookiePath = cookie.getPath();

            logger.finest ("urlDomain = " + urlDomain);
            logger.finest ("urlPath = " + urlPath);
            logger.finest ("cookieDomain = " + cookieDomain);
            logger.finest ("cookiePath = " + cookiePath);
            logger.finest
                (urlDomain.endsWith (cookieDomain) ? "true" : "false");
            logger.finest
                (urlPath.startsWith (cookiePath) ? "true" : "false");

            if (urlDomain.endsWith (cookieDomain) &&
                urlPath.startsWith (cookiePath))
            {
                if (ret == null)
                    ret = cookie.getCookieString();
                else
                    ret += "; " + cookie.getCookieString();
            }
        }

        return (ret);
    }

    public static void main (String args[]) throws MalformedURLException
    {
        Cookies cookies = new Cookies();

        cookies.loadCookies ("cookies.txt");
        System.out.println (cookies.findCookies (new URL
            ("http://cs.mscd.edu/")));

        cookies.saveCookies ("newcookies.txt");
    }
}
