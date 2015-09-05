package edu.mscd.cs.httpfile;

import java.io.*;

import java.util.Map;
import java.util.List;
import java.util.Vector;
import java.util.Set;
import java.util.Hashtable;
import java.util.Date;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.logging.Logger;

import java.net.URL;
import java.net.HttpURLConnection;

import java.text.SimpleDateFormat;
import java.text.ParseException;


// http://java.sun.com/developer/onlineTraining/protocolhandlers/
// http://www.docjar.com/html/api/org/apache/geronimo/system/url/file/FileURLConnection.java.html

/**
 * A class that looks a lot like a web connection, but serves all the
 * information from the local machine, including all headers and return
 * codes.  I wanted to be able to do detailed debugging more easily than
 * making changes to files on a web server allowed me to do, so I wrote
 * this.  Note: case is not considered when matching a header field key,
 * but keys and values are returned using the case they arrived in.  The
 * specification states that case is irrelevant.
 *
 * @author Steve Beaty
 */
public class HttpfileURLConnection extends HttpURLConnection
{
    /*
    private Map<String, List<String>> headerFields =
        new Hashtable<String, List<String>>();
    */
    private Map headerFields = new Hashtable();

    private static Logger logger = Logger.getLogger ("global");
    private InputStream is;
    private int code = HTTP_OK;
    private String message = "OK";

    public HttpfileURLConnection (URL url) { super (url); }

    private String getValue (String key)
    {
        logger.finer (key);

        List ls = (List) headerFields.get (key.toLowerCase());
        if (ls == null)
            return (null);

        String ret = null;
        boolean first = true;

        for (Iterator i = ls.iterator(); i.hasNext(); )
        {
            String s = (String) i.next();

            if (first)
            {
                ret = s;
                first = false;
            }
            else
            {
                ret += "; " + s;
            }
        }

        return (ret);
    }

    public Map getHeaderFields() { return (headerFields); }

    public String getContentEncoding()
        { return (getValue ("content-encoding")); }
    public int getContentLength()
        { return (Integer.parseInt (getValue ("content-length"))); }
    public String getContentType()
        { return (getValue ("content-type")); }
    public long getDate()
        { return (Long.parseLong (getValue ("date"))); }
    public long getExpiration()
        { return (Long.parseLong (getValue ("expires"))); }
    public long getIfModifiedSince()
        { return (Long.parseLong (getValue ("if-modified-since"))); }

    /**
     * Return the time in seconds from 01/01/1970 retrieved from the
     * Last-Modified http header field.
     *
     * @return	the time in seconds since 01/01/1970
     */
    public long getLastModified()
    {
        String LM = getValue ("last-modified");
        logger.finest (LM);

        if (LM != null)
        {
            try
            {
                Date d = (new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss zzz").
                    parse (LM));
                return (d.getTime());
            }
            catch (ParseException PE)
            {
                logger.finer (PE.toString());
            }
        }

        return (-1);
    }

    /**
     * Return the http respose code from the connection.  HTTP_NOT_FOUND
     * for a file not found, HTTP_INTERNAL_ERROR for other exceptions, and
     * HTTP_OK for an open file.
     */
    public int getResponseCode() { return (code); }

    /**
     * Return the text string associated with the response code
     */
    public String getResponseMessage() { return (message); }

    /**
     * Whether this connection is using a proxy: always false.
     *
     * @return	false
     */
    public boolean usingProxy() { return (false); }

    /**
     * Close the InputStream associated with the file.
     */
    public void disconnect()
    {
        try { is.close(); }
        catch (IOException e) { logger.finer (e.toString()); }
        connected = false;
    }

    /**
     * Get the error stream, always returns null.
     *
     * @return	null
     */
    public InputStream getErrorStream() { return (null); }

    /**
     * Get a particular header field.
     *
     * @param	s	the name of the header field
     * @return		the value of the header field
     */
    public String getHeaderField (String s) { return (getValue (s)); }

    /**
     * Get a header field value as an integer.  If there isn't an
     * associated value, return the default, which is the second parameter.
     *
     * @param	name	the name of the header field
     * @param	d	the default value to return if name doesn't match
     * @return		the value if found, or the default
     */
    public int getHeaderFieldInt (String name, int d)
    {
        String s = getValue (name);
        if (s != null)
            return (Integer.parseInt (s));
        else
            return (d);
    }

    /**
     * Get a header field value as a date.  If there isn't an
     * associated value, return the default, which is the second parameter.
     *
     * @param	name	the name of the header field
     * @param	d	the default value to return if name doesn't match
     * @return		the date if found, or the default
     */
    public long getHeaderFieldDate (String name, long d)
    {
        logger.finer (name);
        logger.finer ("" + d);

        String s = getValue (name);
        if (s != null)
        {
            try
            {
                return (java.text.DateFormat.getDateInstance().
                    parse (s).getTime());
            }
            catch (java.text.ParseException e)
            {
                logger.finer (e.toString());
            }
        }

        return (d);
    }

    /**
     * Get the nth header field key.
     */
    public String getHeaderFieldKey (int n)
    {
        String ret = null;
        int count = 0;

        for (Iterator i = headerFields.keySet().iterator(); i.hasNext(); )
        {
            String s = (String) i.next();

            ret = s;
            if (count++ >= n)
                break;
        }

        return (ret);
    }

    /**
     * Get the nth header field value.
     */
    public String getHeaderField (int n)
    {
        logger.finer ("" + n);

        String ret = null;
        int count = 0;

        for (Iterator i = headerFields.values().iterator(); i.hasNext(); )
        {
            if (count == n)
            {
                List ls = (List) i.next();
                for (Iterator j = ls.iterator(); j.hasNext(); )
                {
                    String s = (String) j.next();

                    if (ret == null)
                    ret = s;
                    else
                    ret += "; " + s;
                }
            }

            if (count++ >= n)
                break;
        }

        return (ret);
    }

    public void connect()
    {
        readHeaders();

        try
        {
            is = new BufferedInputStream (new FileInputStream (url.getPath()));
        }
        catch (FileNotFoundException e)
        {
            logger.warning ("File not found: " + url.getPath());
            code = HTTP_NOT_FOUND;
            message = "Not found: " + url.getPath();
            return;
        }

        int c, count = 0;

        for (;;)
        {
            try
            {
                c = is.read();

                if (c == -1)
                {
                    code = HTTP_INTERNAL_ERROR;
                    message = "Internal server error: no headers";
                    return;
                }

                // what if the line separator is multi-character?
                if (c == System.getProperty ("line.separator").charAt(0))
                    count++;
                else
                    count = 0;

                if (count == 2)
                    break;
            }
            catch (IOException e)
            {
                code = HTTP_INTERNAL_ERROR;
                message = "Internal server error: IOException";
                return;
            }
        }

        connected = true;
    }

    /**
     * Return the InputStream associated with the file, beyond the end of
     * the headers.
     */
    public InputStream getInputStream() { return (connected ? is : null); }

    private void readHeaders()
    {
        BufferedReader br = null;
        try
        {
            br = new BufferedReader (new FileReader (url.getPath()));
        }
        catch (FileNotFoundException e)
        {
            logger.finer (e.toString());
            return;
        }

        try
        {
            String s = null;
            while ((s = br.readLine()) != null)
            {
                if (s.trim().equals (""))
                    break;

                String[] split = s.split (":", 2);
                if (split.length != 2)
                {
                    logger.warning (s + " is not an HTTP header, assuming no"
                        + " headers at all");
                    headerFields.clear();
                    br.close();
                    return;
                }

                String key = split[0].trim().toLowerCase();

                List v = (List) headerFields.get (key);
                if (v == null)
                    v = new Vector();

                String a[] = split[1].split (";");

                for (int i = 0; i < a.length; i++)
                {
                    v.add (a[i].trim());
                }

                headerFields.put (key, v);
            }

            br.close();
        }
        catch (IOException e)
        {
            logger.finer (e.toString());
            return;
        }
    }

    public static void main (String args[]) throws IOException
    {
        // System.setProperty ("java.protocol.handler.pkgs", "edu.mscd.cs");
        URL.setURLStreamHandlerFactory (new HandlerFactory());

        HttpURLConnection fuc = new HttpfileURLConnection
            (new URL ("httpfile:tests/Cookies/CookieExpires.httpfile"));
        fuc.connect();
        System.out.println (fuc.getContentType());
        System.out.println (fuc.getHeaderFields());
        System.out.println (fuc.getDoInput());
        System.out.println ("k(0)" + fuc.getHeaderFieldKey (0));
        System.out.println ("v(0)" + fuc.getHeaderField (0));
        System.out.println ("k(1)" + fuc.getHeaderFieldKey (1));
        System.out.println ("v(1)" + fuc.getHeaderField (1));
        System.out.println (fuc.getHeaderField ("set-cookie"));

        InputStream is = fuc.getInputStream();
        int i;
        while ((i = is.read()) != -1)
            System.out.print ((char) i);
    }
}
