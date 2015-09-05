import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

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
                   |       "Max-Age" "=" value
                   |       "Path" "=" value
                   |       "Secure"
                   |       "Version" "=" 1*DIGIT
*/

/**
 * Cookies revolve around domains and paths, not URLs
 */
public class Cookie extends Hashtable
{
    private JavaLN logger = REplican.logger;
    private String Domain;
    private String Path;
    private long MaxAge = -1;
    private boolean Secure;
    private int Version;

    public String getDomain() { return (Domain); }
    public String getPath() { return (Path); }
    public long getMaxAge() { return (MaxAge); }
    public boolean getSecure() { return (Secure); }
    public int getVersion() { return (Version); }

/*
Version
      Defaults to "old cookie" behavior as originally specified by
      Netscape.  See the HISTORICAL section.

Domain
      Defaults to the request-host.  (Note that there is no dot at the
      beginning of request-host.)

Max-Age
      The default behavior is to discard the cookie when the user agent
      exits.

Path
      Defaults to the path of the request URL that generated the Set-Cookie
      response, up to, but not including, the right-most /.

Secure
      If absent, the user agent may send the cookie over an insecure
      channel.
*/

/*
   The value for the Path attribute is not a prefix of the request-
   URI.

   The value for the Domain attribute contains no embedded dots or
   does not start with a dot.

   The value for the request-host does not domain-match the Domain
   attribute.

   The request-host is a FQDN (not IP address) and has the form HD,
   where D is the value of the Domain attribute, and H is a string
   that contains one or more dots.
*/

    private void reset()
    {
	Domain = null;
	Path = null;
	MaxAge = -1;
	Secure = false;
	Version = 0;
	clear();
    }

    private boolean checkDomain (String host)
    {
	logger.entering (host);

	// might have been set by a previous cookie, and checked then
	if (Domain.equals (host)) return (true);

	if (! Domain.startsWith ("."))
	{
	    logger.fine (Domain + " does not start with .");
	    return (false);
	}

	if (Domain.indexOf (".", 1) == -1)
	{
	    logger.fine (Domain + " does not have embedded dots");
	    return (false);
	}

	if (! host.endsWith (Domain))
	{
	    logger.fine (host + " does not end with " + Domain);
	    return (false);
	}

	String Host = host.replaceFirst ("\\..*", "") + Domain;
	if (! Host.equals (host))
	{
	    logger.fine (host + " is longer than " + Domain);
	    return (false);
	}

	return (true);
    }

    private boolean checkPath (String path)
    {
	logger.entering (path);

	if (! path.startsWith (Path))
	{
	    logger.fine ("Path \"" + path + "\" does not start with \"" +
	        Path + "\"");
	    return (false);
	}

	return (true);
    }

    private boolean defaultValues (String key, String value)
    {
	logger.entering (key);
	logger.entering (value);

	if (key.equalsIgnoreCase ("max-age"))
	{
	    Date d = new Date();

	    logger.finest ("" + d.getTime());
	    MaxAge = d.getTime() + Long.parseLong (value);
	    logger.exiting (true);
	    return (true);
	}

	// Netscape's original proposal defined an Expires header that took a
        // date value in a fixed-length variant format *in place of* Max-Age:
	// Wdy, DD-Mon-YY HH:MM:SS GMT
	// Really:
	// Wdy, DD-Mon-YYYY HH:MM:SS GMT
	// http://kb.mozillazine.org/Cookies.txt
	// expires=Sat, 10-May-2008 21:15:19 GMT
	if (key.equalsIgnoreCase ("expires"))
	{
	    Date d = null;
	    try
	    {
            d = (new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss zzz").
                parse (value));
	    }
	    catch (ParseException PE1)
        {
            try
            {
                d = (new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss zzz").
                    parse (value));
            }
            catch (ParseException PE2)
            {
                logger.throwing (PE2);
                MaxAge = -1;
                logger.exiting (true);
                return (true);
            }
        }

	    MaxAge = d.getTime();
	    logger.exiting (true);
	    return (true);
	}

	if (key.equalsIgnoreCase ("domain"))
	{
	    Domain = value;
	    logger.exiting (true);
	    return (true);
	}
	if (key.equalsIgnoreCase ("path"))
	{
	    Path = value;
	    logger.exiting (true);
	    return (true);
	}
	if (key.equalsIgnoreCase ("version"))
	{
	    Version = Integer.parseInt (value);
	    logger.exiting (true);
	    return (true);
	}
	if (key.equalsIgnoreCase ("secure"))
	{
	    Secure = true;
	    logger.exiting (true);
	    return (true);
	}

	logger.exiting (false);
	return (false);
    }

    private Hashtable createTable (String cookieString)
    {
	logger.entering (cookieString);

	Hashtable ret = new Hashtable();
	String b[] = cookieString.split (";");

        for (int i = 0; i < b.length; i++)
	{
	    logger.finest (b[i]);

	    String c[] = b[i].split ("=", 2);
	    String key = c[0].trim();
	    String value = "";

	    if (c.length == 2)
	    {
	        value = c[1];
		value = value.trim();
	    }
	    logger.fine ("key = " + key + ", value = " + value);

	    if (! defaultValues (key, value))
		ret.put (key, value);
	}

	logger.exiting (ret);
	return (ret);
    }

    private Object getIgnoreCase (Hashtable h, String s)
    {
	for (Enumeration e = keys(); e.hasMoreElements();)
	{
	    String key = (String) e.nextElement();
	    logger.finest ("Comparing key: " + key + " and string " + s);

	    if (key.equalsIgnoreCase (s))
	    {
	        return (h.get (key));
	    }
	}

	return (null);
    }

    public boolean addToValues (String host, String path,
        String cookieString)
    {
	logger.finest (host);
	logger.finest (path);
	logger.finest (cookieString);

	Hashtable newTable = createTable (cookieString);

	if (Domain == null)
	{
	    if (host.equals (""))
	        host = "localhost";
	    Domain = host;
	}
	else if (! checkDomain (host))
	{
	    reset();
	    return (false);
	}

	if (Path == null)
	{
	    Path = path;
	}
	else if (! checkPath (path))
	{
	    reset();
	    return (false);
	}

	if (MaxAge == 0)
	{
	    reset();
	    return (false);
	}

	putAll (newTable);
	return (true);
    }

    public String getSave ()
    {
	long time = new Date().getTime();

	logger.finest ("time: " + time);
	logger.finest ("MaxAge: " + MaxAge);

	if (MaxAge <= 0 || MaxAge < time)
	{
	    logger.fine (this.toString() + " has expired");
	    return (null);
	}

	String ret = "";

	for (Enumeration e = keys(); e.hasMoreElements();)
	{
	    String key = (String) e.nextElement();
	    String value = (String) get (key);

	    ret += Domain + "\t";
	    ret += Domain.startsWith (".") ? "TRUE\t" : "FALSE\t";
	    ret += Path + "\t";
	    ret += Secure ? "TRUE\t" : "FALSE\t";
	    ret += MaxAge/1000 + "\t";
	    if (value != null)
		ret += key + "\t" + value + "\n";
	    else
		ret += key + "\n";
	}

	logger.exiting (ret);
	return (ret);
    }

    public String getCookieString ()
    {
	String ret = "";
	boolean first = true;

	for (Enumeration e = keys(); e.hasMoreElements();)
	{
	    String key = (String) e.nextElement();
	    ret += (first ? "" : "; ") + key;

	    String value = (String) get (key);

	    // booleans such as Secure don't have values...
	    if (! value.equals (""))
		ret += "=" + value;

	    first = false;
	}

	logger.exiting (ret);
	return (ret);
    }

    public String toString()
    {
        return ("Domain = " + Domain +
	    ", Path = " +  Path +
	    ", MaxAge = " +  MaxAge +
	    ", Secure = " +  Secure +
	    ", Version = " +  Version +
	    ", getCookieString() = " + getCookieString());
    }

    public static void main (String args[])
    {
        Cookie cookie = new Cookie();

	cookie.addToValues ("www.google.com", "",
	    "Name=badpath; path=/this/is/bad");
	// System.out.println (cookie);

	cookie.addToValues ("www.google.com", "",
	    "Name=baddomain; domain=.piper.org");
	// System.out.println (cookie);

	cookie.addToValues ("www.google.com", "",
	    "Name=baddomain; domain=piper.org");
	// System.out.println (cookie);

	cookie.addToValues ("www.google.com", "",
	    "Name=baddomain; domain=.org");
	// System.out.println (cookie);

	cookie.addToValues ("www.google.com", "",
	    "Name=good; domain=.google.com");
	// System.out.println (cookie);

	cookie.addToValues ("www.google.com", "", "maxage=0");
	cookie.getSave();
	// System.out.println (cookie);

	cookie.addToValues ("www.google.com", "",
	    "maxage=1000");
	cookie.getSave();
	// System.out.println (cookie);
	// System.out.println (cookie.getSave());

        Cookie cookietoo = new Cookie();
	cookietoo.addToValues ("www.long.google.com", "",
	    "Name=toolong; domain=.google.com");
	// System.out.println (cookietoo);
    }
}
