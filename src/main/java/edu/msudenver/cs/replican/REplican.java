package edu.msudenver.cs.replican;

import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

import java.net.Authenticator;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import edu.msudenver.cs.jclo.JCLO;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html

public class REplican implements Runnable {
    static final Logger logger = LogManager.getLogger("REplican");
    static final REplicanArgs args = new REplicanArgs();
    static Map<String, Boolean> urls = new ConcurrentHashMap<>();
    private final Cookies cookies = new Cookies();
    private int URLcount = 0;

    //THREADSAFE_LEVEL_GREY
    //read/write collision?
    private void loadCookies() {
        for (String cookieFile : args.LoadCookies) {
            logger.info("Loading cookies from " + cookieFile);
            cookies.loadNetscapeCookies(cookieFile);
        }
    }
    //THREADSAFE_LEVEL_GREY
    private void loadPlistCookies() {
        for (String cookieFile : args.PlistCookies) {
            logger.info("Loading cookies from " + cookieFile);
            new Plist("file:" + cookieFile, cookies);
        }
    }

    @SuppressWarnings("unchecked")
    //THREADSAFE_LEVEL_GREY
    private void readCheckpointFile() {
        logger.info("Loading urls from " + args.CheckpointFile);

        try {
            ObjectInputStream ois =
                    new ObjectInputStream(
                            new FileInputStream(args.CheckpointFile));
            urls = (Hashtable<String, Boolean>) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException ioe) {
            logger.throwing(ioe);
        }
    }


    private void setLogLevel() {
        Level level = Level.OFF;

        if (args.logLevel == null) {
            level = Level.WARN;
        } else {
            switch (args.logLevel) {
                case OFF:
                    level = Level.OFF;
                    break;
                case FATAL:
                    level = Level.FATAL;
                    break;
                case ERROR:
                    level = Level.ERROR;
                    break;
                case WARN:
                    level = Level.WARN;
                    break;
                case INFO:
                    level = Level.INFO;
                    break;
                case DEBUG:
                    level = Level.DEBUG;
                    break;
                case TRACE:
                    level = Level.TRACE;
                    break;
                case ALL:
                    level = Level.ALL;
                    break;
            }
        }

        Configurator.setLevel("REplican", level);
    }

    //THREADSAFE_LEVEL_GREY
    private String escapeURL(String URL) {
        logger.traceEntry(URL);

        for (char c : "^.[]$()|*+?{}".toCharArray()) {
            URL = URL.replaceAll("\\" + c, "\\\\" + c);
        }

        logger.traceExit(URL);
        return (URL);
    }

    //THREADSAFE_LEVEL_GREY
    private void setDefaults() {
        if (args.Interesting == null) {
            String urlref = "\\s*=\\s*[\"']?([^\"'>]*)";
            String href = "[hH][rR][eE][fF]";
            String src = "[sS][rR][cC]";

            args.Interesting = new String[]{
                    href + urlref,
                    src + urlref,
            };
        }

        if (args.URLFixUp == null) {
            // so, i don't remember why i collapsed multiple spaces and
            // removed \'s. must have been important and i should have
            // documented. 's confuse URLs...
            // args.URLFixUp = new String[]{"\\s+", " ", "\\\\", ""};
            args.URLFixUp = new String[]{"\\s+", " ", "\\\\", "",
                    "\'", "%27"};
        }

        // if they don't specify anything, look at only text.
        if (args.MIMEExamine == null && args.MIMEIgnore == null &&
                args.PathExamine == null && args.PathIgnore == null) {
            args.MIMEExamine = new String[]{"text/.*"};
            if (args.PrintExamine)
                logger.warn("--MIMEExamine=" +
                        java.util.Arrays.toString(args.MIMEExamine));
        }

        // if they don't specify anything, save only what is specified on
        // the command line.
        if (args.MIMESave == null && args.MIMERefuse == null &&
                args.PathSave == null && args.PathRefuse == null) {
            if (args.additional.length == 0) {
                logger.error("No URLs specified");
                System.exit(1);
            }

            args.PathSave = new String[args.additional.length];

            for (int i = 0; i < args.additional.length; i++)
                args.PathSave[i] = escapeURL(args.additional[i]);

            if (args.PrintSave)
                logger.warn("--PathSave=" +
                        java.util.Arrays.toString(args.PathSave));
        }

        if (args.PrintAll)
            args.PrintAccept = args.PrintReject =
                    args.PrintSave = args.PrintRefuse =
                            args.PrintExamine = args.PrintIgnore =
                                    args.PrintRedirects = true;

        /*
        ** make sure we accept everything we examine, save, and the initial
        ** URLs
        */
        args.PathAccept = Utils.combineArrays(args.PathAccept,
                args.PathExamine);
        args.PathAccept = Utils.combineArrays(args.PathAccept,
                args.PathSave);
        args.PathAccept = Utils.combineArrays(args.PathAccept,
                args.additional);
    }

    /**
     * look for "interesting" parts of a HTML string.  interesting thus far
     * means href's, src's, img's etc.
     *
     * @param s the string to examine
     * @return the interesting part if any, and null if none
     */

    //THREADSAFE_LEVEL_GREY
    private String[] interesting(String s) {
        logger.traceEntry(s);

        if (s == null)
            return (null);

        String m[] = new String[args.Interesting.length];

        for (int i = 0; i < args.Interesting.length; i++) {
            m[i] = match(args.Interesting[i], s);
        }

        return (m);
    }
    //THREADSAFE_LEVEL_GREY
    private void checkpoint() {
        String checkpointFile = args.CheckpointFile;

        logger.trace("writing to " + checkpointFile);

        try {
            ObjectOutputStream oos =
                    new ObjectOutputStream(new FileOutputStream(checkpointFile));
            oos.writeObject(urls);
            oos.close();
        } catch (IOException e) {
            logger.throwing(e);
        }
    }

    /*
    ** add a URL to the list of those to be processed
    */
    //THREADSAFE_LEVEL_GREY
    private void addOne(String total) {
        logger.traceEntry(total);

        urls.put(total, Boolean.FALSE);

        URLcount++;

        int checkpointEvery = args.CheckpointEvery;
        if (checkpointEvery != 0 && URLcount % checkpointEvery == 0)
            checkpoint();
    }

    /*
    ** create a valid URL, paying attenting to a base if there is one.
    */
    //THREADSAFE_LEVEL_GREY
    private URL makeURL(String baseURL, String s) {
        logger.traceEntry(baseURL);
        logger.traceEntry(s);

        URL u = null;

        try {
            if (baseURL != null)
                u = new URL(new URL(baseURL), s);
            else
                u = new URL(s);
        } catch (MalformedURLException e) {
            logger.throwing(e);
        }

        logger.traceExit(u);
        return (u);
    }

    /*
    ** In the given string s, look for pattern.  If found, return the
    ** concatenation of the capturing groups.
    */
    //THREADSAFE_LEVEL_GREY
    private String match(String pattern, String s) {
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
    //THREADSAFE_LEVEL_GREY
    private String newBase(String base) {
        logger.traceEntry(base);

        if (base == null)
            return (null);

        String b = "<[bB][aA][sS][eE].*[hH][rR][eE][fF]=[\"']?([^\"'# ]*)";
        String ret = match(b, base);

        logger.traceExit(ret);
        return (ret);
    }

    // Process a single URL and see if we need to add it to the todo
    // list.
    //THREADSAFE_LEVEL_GREY
    private void process(String total) {
        String PathAccept[] = args.PathAccept;
        String PathReject[] = args.PathReject;

        boolean accept = Utils.blurf(PathAccept, PathReject, total, true);

        if (args.PrintAccept && accept) logger.info("Accepting path: " + total);
        if (args.PrintReject && !accept) logger.info("Rejecting path: " + total);

        if (accept) {
            if (args.URLRewrite != null)
                total = Utils.replaceAll(total, args.URLRewrite);

            // if we don't already have it
            if (urls.get(total) == null) {
                if (args.PrintAdd)
                    logger.info("Adding: " + total);
                addOne(total);
            }
        }
    }
    //THREADSAFE_LEVEL_GREY
    private void addToURLs(String baseURL, List<String> strings) {
        logger.traceEntry(baseURL);
        logger.traceEntry(strings.toString());

        for (String s : strings) {
            String next = Utils.replaceAll(s, args.URLFixUp);

            // is this resetting the base?
            String newBase = newBase(next);
            if (newBase != null) {
                logger.debug("Setting base to " + baseURL);
                baseURL = newBase;
            }

            for (String possible : interesting(next)) {
                if (possible != null) {
                    URL u = makeURL(baseURL, possible);

                    if (u == null)
                        continue;

                    String total = u.toString();
                    process(total);
                }
            }
        }
    }

    private void snooze(int milliseconds) {
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

    private String speed(long start, long stop, long read) {
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
    ** read from an input stream, optionally write to an output stream, and
    ** optionally look at all the URL's found in the input stream.
    */

    //THREADSAFE_LEVEL_GREY
    //InputStream OutputStream?
    private boolean examineORsave(YouAreEll yrl, InputStream is,
                                  BufferedOutputStream bos, boolean examine, boolean save, String url) {
        // logger.traceEntry ((Message) is);
        // logger.traceEntry ((Message) bos);
        logger.traceEntry(String.valueOf(examine));
        logger.traceEntry(String.valueOf(save));
        logger.traceEntry(url);

        try {
            long read = 0;
            long written = 0;
            long content_length = yrl.getContentLength();
            long ten_percent = content_length > 0 ? content_length / 10 : 0;
            long count = 1;
            boolean percent = args.SaveProgress && save && ten_percent > 0;
            boolean spin = args.SaveProgress && save && ten_percent == 0;
            long start = new java.util.Date().getTime();

            if (percent) System.out.print("0..");
            if (spin) System.out.print("|");

            int c;
            while ((c = is.read()) != -1) {
                if (save) {
                    bos.write((char) c);
                    written++;

                    if (percent && count < 10 && written > count * ten_percent) {
                        System.out.print(count * 10 + "..");
                        count++;
                    }
                    // spin every 1000 bytes read -- we don't know how long
                    // the file is.
                    else if (spin && written % 1000 == 0) {
                        // it'd be nice if Java know a long % 4 will always
                        // be between 0 and 3 -- an integer...
                        int where = (int) count % 4;
                        System.out.print("\b" + "|/-\\".charAt(where));
                        count++;
                    }
                }
                read++;
            }

            long stop = new java.util.Date().getTime();

            if (percent) System.out.println("100");
            if (spin) System.out.println("");
            if (spin || percent) {
                System.out.println(speed(start, stop, read));
            }

            if (save && args.PauseAfterSave != 0) snooze(args.PauseAfterSave);

            if (examine)
                addToURLs(url, ((DelimitedBufferedInputStream) is).getStrings());
        } catch (IOException e) {
            logger.throwing(e);
            return (false);
        }

        return (true);
    }

    //THREADSAFE_LEVEL_BLACK
    private void fetchOne(boolean examine, boolean save, YouAreEll yrl,
                          InputStream is) {
        logger.traceEntry(String.valueOf(examine));
        logger.traceEntry(String.valueOf(save));
        logger.traceEntry(yrl.toString());

        if (examine)
            is = new DelimitedBufferedInputStream(is, '<', '>');

        BufferedOutputStream bos = null;
        WebFile wf = null;
        if (save) {
            wf = new WebFile(yrl, args);
            bos = wf.getBOS();

            if (bos == null)
                save = false;
        }

        if (save || examine) {
            if (!examineORsave(yrl, is, bos, examine, save, yrl.getURL())) {
                logger.error("examineORsave failed");
            }
        }

        if (bos != null) {
            try {
                bos.close();
                if (args.SetLastModified)
                    if (!wf.getFile().setLastModified(yrl.getLastModified()))
                        logger.warn("Couldn't set last modified");
            } catch (IOException e) {
                logger.throwing(e);
            }
        }

        if (examine) {
            try {
                is.close();
            } catch (IOException e) {
                logger.throwing(e);
            }
        }
    }

    /*
    ** calculate, given the examine/ignore and save/refuse values, whether
    ** to examine and/or save s.
    */
    //THREADSAFE_LEVEL_BLACK
    private boolean[] EISR(String s, String which,
                           String examine[], String ignore[], String save[], String refuse[]) {
        if (s == null)
            return (null);

        logger.debug(s);
        logger.debug(which);
        logger.debug(java.util.Arrays.toString(examine));
        logger.debug(java.util.Arrays.toString(ignore));
        logger.debug(java.util.Arrays.toString(save));
        logger.debug(java.util.Arrays.toString(refuse));

        boolean E = Utils.blurf(examine, ignore, s, false);
        boolean S = Utils.blurf(save, refuse, s, false);

        if (args.PrintExamine && E)
            logger.info("Examining " + which + ": " + s);
        if (args.PrintIgnore && !E)
            logger.info("Ignoring " + which + ": " + s);

        if (args.PrintSave && S)
            logger.info("Saving " + which + ": " + s);
        if (args.PrintRefuse && !S)
            logger.info("Refusing " + which + ": " + s);

        boolean ret[] = new boolean[2];
        ret[0] = E;
        ret[1] = S;
        return (ret);
    }

    // accept everything we examine or save
    // reject everything we ignore or refuse

    //THREADSAFE_LEVEL_GRAY
    private void fetch(String url) {
        logger.traceEntry(url);

        boolean Path = args.PathExamine != null || args.PathIgnore != null ||
                args.PathSave != null || args.PathRefuse != null;
        boolean MIME = args.MIMEExamine != null || args.MIMEIgnore != null ||
                args.MIMESave != null || args.MIMERefuse != null;

        logger.debug("Path = " + Path);
        logger.debug("MIME = " + MIME);

        boolean tb[] = EISR(url, "path", args.PathExamine, args.PathIgnore,
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
        for (int t = 0; t < args.Tries; t++) {
            yrl = new YouAreEll(url, cookies);
            is = yrl.getInputStream();
            if (is != null)
                break;
            if (args.Tries > 1)
                logger.warn("Trying again");
        }

        if (is == null)
            return;

        boolean Mexamine = false;
        boolean Msave = false;

        if (MIME && yrl.getContentType() != null) {
            tb = EISR(yrl.getContentType(), "MIME",
                    args.MIMEExamine, args.MIMEIgnore,
                    args.MIMESave, args.MIMERefuse);
            Mexamine = tb[0];
            Msave = tb[1];
        }

        // we've looked at both Path and now MIME and there's nothing to do
        if (!Pexamine && !Psave && !Mexamine && !Msave)
            return;

        fetchOne(Pexamine || Mexamine, Psave || Msave, yrl, is);

        try {
            is.close();
        } catch (IOException IOE) {
            logger.throwing(IOE);
        }
    }
    //THREADSAFE_LEVEL_GREY
    private void fetchAll() {
        boolean done = false;

        while (!done) {
            done = true;

            for (String url : urls.keySet()) {
                boolean fetched = urls.get(url);

                done &= fetched;

                if (!fetched) {
                    fetch(url);
                    urls.put(url, true);
                    if (args.PauseBetween != 0)
                        snooze(args.PauseBetween);
                }
            }
        }
    }
    //THREADSAFE_LEVEL_GREY
    //add[] array?
    private void doit() {
        String username = args.Username;
        String password = args.Password;
        if (username != null || password != null)
            Authenticator.setDefault(new MyAuthenticator(username, password));

        // this is for tests using
        // System.setProperty ("java.protocol.handler.pkgs", "edu.msudenver.cs");

        String[] add = args.additional;

        if (add == null) {
            logger.warn("No URLs specified, exiting");
            System.exit(1);
        }

        /*
        ** add the specified URLs to the list to be fetched.
        */
        // String[] t = new String[add.length];
        List<String> t = new ArrayList<>();
        for (String s : add) {
            t.add("<a href=\"" + s + "\">");
        }

        /*
        ** add to the URLs, with no base
        */
        addToURLs(null, t);
        fetchAll();

        /*
        ** shall we save the cookies to a file?
        */
        String savecookies = args.SaveCookies;
        if (savecookies != null)
            cookies.saveNetscapeCookies(savecookies);
    }

    public static void main(String[] arguments) throws FileNotFoundException {

        /**
         * Fixed number of threads in thread pool
         * to be pulled in from JCLO eventually
         * (right now set to 4)
         */
         final int MAX_T = 4;

        /**
         * Four steps for threading
         *  - create a task
         *  - create an executor pool
         *  - pass the tasks to the executor
         *  - shutdown the pool
         */

        JCLO jclo = new JCLO(args);

        if (arguments.length == 0) {
            System.out.println("Arguments:\n" + jclo.usage() + "URLs...");
            System.exit(1);
        }

        try {
            jclo.parse(arguments);
        } catch (IllegalArgumentException IAE) {
            System.err.println(IAE);
            System.err.println("Arguments:\n" + jclo.usage() + "URLs...");
            System.exit(1);
        }

        if (args.Version) {
            System.out.println(Version.getVersion());
            System.exit(0);
        }

        if (args.Help) {
            System.out.println("Arguments:\n" + jclo.usage() + "URLs...");
            System.exit(0);
        }

        Runnable runnableREplican = new REplican();
        ExecutorService threadPool = Executors.newFixedThreadPool(args.Threads);
        threadPool.execute(runnableREplican);
        threadPool.shutdown();

        /** Moved to run() for the moment
         REplican r = new REplican();
         r.setLogLevel();
         r.setDefaults();



        if (args.LoadCookies != null) r.loadCookies();
        if (args.PlistCookies != null) r.loadPlistCookies();
        if (args.CheckpointEvery != 0) r.readCheckpointFile();

        r.doit();
         */
    }

    // Needed to implement Runnable
    @Override
    public void run() {
        try{
            REplican r = new REplican();
            r.setLogLevel();
            r.setDefaults();



            if (args.LoadCookies != null) r.loadCookies();
            if (args.PlistCookies != null) r.loadPlistCookies();
            if (args.CheckpointEvery != 0) r.readCheckpointFile();

            r.doit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }
}
