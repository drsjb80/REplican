package edu.msudenver.cs.replican;

import java.util.Hashtable;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import org.apache.logging.log4j.Logger;

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
class Cookie extends Hashtable<String, String>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Logger logger = REplican.getLogger();
	private String Domain;
	private String Path;
	private long MaxAge = -1;
	private boolean Secure;
	private int Version;

	public Cookie() {}

	public Cookie(String Domain, String Path, long MaxAge, boolean Secure,
                  String name, String value) {
		this.Domain = Domain;
		this.Path = Path;
		this.MaxAge = MaxAge;
		this.Secure = Secure;
		this.put(name, value);
	}

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

	private void checkDomain (String host) throws IllegalArgumentException
	{
		logger.traceEntry (host);

		// might have been set by a previous cookie, and checked then
		if (Domain.equals (host)) return;

		if (! Domain.startsWith ("."))
		{
			reset();
			throw new IllegalArgumentException(Domain + " does not start with .");
		}

		if (Domain.indexOf (".", 1) == -1)
		{
			reset();
			throw new IllegalArgumentException(Domain + " does not have embedded dots");
		}

		if (! host.endsWith (Domain))
		{
			reset();
			throw new IllegalArgumentException(host + " does not end with " + Domain);
		}

		String Host = host.replaceFirst ("\\..*", "") + Domain;
		if (! Host.equals (host))
		{
			reset();
			throw new IllegalArgumentException(host + " is longer than " + Domain);
		}
	}

	private void checkPath (String path) throws IllegalArgumentException
	{
		logger.traceEntry (path);

		if (! path.startsWith (Path))
		{
			reset();
			throw new IllegalArgumentException("Path \"" + path + "\" does not start with \"" +
					Path + "\"");
		}
	}

	private boolean defaultValues (String key, String value)
	{
		logger.traceEntry (key);
		logger.traceEntry (value);

		if (key.equalsIgnoreCase ("max-age"))
		{
			Date d = new Date();

			logger.trace ("" + d.getTime());
			MaxAge = d.getTime() + Long.parseLong (value);
			logger.traceExit (true);
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
					logger.traceExit (true);
					return (true);
				}
			}

			MaxAge = d.getTime();
			logger.traceExit (true);
			return (true);
		}

		if (key.equalsIgnoreCase ("domain"))
		{
			Domain = value;
			logger.traceExit (true);
			return (true);
		}
		if (key.equalsIgnoreCase ("path"))
		{
			Path = value;
			logger.traceExit (true);
			return (true);
		}
		if (key.equalsIgnoreCase ("version"))
		{
			Version = Integer.parseInt (value);
			logger.traceExit (true);
			return (true);
		}
		if (key.equalsIgnoreCase ("secure"))
		{
			Secure = true;
			logger.traceExit (true);
			return (true);
		}

		logger.traceExit (false);
		return (false);
	}

	private Hashtable<String, String> createTable (String cookieString)
	{
		logger.traceEntry (cookieString);

		Hashtable<String, String> ret = new Hashtable<>();
		String b[] = cookieString.split (";");

		for (int i = 0; i < b.length; i++)
		{
			logger.trace (b[i]);

			String c[] = b[i].split ("=", 2);
			String key = c[0].trim();
			String value = "";

			if (c.length == 2)
			{
				value = c[1];
				value = value.trim();
			}
			logger.debug ("key = " + key + ", value = " + value);

			if (! defaultValues (key, value))
				ret.put (key, value);
		}

		logger.traceExit (ret);
		return (ret);
	}

	/*
	private Object getIgnoreCase (Hashtable<String, String> h, String s)
	{
		for (Enumeration e = keys(); e.hasMoreElements();)
		{
			String key = (String) e.nextElement();
			logger.trace ("Comparing key: " + key + " and string " + s);

			if (key.equalsIgnoreCase (s))
			{
				return (h.get (key));
			}
		}

		return (null);
	}
	*/

	public void addToValues (String host, String path, String cookieString)
			throws IllegalArgumentException
	{
		logger.trace (host);
		logger.trace (path);
		logger.trace (cookieString);

		Hashtable<String, String> newTable = createTable (cookieString);

		if (Domain == null) {
			if (host.equals ("")) host = "localhost";
			Domain = host;
		}
		else {
			checkDomain(host);
		}

		if (Path == null) {
			Path = path;
		}
		else {
			checkPath (path);
		}

		if (MaxAge == 0) {
			reset();
			throw new IllegalArgumentException("MaxAge==0");
		}

		putAll (newTable);
	}

    /**
     * Get a string of the correct form to save in a Netscape file.
     * @return the formatted string
     */
	public String getSave ()
	{
		long time = new Date().getTime();

		logger.trace ("time: " + time);
		logger.trace ("MaxAge: " + MaxAge);

		if (MaxAge <= 0 || MaxAge < time)
		{
			logger.debug (this.toString() + " has expired");
			return (null);
		}

		String ret = "";

		for (String key: keySet())
		{
			String value = get (key);

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

		logger.traceExit (ret);
		return (ret);
	}

	public String getCookieString ()
	{
		String ret = "";
		boolean first = true;

		for (String key: keySet())
		{
			ret += (first ? "" : "; ") + key;

			String value = get (key);

			// booleans such as Secure don't have values...
			if (! value.equals (""))
				ret += "=" + value;

			first = false;
		}

		logger.traceExit (ret);
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
