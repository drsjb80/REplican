package edu.msudenver.replican;

import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.io.*;

import java.util.Hashtable;
import java.util.Map;
import java.util.List;

import org.apache.logging.log4j.Logger;

public class YouAreEll
{
    private Logger logger = REplican.getLogger();
    private URLConnection uc;
    private String ContentType;
    private int ContentLength;
    private InputStream inputstream;

    private String url;
    private REplicanArgs args;
    private Cookies cookies;
    private Hashtable<String, Boolean> urls;

    public YouAreEll (String url, Hashtable<String, Boolean> urls,
        Cookies cookies, REplicanArgs args)
    {
        this.url = url;
        this.urls = urls;
        this.cookies = cookies;
        this.args = args;
        inputstream = createInputStream();
    }

    String getContentType() { return (ContentType); }
    int getContentLength() { return (ContentLength); }
    String getURL() { return (url); }
    InputStream getInputStream() { return (inputstream); }
    long getLastModified() { return (uc.getLastModified()); }

    private void dealWithRedirects ()
    {
        /*
        HTTP/1.1 301 Moved Permanently
        Date: Wed, 16 May 2007 19:18:57 GMT
        Server: Apache/2.0.53 (Fedora)
        Location: http://emess.mscd.edu/~beaty/Pictures/
        Content-Length: 326
        Connection: close
        Content-Type: text/html; charset=iso-8859-1
        */

        // mark the original fetched

        urls.put (url, new Boolean(true));

        if (! args.FollowRedirects)
            return;

        String redirected = uc.getHeaderField ("Location");
        if (args.PrintRedirects)
            logger.info ("Redirected to: " + redirected);

        URL newURL = null;

        try
        {
            newURL = new URL (new URL (url), redirected);
        }
        catch (MalformedURLException e)
        {
            logger.throwing (e);
            return;
        }

        urls.put (newURL.toString(), new Boolean(false));
    }

    private void dealWithStopOns (int code)
    {
        int[] stopon = args.StopOn;

        for (int i = 0; i < stopon.length; i++)
        {
            if (code == stopon[i])
            {
                logger.warn ("Stopping on return code: " + code);
                System.exit (0);
            }
        }
    }

    private InputStream HUC ()
    {
        ContentType = uc.getHeaderField ("Content-Type");
        String cl = uc.getHeaderField ("Content-Length");
        if (cl != null)
        {
            try
            {
                ContentLength = Integer.parseInt (cl);
            }
            catch (NumberFormatException NFE)
            {
                ContentLength = 0;
            }
        }

        String MIMEAccept[] = args.MIMEAccept;
        String MIMEReject[] = args.MIMEReject;

        // here, if MIME has no input on the matter, assume Path has
        // already spoken so return true from blurf.
        if (! Utils.blurf (MIMEAccept, MIMEReject, ContentType, true))
            return (null);

        try
        {
            return (uc.getInputStream());
        }
        catch (IOException e)
        {
            logger.throwing (e);
            return (null);
        }
    }

    private InputStream dealWithReturnCode (int code)
    {
        if (args.StopOnnull)
            dealWithStopOns (code);

        switch (code)
        {
            case 200: return (HUC());
            case 301:
            case 302:
            {
                dealWithRedirects ();
                return (null);
            }
            default:
            {
                try
                {
                    String message = "";

                    if (uc instanceof HttpURLConnection)
                        message = ((HttpURLConnection) uc).getResponseMessage();
                    logger.warn ("For: " + url + " server returned: " +
                        code + " " + message);
                }
                catch (IOException e)
                {
                    logger.throwing (e);
                }

                return (null);
            }
        }
    }

    private int connect() throws IOException
    {
        uc = new URL (url).openConnection();

        try
        {
            if (args.UserAgent != null)
                uc.setRequestProperty ("User-Agent", args.UserAgent);

            if (args.Header != null)
            {
                for (int i = 0; i < args.Header.length; i++)
                {
                    String s[] = args.Header[i].split (":");
                    if (s[0] != null && s[1] != null)
                    {
                        uc.setRequestProperty (s[0], s[1]);
                    }
                    else
                    {
                        logger.trace ("Couldn't decipher " + args.Header[i]);
                    }
                }
            }

            String Referer = args.Referer;
            if (Referer != null)
                uc.setRequestProperty ("Referer", Referer);

            if (! args.IgnoreCookies)
            {
                String c = cookies.findCookies (new URL (url));

                if (c == null)
                    logger.trace ("No cookie");
                else
                {
                    logger.trace (c);
                    uc.setRequestProperty ("Cookie", c);
                }
            }
        }
        catch (IllegalStateException ise) { logger.throwing (ise); }

        int rc = 200;

        if (uc instanceof HttpURLConnection)
            rc = ((HttpURLConnection) uc).getResponseCode();

        uc.connect();

        logger.traceExit (rc);
        return (rc);
    }

    private InputStream getURLInputStream ()
    {
        int code;

        try
        {
            code = connect();
        }
        catch (IOException e)
        {
            logger.warn (e);
            return (null);
        }

        logger.trace (uc.toString());
        logger.trace (uc.getHeaderFields().toString());

        if (! args.IgnoreCookies)
        {
            Map<String,List<String>> m = uc.getHeaderFields();
            List<String> l = (List<String>) m.get ("Set-Cookie");
            if (l != null)
            {
                for (String cookie: l)
                {
                    logger.trace ("Adding cookie: " + cookie);
                    try
                    {
                        cookies.addCookie (new URL (url), cookie);
                    }
                    catch (MalformedURLException MUE)
                    {
                        logger.throwing (MUE);
                    }
                }
            }
        }

        return (dealWithReturnCode (code));
    }

    /**
     * get an InputStream from either a file: or http: URL.  deals
     * with http redirections.
     *
     */
    private InputStream createInputStream ()
    {
        // http://httpd.apache.org/docs/1.3/mod/mod_dir.html#directoryindex

        logger.traceEntry (url);
        InputStream is = getURLInputStream ();
        logger.traceExit (is);
        return (is);
    }
}
