package edu.msudenver.cs.replican;

import edu.msudenver.cs.jclo.JCLO;
import lombok.NonNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.*;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html
public class REplican {
    static final Logger logger = LogManager.getLogger();
    static final REplicanArgs args = new REplicanArgs();
    static Map<String, Boolean> urls = new ConcurrentHashMap<>();
    static final Cookies cookies = new Cookies();
    private static int URLcount = 0;

    // turn on assert for every class *but this one*.
    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

    private static void loadNetscapeCookies() {
        for (String cookieFile : args.NetscapeCookies) {
            try {
                NetscapeCookies.loadCookies(cookieFile);
            } catch (IOException IOE) {
                logger.throwing(IOE);
            }
        }
    }

    private static void loadPlistCookies() {
        for (String cookieFile : args.PlistCookies) {
            logger.info("Loading cookies from " + cookieFile);
            new Plist("file:" + cookieFile, cookies);
        }
    }

    private static void loadFirefoxCookies() {
        for (String cookieFile : args.FirefoxCookies) {
            logger.info("Loading cookies from " + cookieFile);
            FirefoxCookies.loadCookies(cookieFile);
        }
    }

    @SuppressWarnings("unchecked")
    private static void readCheckpointFile() {
        logger.info("Loading urls from " + args.CheckpointFile);

        try {
            ObjectInputStream ois =
                    new ObjectInputStream(
                            new FileInputStream(args.CheckpointFile));
            urls = (Map<String, Boolean>) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException ioe) {
            logger.throwing(ioe);
        }
    }


    private static void setLogLevel() {
        Level level = Level.OFF;

        if (args.logLevel == null) {
            level = Level.WARN;
        } else switch (args.logLevel) {
            case FATAL: level = Level.FATAL; break;
            case ERROR: level = Level.ERROR; break;
            case WARN: level = Level.WARN; break;
            case INFO: level = Level.INFO; break;
            case DEBUG: level = Level.DEBUG; break;
            case TRACE: level = Level.TRACE; break;
            case ALL: level = Level.ALL; break;
        }

        Configurator.setLevel(logger.getName(), level);
    }

    private static String escapeURL(@NonNull String URL) {
        logger.traceEntry(URL);

        for (char c : "^.[]$()|*+?{}".toCharArray()) {
            URL = URL.replaceAll("\\" + c, "\\\\" + c);
        }

        logger.traceExit(URL);
        return (URL);
    }

    private static void setDefaults() {
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
            if (args.additional == null) {
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
        args.PathAccept = Utils.combineArrays(args.PathAccept, args.PathExamine);
        args.PathAccept = Utils.combineArrays(args.PathAccept, args.PathSave);
        args.PathAccept = Utils.combineArrays(args.PathAccept, args.additional);
    }

    private static void checkpoint() {
        String checkpointFile = args.CheckpointFile;

        logger.trace("writing to " + checkpointFile);

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(checkpointFile));
            oos.writeObject(urls);
            oos.close();
        } catch (IOException e) {
            logger.throwing(e);
        }
    }

    /*
    ** add a URL to the list of those to be processed
    */
    private static void addOne(@NonNull final String total) {
        logger.traceEntry(total);

        urls.putIfAbsent(total, false);
        URLcount++;

        int checkpointEvery = args.CheckpointEvery;
        if (checkpointEvery != 0 && URLcount % checkpointEvery == 0)
            checkpoint();
    }

    /*
    ** create a valid URL, paying attention to a base if there is one.
    */
    private static URL makeURL(final String baseURL, @NonNull final String s) {
        logger.traceEntry(baseURL);
        logger.traceEntry(s);

        URL u = null;

        try {
            if (baseURL != null)
                u = new URL(new URL(baseURL), s);
            else
                u = new URL(s);
        } catch (MalformedURLException e) {
            logger.debug(e.getMessage());
        }

        logger.traceExit(u);
        return (u);
    }

    // Process a single URL and see if we need to add it to the to do
    // list.
    private static void process(@NonNull String total) {
        boolean accept = Utils.blurf(args.PathAccept, args.PathReject, total, true);

        if (args.PrintAccept && accept) {
            logger.info("Accepting path: " + total);
        }

        if (args.PrintReject && !accept) {
            logger.info("Rejecting path: " + total);
        }

        if (accept) {
            if (args.URLRewrite != null)
                total = Utils.replaceAll(total, args.URLRewrite);

            if (urls.get(total) == null) {
                if (args.PrintAdd)
                    logger.info("Adding: " + total);
                addOne(total);
            }
        }
    }

    private static void addToURLs(String baseURL, @NonNull final List<String> strings) {
        logger.traceEntry(baseURL);
        logger.traceEntry(strings.toString());

        for (String s : strings) {
            String next = Utils.replaceAll(s, args.URLFixUp);

            // is this resetting the base?
            String newBase = Utils.newBase(next);
            if (newBase != null) {
                logger.debug("Setting base to " + baseURL);
                baseURL = newBase;
            }

            for (String possible : Utils.interesting(next)) {
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

    /*
    ** read from an input stream, optionally write to an output stream, and
    ** optionally look at all the URL's found in the input stream.
    ** null: yrl, bos
    */
    private static boolean examineORsave(final YouAreEll yrl, final InputStream is, final BufferedOutputStream bos, final boolean examine, final boolean save, final String url) {
        // System.err.println("yrl = " + yrl);
        // System.err.println("is = " + is);
        // System.err.println("bos = " + bos);


        logger.traceEntry(String.valueOf(examine));
        logger.traceEntry(String.valueOf(save));
        logger.traceEntry(url);

        try {
            readAndWrite(is, bos, save, yrl.getContentLength());

            if (save && args.PauseAfterSave != 0) {
                Utils.snooze(args.PauseAfterSave);
            }

            if (examine) {
                addToURLs(url, ((DelimitedBufferedInputStream) is).getStrings());
            }
        } catch (IOException e) {
            logger.throwing(e);
            return (false);
        }

        return (true);
    }

    private static void readAndWrite(final InputStream is, final BufferedOutputStream bos, final boolean save, final long content_length) throws IOException {
        long ten_percent = content_length > 0 ? content_length / 10 : 0;
        boolean percent = args.SaveProgress && save && ten_percent > 0;
        boolean spin = args.SaveProgress && save && ten_percent == 0;
        long startTime = 0;

        if (spin || percent) {
            startTime = new java.util.Date().getTime();
        }

        if (percent) System.out.print("0..");
        if (spin) System.out.print("|");

        long written = 0;
        long read = 0;
        long count = 1;
        int c;
        while ((c = is.read()) != -1) {
            if (save) {
                bos.write((char) c);
                written++;

                if (percent && count < 10 && written > count * ten_percent) {
                    System.out.print(count * 10 + "..");
                    count++;
                } else if (spin && written % 1000 == 0) {
                    // spin every 1000 bytes read -- we don't know how long
                    // the file is.
                    // it'd be nice if Java know a long % 4 will always
                    // be between 0 and 3 -- an integer...
                    int where = (int) count % 4;
                    System.out.print("\b" + "|/-\\".charAt(where));
                    count++;
                }
            }
            read++;
        }

        if (spin || percent) {
            if (percent) {
                System.out.println("100");
            }
            if (spin) {
                System.out.println();
            }

            long stopTime = new java.util.Date().getTime();
            System.out.println(Utils.speed(startTime, stopTime, read));
        }
    }

    private static void fetchOne(final boolean examine, boolean save, @NonNull final YouAreEll yrl, @NonNull final InputStream is)
            throws MalformedURLException, FileNotFoundException {
        // System.err.println(is);
        logger.traceEntry(String.valueOf(examine));
        logger.traceEntry(String.valueOf(save));
        logger.traceEntry(yrl.toString());

        InputStream dbis = is;

        if (examine) {
            dbis = new DelimitedBufferedInputStream(is, '<', '>');
        }

        BufferedOutputStream bos = null;
        File webFile = null;
        if (save) {
            try {
                logger.debug("before calling createFile");
                webFile = new WebFile(yrl).createFile();
                logger.debug("after calling createFile");
                bos = new BufferedOutputStream(new FileOutputStream(webFile));
            } catch (FileSystemException FSE) {
                save = false;
            }
        }

        if (examine || save) {
            if (!examineORsave(yrl, dbis, bos, examine, save, yrl.getUrl())) {
                logger.error("examineORsave failed");
            }
        }

        if (bos != null) {
            try {
                bos.close();
                if (args.SetLastModified) {
                    if (!webFile.setLastModified(yrl.getLastModified())) {
                        logger.warn("Couldn't set last modified");
                    }
                }
            } catch (IOException e) {
                logger.throwing(e);
            }
        }

        if (examine) {
            try {
                dbis.close();
            } catch (IOException e) {
                logger.throwing(e);
            }
        }
    }

    /*
    ** calculate, given the examine/ignore and save/refuse values, whether
    ** to examine and/or save s.
    */
    private static boolean[] EISR(@NonNull final String s, @NonNull final String which, String[] examine, String[] ignore, String[] save, String[] refuse) {
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

        boolean[] ret = new boolean[2];
        ret[0] = E;
        ret[1] = S;
        return (ret);
    }

    // accept everything we examine or save
    // reject everything we ignore or refuse
    private static void fetch(@NonNull final String url) throws MalformedURLException, FileNotFoundException {
        logger.traceEntry(url);

        boolean Path = args.PathExamine != null || args.PathIgnore != null ||
                args.PathSave != null || args.PathRefuse != null;
        boolean MIME = args.MIMEExamine != null || args.MIMEIgnore != null ||
                args.MIMESave != null || args.MIMERefuse != null;

        logger.debug("Path = " + Path);
        logger.debug("MIME = " + MIME);

        boolean[] tb = EISR(url, "path", args.PathExamine, args.PathIgnore,
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
            yrl = new YouAreEll(url);
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
        if (!Pexamine && !Psave && !Mexamine && !Msave) {
            try {
                is.close();
            } catch (IOException IOE) {
                logger.throwing(IOE);
            }
            return;
        }

        fetchOne(Pexamine || Mexamine, Psave || Msave, yrl, is);

        try {
            is.close();
        } catch (IOException IOE) {
            logger.throwing(IOE);
        }
    }

    private static void fetchAll() {
        boolean done = false;

        while (!done) {
            done = true;

            for (String url : urls.keySet()) {
                boolean fetched = urls.get(url);

                done &= fetched;

                if (!fetched) {
                    try {
                        fetch(url);
                    } catch (MalformedURLException | FileNotFoundException e) {
                        logger.throwing(e);
                    }
                    urls.put(url, true);
                    if (args.PauseBetween != 0)
                        Utils.snooze(args.PauseBetween);
                }
            }
        }
    }

    private static void doit() {
        final String username = args.Username;
        final String password = args.Password;
        if (username != null || password != null)
            Authenticator.setDefault(new MyAuthenticator(username, password));

        // this is for tests using
        // System.setProperty ("java.protocol.handler.pkgs", "edu.msudenver.cs");

        final String[] add = args.additional;

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

        String savecookies = args.SaveCookies;
        if (savecookies != null) {
            try {
                NetscapeCookies.loadCookies(savecookies);
            } catch (IOException IOE) {
                logger.throwing(IOE);
            }
        }
        */
    }

    public static void main(String[] arguments) {

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

        setLogLevel();
        setDefaults();

        if (args.FirefoxCookies != null) loadFirefoxCookies();
        if (args.NetscapeCookies != null) loadNetscapeCookies();
        if (args.PlistCookies != null) loadPlistCookies();

        if (args.CheckpointEvery != 0) readCheckpointFile();

        doit();
    }

}
