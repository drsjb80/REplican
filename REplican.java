import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;

import java.net.Authenticator;

import java.io.*;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Arrays;

import java.util.logging.Level;
import java.util.logging.ConsoleHandler;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import edu.mscd.cs.jclo.JCLO;
import edu.mscd.cs.javaln.*;

import org.jsoup.*;

// http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html

public class REplican
{
    static JavaLN logger = new JavaLN();
    private REplicanArgs args = new REplicanArgs();
    private Hashtable urls = new Hashtable();
    Cookies cookies;
    private int URLcount = 0;

    REplican (String arguments[])
    {
        JCLO jclo = new JCLO (args);

        if (arguments.length == 0)
        {
            System.out.println ("Arguments:\n" + jclo.usage() + "URLs...");
            System.exit (1);
        }

        try
        {
             jclo.parse (arguments);
        }
        catch (IllegalArgumentException IAE)
        {
            System.err.println (IAE);
            System.err.println ("Arguments:\n" + jclo.usage() + "URLs...");
            System.exit (0);
        }

        String logLevel = args.LogLevel;
        ConsoleHandler ch = new ConsoleHandler();

        if (logLevel != null)
        {
            Level level = JavaLN.getLevel (logLevel);
            ch.setLevel (level);
            ch.setFormatter (new LineNumberFormatter());
            logger.setLevel (level);
        }
        else
        {
            ch.setFormatter (new NullFormatter());
        }

        logger.addHandler (ch);
        logger.setUseParentHandlers (false);

        if (args.Version)
        {
            System.out.println (Version.getVersion());
            System.exit (0);
        }

        if (args.Help)
        {
            System.out.println ("Arguments:\n" + jclo.usage() + "URLs...");
            System.exit (0);
        }

        cookies = new Cookies ();

        setDefaults ();

        if (args.LoadCookies != null)
        {
            for (int i = 0; i < args.LoadCookies.length; i++)
            {
                logger.config ("Loading cookies from " + args.LoadCookies[i]);
                cookies.loadCookies (args.LoadCookies[i]);
            }
        }

        if (args.PlistCookies != null)
        {
            for (int i = 0; i < args.PlistCookies.length; i++)
            {
                logger.config ("Loading cookies from " + args.PlistCookies[i]);
                new Plist ("file:" + args.PlistCookies[i], cookies);
            }
        }

        if (args.CheckpointEvery != 0)
        {
            logger.config ("Loading urls from " + args.CheckpointFile);

            try
            {
                ObjectInputStream ois = 
                    new ObjectInputStream (
                         new FileInputStream (args.CheckpointFile));
                urls = (Hashtable) ois.readObject();
                ois.close();
            }
            catch (IOException ioe)
            {
                logger.throwing (ioe);
            }
            catch (ClassNotFoundException cnfe)
            {
                logger.throwing (cnfe);
            }
        }

        if (args.FollowRedirects)
            HttpURLConnection.setFollowRedirects (false);
    }

    private String escapeURL (String URL)
    {
        logger.entering (URL);

        String ret = URL;

        String meta = "^.[]$()|*+?{";
        
        for (int i = 0; i < meta.length(); i++)
        {
            char c = meta.charAt(i);
            ret = ret.replaceAll ("\\" + c,  "\\\\" + c);
        }

        logger.exiting (ret);
        return (ret);
    }

    private void setDefaults ()
    {
        if (args.Interesting == null)
        {
            String urlref = "\\s*=\\s*[\"']?([^\"'>]*)";
            String href = "[hH][rR][eE][fF]";
            String src = "[sS][rR][cC]";

            String init[] =
            { 
                href + urlref,
                src + urlref,
            };

            args.Interesting = init;
        }

        if (args.URLFixUp == null)
        {
            // so, i don't remember why i collasped multiple spaces and
            // removed \'s. must have been important and i should have
            // documented. 's confuse URLs...
            // args.URLFixUp = new String[]{"\\s+", " ", "\\\\", ""};
            args.URLFixUp = new String[]{"\\s+", " ", "\\\\", "",
                "\'", "%27"};
        }

        // if they don't specify anything, look at only text.
        if (args.MIMEExamine == null && args.MIMEIgnore == null &&
            args.PathExamine == null && args.PathIgnore == null)
        {
            args.MIMEExamine = new String[]{"text/.*"};
            if (args.PrintExamine)
                logger.warning ("--MIMEExamine=" +
                    java.util.Arrays.toString (args.MIMEExamine));
        }

        // if they don't specify anything, save only what is specified on
        // the command line.
        if (args.MIMESave == null && args.MIMERefuse == null &&
            args.PathSave == null && args.PathRefuse == null)
        {
            if (args.additional.length == 0)
            {
                logger.severe ("No URLs specified");
                System.exit (1);
            }

            args.PathSave = new String[args.additional.length];

            for (int i = 0; i < args.additional.length; i++)
                args.PathSave[i] = escapeURL (args.additional[i]);

            if (args.PrintSave)
                logger.warning ("--PathSave=" +
                    java.util.Arrays.toString (args.PathSave));
        }

        if (args.PrintAll)
            args.PrintAccept = args.PrintReject = 
                args.PrintSave = args.PrintRefuse =
                args.PrintExamine = args.PrintIgnore = true;

        /*
        ** make sure we accept everything we examine, save, and the initial
        ** URLs
        */
        args.PathAccept = Utils.combineArrays (args.PathAccept,
            args.PathExamine);
        args.PathAccept = Utils.combineArrays (args.PathAccept,
            args.PathSave);
        args.PathAccept = Utils.combineArrays (args.PathAccept,
            args.additional);
    }

    /**
     * look for "interesting" parts of a HTML string.  interesting thus far
     * means href's, src's, img's etc.
     *
     * @param        s        the string to examine
     * @return                the interesting part if any, and null if none
     */
    private String[] interesting (String s)
    {
        logger.entering (s);

        if (s == null)
            return (null);

        String m[] = new String[args.Interesting.length];

        for (int i = 0; i < args.Interesting.length; i++)
        {
            m[i] = match (args.Interesting[i], s);
        }

        return (m);
    }

    private void checkpoint()
    {
        String checkpointFile = args.CheckpointFile;

        logger.finest ("writing to " + checkpointFile);

        try
        {
            ObjectOutputStream oos = 
                new ObjectOutputStream (new FileOutputStream (checkpointFile));
            oos.writeObject (urls);
            oos.close();
        }
        catch (IOException e)
        {
            logger.throwing (e);
        }
    }

    /*
    ** add a URL to the list of those to be processed
    */
    private void addOne (String total)
    {
        logger.entering (total);

        urls.put (total, new Boolean (false));

        URLcount++;

        int checkpointEvery = args.CheckpointEvery;
        if (checkpointEvery != 0 && URLcount % checkpointEvery == 0)
            checkpoint();
    }

    /*
    ** create a valid URL, paying attenting to a base if there is one.
    */
    private URL makeURL (String baseURL, String s)
    {
        logger.entering (baseURL);
        logger.entering (s);

        URL u = null;

        try
        {
            if (baseURL != null)
                u = new URL (new URL (baseURL), s);
            else
                u = new URL (s);
        }
        catch (MalformedURLException e)
        {
            logger.throwing (e);
        }

        logger.exiting (u);
        return (u);
    }

    /*
    ** In the given string s, look for pattern.  If found, return the
    ** concatenation of the capturing groups.
    */
    private String match (String pattern, String s)
    {
        logger.entering (pattern);
        logger.entering (s);

        String ret = null;

        Matcher matcher = Pattern.compile (pattern).matcher (s);
        if (matcher.find())
        {
            ret = "";
            for (int i = 1; i <= matcher.groupCount(); i++)
            {
                ret += (matcher.group (i));
            }
        }

        logger.exiting (ret);
        return (ret);
    }

    /*
    ** http://www.w3schools.com/tags/tag_base.asp
    ** "The <base> tag specifies the base URL/target for all relative URLs
    ** in a document.
    ** The <base> tag goes inside the <head> element."
    */
    private String newBase (String base)
    {
        logger.entering (base);

        if (base == null)
            return (null);

        String b = "<[bB][aA][sS][eE].*[hH][rR][eE][fF]=[\"']?([^\"'# ]*)";
        String ret = match (b, base);

        logger.exiting (ret);
        return (ret);
    }

    /*
    ** Use the array pairs as pattern and replacement pairs.  E.g.:
    ** pairs[0] == "\\.wmv.*" and pairs[1] == ".wmv"
    */
    static String replaceAll (String s, String pairs[])
    {
        logger.entering (s);
        logger.entering (pairs);

        if (pairs == null)
            return (s);

        if ((pairs.length % 2) != 0)
        {
            logger.severe ("pairs not even");
        }

        for (int i = 0; i < pairs.length; i += 2)
        {
            s = s.replaceAll (pairs[i], pairs[i+1]);
        }

        logger.exiting (s);
        return (s);
    }

    /*
    ** @param        s        the string to rewrite
    ** @param        r        a pattern and replacement ("\\.wmv.* .wmv")
    ** @return                the interesting part if any, and null if none
    */
    static String rewrite (String s, String r)
    {
        logger.entering (s);
        logger.entering (r);

        if (s != null && r != null)
        {
            String rewrites[] = r.split ("\\s");

            return (replaceAll (s, rewrites));
        }

        logger.exiting (s);
        return (s);
    }

    private void addToURLs (String baseURL, String strings[])
    {
        logger.entering (baseURL);
        logger.entering (strings);

        for (int i = 0; i < strings.length; i++)
        {
            String next = replaceAll (strings[i], args.URLFixUp);

            String newBase = newBase (next);
            if (newBase != null)
            {
                logger.fine ("Setting base to " + baseURL);
                baseURL = newBase;
            }

            String possible[] = interesting (next);

            for (int j = 0; j < args.Interesting.length; j++)
            {
                if (possible[j] != null)
                {
                    URL u = makeURL (baseURL, possible[j]);

                    if (u == null)
                        continue;
                    
                    String total = u.toString();

                    String PathAccept[] = args.PathAccept;
                    String PathReject[] = args.PathReject;

                    boolean accept = Utils.blurf (PathAccept, PathReject,
                        total, true);

                    if (args.PrintAccept && accept)
                        logger.info ("Accepting path: " + total);
                    if (args.PrintReject && !accept)
                        logger.info ("Rejecting path: " + total);

                    if (accept)
                    {
                        if (args.URLRewrite != null)
                            total = REplican.replaceAll (total,
                                args.URLRewrite);

                        // if we don't already have it
                        if (urls.get (total) == null)
                        {
                            if (args.PrintAdd)
                                logger.info ("Adding: " + total);
                            addOne (total);
                        }
                    }
                }
            }
        }
    }
    
    private void snooze (int milliseconds)
    {
        logger.entering (milliseconds);

        if (milliseconds == 0)
            return;

        logger.info ("Sleeping for " + milliseconds + " milliseconds");

        try
        {
            Thread.sleep (milliseconds);
        }
        catch (InterruptedException ie)
        {
            logger.throwing (ie);
        }
    }

    /*
    ** read from an input stream, optionally write to an output stream, and
    ** optionally look at all the URL's found in the input stream.
    */
    private boolean examineORsave (YouAreEll yrl, InputStream is,
        BufferedOutputStream bos, boolean examine, boolean save, String url)
    {
        logger.entering (is);
        logger.entering (bos);
        logger.entering (new Boolean (examine));
        logger.entering (new Boolean (save));
        logger.entering (url);


        try
        {
            int c;
            int read = 0;
            int written = 0;
            int content_length = yrl.getContentLength();
            int ten_percent = content_length > 0 ? content_length / 10 : 0;
            int count = 1;
            boolean percent = args.SaveProgress && save && ten_percent > 0;
            boolean spin = args.SaveProgress && save && ten_percent == 0;
            long start = new java.util.Date().getTime();

            if (percent) System.out.print ("0..");
            if (spin) System.out.print ("|");

            while ((c = is.read()) != -1)
            {
                if (save)
                {
                    bos.write ((char) c);
                    written++;

                    
                    if (percent && count < 10 && written > count * ten_percent)
                    {
                        System.out.print (count * 10 + "..");
                        count++;
                    }
                    else if (spin && written % 1000 == 0)
                    {
                        // System.out.println (count);
                        // System.out.println (count % 4);
                        System.out.print ("\b" + "|/-\\".charAt (count % 4));
                        count++;
                    }
                }
                read++;
            }

            long stop = new java.util.Date().getTime();

            if (percent) System.out.println ("100");
            if (spin) System.out.println ("");
            if (spin || percent)
            {
                long seconds = (stop - start) / 1000;
                long BPS = read / (seconds == 0 ? 1 : seconds);

                if (BPS > 1000000000000000L)
                    System.out.println (BPS/1000000000000000L + " EBps");
                else if (BPS > 1000000000000L)
                    System.out.println (BPS/1000000000000L + " TBps");
                else if (BPS > 1000000000)
                    System.out.println (BPS/1000000000 + " GBps");
                else if (BPS > 1000000)
                    System.out.println (BPS/1000000 + " MBps");
                else if (BPS > 1000)
                    System.out.println (BPS/1000 + " KBps");
                else
                    System.out.println (BPS + " Bps");
            }

            if (save) snooze (args.PauseAfterSave);

            // logger.finest ("bytes read: " + read);
            // logger.finest ("bytes written: " + written);

            if (examine)
                addToURLs (url, ((DelimitedBufferedInputStream) is).
                   getStrings());
        }
        catch (IOException e)
        {
            logger.throwing (e);
            return (false);
        }

        return (true);
    }

    private void fetchOne (boolean examine, boolean save, YouAreEll yrl,
        InputStream is)
    {
        logger.entering (examine);
        logger.entering (save);
        logger.entering (yrl);

        if (examine)
            is = new DelimitedBufferedInputStream (is, '<', '>');

        BufferedOutputStream bos = null;
        WebFile wf = null;
        if (save)
        {
            wf = new WebFile (yrl, args);
            bos = wf.getBOS();

            if (bos == null)
                save = false;
        }

        if (save || examine)
        {
            if (! examineORsave (yrl, is, bos, examine, save, yrl.getURL()))
            {
                logger.severe ("examineORsave failed");
            }
        }

        if (bos != null)
        {
            try
            {
                bos.close();
                if (args.SetLastModified)
                    wf.getFile().setLastModified (yrl.getLastModified());
            }
            catch (IOException e)
            {
                logger.throwing (e);
            }
        }

        if (examine)
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                logger.throwing (e);
            }
        }
    }

    /*
    ** calculate, given the examine/ignore and save/refuse values, whether
    ** to examine and/or save s.
    */
    private boolean[] EISR (String s, String which,
        String examine[], String ignore[], String save[], String refuse[])
    {
        if (s == null)
            return (null);

        logger.fine (s);
        logger.fine (which);
        logger.fine (java.util.Arrays.toString (examine));
        logger.fine (java.util.Arrays.toString (ignore));
        logger.fine (java.util.Arrays.toString (save));
        logger.fine (java.util.Arrays.toString (refuse));

        boolean E = Utils.blurf (examine, ignore, s, false);
        boolean S = Utils.blurf (save, refuse, s, false);

        if (args.PrintExamine && E)
                logger.info ("Examining " + which + ": " + s);
        if (args.PrintIgnore && !E)
                logger.info ("Ignoring " + which + ": " + s);

        if (args.PrintSave && S)
                logger.info ("Saving " + which + ": " + s);
        if (args.PrintRefuse && !S)
                logger.info ("Refusing " + which + ": " + s);

        boolean ret[] = new boolean[2]; 
        ret[0] = E;
        ret[1] = S;
        return (ret);
    }

    // accept everything we examine or save
    // reject everything we ignore or refuse

    private void fetch (String url)
    {
        logger.entering (url);

        boolean Path = args.PathExamine != null || args.PathIgnore != null ||
            args.PathSave != null || args.PathRefuse != null;
        boolean MIME = args.MIMEExamine != null || args.MIMEIgnore != null ||
            args.MIMESave != null || args.MIMERefuse != null;

        logger.fine ("Path = " + Path);
        logger.fine ("MIME = " + MIME);

        boolean tb[] = EISR (url, "path", args.PathExamine, args.PathIgnore,
            args.PathSave, args.PathRefuse);

        boolean Pexamine = tb[0];
        boolean Psave = tb[1];

        /*
         * if there is no MIME, and the Path doesn't say to examine or save,
         * we're done.
         */
        if (!MIME && !Pexamine && !Psave)
            return;

        /*
         * otherwise, we need to Path examine or save, or we need the MIME
         * header.  in either case, we need an InputStream.
         */
        InputStream is = null;
        YouAreEll yrl = null;
        for (int t = 0; t < args.Tries; t++)
        {
            yrl = new YouAreEll (url, urls, cookies, args);
            is = yrl.getInputStream();
            if (is != null)
                break;
            if (args.Tries > 1)
                logger.warning ("Trying again");
        }

        if (is == null)
            return;

        boolean Mexamine = false;
        boolean Msave = false;

        if (MIME && yrl.getContentType() != null)
        {
            tb = EISR (yrl.getContentType(), "MIME",
                args.MIMEExamine, args.MIMEIgnore,
                    args.MIMESave, args.MIMERefuse);
            Mexamine = tb[0];
            Msave = tb[1];
        }

        // we've looked at both Path and now MIME and there's nothing to do
        if (!Pexamine && !Psave && !Mexamine && !Msave)
            return;

        fetchOne (Pexamine || Mexamine, Psave || Msave, yrl, is);

        try
        {
            is.close();
        }
        catch (IOException IOE)
        {
            logger.throwing (IOE);
        }
    }

    private void fetchAll()
    {
        boolean done = false;

        while (!done)
        {
            done = true;

            for (Enumeration e = urls.keys(); e.hasMoreElements();)
            {
                String total = (String) e.nextElement();
                boolean fetched = ((Boolean) urls.get (total)).booleanValue();

                done &= fetched;

                if (!fetched)
                {
                    fetch (total);
                    urls.put (total, new Boolean (true));

                    snooze (args.PauseBetween);
                }
            }
        }
    }

    void doit ()
    {
        String username = args.Username;
        String password = args.Password;
        if (username != null || password != null)
            Authenticator.setDefault (new MyAuthenticator (username, password));

        System.setProperty ("java.protocol.handler.pkgs", "edu.mscd.cs");

        String[] add = args.additional;

        if (add == null)
        {
            logger.warning ("No URLs specified, exiting");
            System.exit (1);
        }

        /*
        ** add the specified URLs to the list to be fetched.
        */
        String[] t = new String[add.length];
        for (int i = 0; i < add.length; i++)
        {
            t[i] = "<a href=\"" + add[i] + "\">";
        }

        /*
        ** add to the URLs, with no base
        */
        addToURLs (null, t);
        fetchAll();

        /*
        ** shall we save the cookies to a file?
        */
        String savecookies = args.SaveCookies;
        if (savecookies != null)
            cookies.saveCookies (savecookies);
    }

    public static void main (String[] arguments) throws FileNotFoundException
    {
        REplican r = new REplican (arguments);
        r.doit();
    }
}
