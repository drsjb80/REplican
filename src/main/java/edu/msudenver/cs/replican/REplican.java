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
import java.util.concurrent.atomic.AtomicInteger;

// http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html
class REplican {
    static final Logger LOGGER = LogManager.getLogger();
    static final REplicanArgs ARGS = new REplicanArgs();
    static Map<String, Boolean> urls = new ConcurrentHashMap<>();
    static final Cookies COOKIES = new Cookies();
    static AtomicInteger URLcount = new AtomicInteger(0);

    // turn on assert for every class *but this one*.
    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

    private static void loadNetscapeCookies() {
        for (String cookieFile : ARGS.NetscapeCookies) {
            try {
                NetscapeCookies.loadCookies(cookieFile);
            } catch (IOException IOE) {
                LOGGER.throwing(IOE);
            }
        }
    }

    private static void loadPlistCookies() {
        for (String cookieFile : ARGS.PlistCookies) {
            LOGGER.info("Loading cookies from " + cookieFile);
            new Plist("file:" + cookieFile, COOKIES);
        }
    }

    private static void loadFirefoxCookies() {
        for (String cookieFile : ARGS.FirefoxCookies) {
            LOGGER.info("Loading cookies from " + cookieFile);
            FirefoxCookies.loadCookies(cookieFile);
        }
    }

    @SuppressWarnings("unchecked")
    private static void readCheckpointFile() {
        LOGGER.info("Loading urls from " + ARGS.CheckpointFile);

        try {
            final ObjectInputStream ois =
                    new ObjectInputStream(
                            new FileInputStream(ARGS.CheckpointFile));
            urls = (Map<String, Boolean>) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException ioe) {
            LOGGER.throwing(ioe);
        }
    }


    private static void setLogLevel() {
        Level level = Level.OFF;

        if (ARGS.LogLevel == null) {
            level = Level.WARN;
        } else {
            switch (ARGS.LogLevel) {
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

        Configurator.setLevel(LOGGER.getName(), level);
    }

    private static String escapeURL(@NonNull final String URL) {
        LOGGER.traceEntry(URL);
        String escapedURL = URL;

        for (char c : "^.[]$()|*+?{}".toCharArray()) {
            escapedURL = escapedURL.replaceAll("\\" + c, "\\\\" + c);
        }

        LOGGER.traceExit(escapedURL);
        return escapedURL;
    }

    private static void setDefaults() {
        if (ARGS.Interesting == null) {
            final String urlref = "\\s*=\\s*[\"']?([^\"'>]*)";
            final String href = "[hH][rR][eE][fF]";
            final String src = "[sS][rR][cC]";

            ARGS.Interesting = new String[]{href + urlref, src + urlref};
        }

        if (ARGS.URLFixUp == null) {
            // so, i don't remember why i collapsed multiple spaces and
            // removed \'s. must have been important and i should have
            // documented. 's confuse URLs...
            // args.URLFixUp = new String[]{"\\s+", " ", "\\\\", ""};
            ARGS.URLFixUp = new String[]{"\\s+", " ", "\\\\", "", "\'", "%27"};
        }

        // if they don't specify anything, look at only text.
        if (ARGS.MIMEExamine == null
                && ARGS.MIMEIgnore == null
                && ARGS.PathExamine == null
                && ARGS.PathIgnore == null) {
            ARGS.MIMEExamine = new String[]{"text/.*"};
            if (ARGS.PrintExamine) {
                LOGGER.warn("--MIMEExamine=" + java.util.Arrays.toString(ARGS.MIMEExamine));
            }
        }

        // if they don't specify anything, save only what is specified on
        // the command line.
        if (ARGS.MIMESave == null
                && ARGS.MIMERefuse == null
                && ARGS.PathSave == null
                && ARGS.PathRefuse == null) {
            if (ARGS.additional == null) {
                LOGGER.error("No URLs specified");
                System.exit(1);
            }

            ARGS.PathSave = new String[ARGS.additional.length];

            for (int i = 0; i < ARGS.additional.length; i++) {
                ARGS.PathSave[i] = escapeURL(ARGS.additional[i]);
            }

            if (ARGS.PrintSave) {
                LOGGER.warn("--PathSave=" + java.util.Arrays.toString(ARGS.PathSave));
            }
        }

        if (ARGS.PrintAll) {
            ARGS.PrintAccept = ARGS.PrintReject =
                    ARGS.PrintSave = ARGS.PrintRefuse =
                            ARGS.PrintExamine = ARGS.PrintIgnore =
                                    ARGS.PrintRedirects = true;
        }

        /*
         ** make sure we accept everything we examine, save, and the initial
         ** URLs
         */
        ARGS.PathAccept = Utils.combineArrays(ARGS.PathAccept, ARGS.PathExamine);
        ARGS.PathAccept = Utils.combineArrays(ARGS.PathAccept, ARGS.PathSave);
        ARGS.PathAccept = Utils.combineArrays(ARGS.PathAccept, ARGS.additional);
    }

    private static void checkpoint() {
        final String checkpointFile = ARGS.CheckpointFile;

        LOGGER.trace("writing to " + checkpointFile);

        try {
            final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(checkpointFile));
            oos.writeObject(urls);
            oos.close();
        } catch (IOException e) {
            LOGGER.throwing(e);
        }
    }

    // Process a single URL and see if we need to add it to the to do
    // list.
    private static void process(@NonNull String total) {
        LOGGER.traceEntry(total);
        final boolean accept = Utils.blurf(ARGS.PathAccept, ARGS.PathReject, total, true);

        if (ARGS.PrintAccept && accept) {
            LOGGER.info("Accepting path: " + total);
        }

        if (ARGS.PrintReject && !accept) {
            LOGGER.info("Rejecting path: " + total);
        }

        if (accept) {
            if (ARGS.URLRewrite != null) {
                total = Utils.replaceAll(total, ARGS.URLRewrite);
            }

            if (urls.putIfAbsent(total, false) == null) {
                if (ARGS.PrintAdd) {
                    LOGGER.info("Adding: " + total);
                }
                URLcount.incrementAndGet();
            }

            final int checkpointEvery = ARGS.CheckpointEvery;
            if (checkpointEvery != 0 && URLcount.get() % checkpointEvery == 0) {
                checkpoint();
            }
        }
    }

    // all all the URLs from a file.
    private static void addToURLs(final String baseURL, @NonNull final List<String> strings) {
        LOGGER.traceEntry(baseURL);
        LOGGER.traceEntry(strings.toString());

        for (String string : strings) {
            final String next = Utils.replaceAll(string, ARGS.URLFixUp);
            LOGGER.trace(next);

            final String newBase = Utils.newBase(next);
            if (newBase != null) {
                LOGGER.debug("Setting base to " + newBase);
            }

            for (String possible : Utils.interesting(next)) {
                LOGGER.trace(possible);
                if (possible != null) {
                    final URL u;
                    LOGGER.trace(newBase);
                    LOGGER.trace(baseURL);
                    try {
                        if (newBase != null) {
                            u = new URL(new URL(newBase), possible);
                        } else if (baseURL != null){
                            u = new URL(new URL(baseURL), possible);
                        } else {
                            u = new URL(possible);
                        }
                    } catch (MalformedURLException MUE) {
                        LOGGER.error(MUE);
                        continue;
                    }

                    process(u.toString());
                }
            }
        }
    }

    /*
     ** read from an input stream, optionally write to an output stream, and
     ** optionally look at all the URL's found in the input stream.
     ** null: yrl, bos
     */
    private static void examineORsave(final YouAreEll yrl, final InputStream is, final BufferedOutputStream bos, final boolean examine, final boolean save, final String url) throws IOException {
        LOGGER.traceEntry(String.valueOf(examine));
        LOGGER.traceEntry(String.valueOf(save));
        LOGGER.traceEntry(url);

        readAndWrite(is, bos, save, yrl.getContentLength());

        if (save && ARGS.PauseAfterSave != 0) {
            Utils.snooze(ARGS.PauseAfterSave);
        }

        if (examine) {
            addToURLs(url, ((DelimitedBufferedInputStream) is).getStrings());
        }
    }

    private static void readAndWrite(final InputStream is, final BufferedOutputStream bos, final boolean save, final long content_length) throws IOException {
        final long ten_percent = content_length > 0 ? content_length / 10 : 0;
        final boolean percent = ARGS.SaveProgress && save && ten_percent > 0;
        final boolean spin = ARGS.SaveProgress && save && ten_percent == 0;
        final long startTime = startReadAndWrite(percent, spin);

        long written = 0;
        long read = 0;
        long count = 1;
        int c;
        while ((c = is.read()) != -1) {
            if (save) {
                bos.write((char) c);
                written++;

                count = pacifier(ten_percent, percent, spin, written, count);
            }
            read++;
        }

        finalizeReadAndWrite(percent, spin, startTime, read);
    }

    private static long pacifier(long ten_percent, boolean percent, boolean spin, long written, long count) {
        if (percent && count < 10 && written > count * ten_percent) {
            System.out.print(count * 10 + "..");
            count++;
        } else if (spin && written % 1000 == 0) {
            // spin every 1000 bytes read -- we don't know how long
            // the file is.
            // it'd be nice if Java know a long % 4 will always
            // be between 0 and 3 -- an integer...
            final int where = (int) count % 4;
            System.out.print("\b" + "|/-\\".charAt(where));
            count++;
        }
        return count;
    }

    private static long startReadAndWrite(boolean percent, boolean spin) {
        long startTime = 0L;
        if (spin || percent) {
            startTime = new java.util.Date().getTime();
        }

        if (percent) System.out.print("0..");
        if (spin) System.out.print("|");
        return startTime;
    }

    private static void finalizeReadAndWrite(final boolean percent, final boolean spin, final long startTime, final long read) {
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
        LOGGER.traceEntry(String.valueOf(examine));
        LOGGER.traceEntry(String.valueOf(save));
        LOGGER.traceEntry(yrl.toString());

        InputStream dbis = is;

        if (examine) {
            dbis = new DelimitedBufferedInputStream(is, '<', '>');
        }

        BufferedOutputStream bos = null;
        File webFile = null;
        if (save) {
            try {
                webFile = new WebFile(yrl).createFile();
                bos = new BufferedOutputStream(new FileOutputStream(webFile));
            } catch (FileSystemException FSE) {
                save = false;
            }
        }

        if (examine || save) {
            try {
                examineORsave(yrl, dbis, bos, examine, save, yrl.getUrl());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (bos != null) {
            try {
                bos.close();
                if (ARGS.SetLastModified) {
                    if (!webFile.setLastModified(yrl.getLastModified())) {
                        LOGGER.warn("Couldn't set last modified");
                    }
                }
            } catch (IOException e) {
                LOGGER.throwing(e);
            }
        }

        if (examine) {
            try {
                dbis.close();
            } catch (IOException e) {
                LOGGER.throwing(e);
            }
        }
    }

    /*
     ** calculate, given the examine/ignore and save/refuse values, whether
     ** to examine and/or save url.
     */
    private static Map.Entry<Boolean, Boolean> EISR(@NonNull final String url, @NonNull final String which, String[] examine, String[] ignore, String[] save, String[] refuse) {
        LOGGER.debug(url);
        LOGGER.debug(which);
        LOGGER.debug(java.util.Arrays.toString(examine));
        LOGGER.debug(java.util.Arrays.toString(ignore));
        LOGGER.debug(java.util.Arrays.toString(save));
        LOGGER.debug(java.util.Arrays.toString(refuse));

        boolean E = Utils.blurf(examine, ignore, url, false);
        boolean S = Utils.blurf(save, refuse, url, false);

        if (ARGS.PrintExamine && E) {
            LOGGER.info("Examining " + which + ": " + url);
        }
        if (ARGS.PrintIgnore && !E) {
            LOGGER.info("Ignoring " + which + ": " + url);
        }

        if (ARGS.PrintSave && S) {
            LOGGER.info("Saving " + which + ": " + url);
        }
        if (ARGS.PrintRefuse && !S) {
            LOGGER.info("Refusing " + which + ": " + url);
        }

        return (Map.entry(E, S));
    }

    // accept everything we examine or save
    // reject everything we ignore or refuse
    private static void fetch(@NonNull final String url) throws IOException {
        LOGGER.traceEntry(url);

        boolean Path = ARGS.PathExamine != null || ARGS.PathIgnore != null ||
                ARGS.PathSave != null || ARGS.PathRefuse != null;
        boolean MIME = ARGS.MIMEExamine != null || ARGS.MIMEIgnore != null ||
                ARGS.MIMESave != null || ARGS.MIMERefuse != null;

        LOGGER.debug("Path = " + Path);
        LOGGER.debug("MIME = " + MIME);

        Map.Entry<Boolean, Boolean> eisr = EISR(url, "path",
                ARGS.PathExamine, ARGS.PathIgnore,
                ARGS.PathSave, ARGS.PathRefuse);

        boolean pathExamine = eisr.getKey();
        boolean pathSave = eisr.getValue();

        /*
         * if there is no MIME, and the Path doesn't say to examine or save,
         * we're done.
         */
        if (!MIME && !pathExamine && !pathSave) {
            return;
        }

        YouAreEll yrl = new YouAreEll(url);
        /*
        ** quick check before doing anything across the network.
        */
        File file = new WebFile(yrl).openFile();
        if (file.exists() && !REplican.ARGS.Overwrite) {
            LOGGER.warn("Not overwriting: " + file);
            return;
        }

        InputStream is = yrl.getInputStream();
        if (is == null) {
            return;
        }

        boolean mineExamine = false;
        boolean mimeSave = false;

        if (MIME && yrl.getContentType() != null) {
            eisr = EISR(yrl.getContentType(), "MIME",
                    ARGS.MIMEExamine, ARGS.MIMEIgnore,
                    ARGS.MIMESave, ARGS.MIMERefuse);
            mineExamine = eisr.getKey();
            mimeSave = eisr.getValue();
        }

        // we've looked at both Path and now MIME and there's nothing to do
        if (!pathExamine && !pathSave && !mineExamine && !mimeSave) {
            is.close();
            return;
        }

        fetchOne(pathExamine || mineExamine, pathSave || mimeSave, yrl, is);

        is.close();
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
                    } catch (IOException e) {
                        LOGGER.throwing(e);
                    }
                    urls.put(url, true);
                    if (ARGS.PauseBetween != 0) {
                        Utils.snooze(ARGS.PauseBetween);
                    }
                }
            }
        }
    }

    private static void doit() {
        final String username = ARGS.Username;
        final String password = ARGS.Password;
        if (username != null || password != null) {
            Authenticator.setDefault(new MyAuthenticator(username, password));
        }

        final String[] add = ARGS.additional;

        if (add == null) {
            LOGGER.warn("No URLs specified, exiting");
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
        String[][] aliases =
                {{"PathDoNotAccept", "PathReject"}, {"PathDoNotSave", "PathRefuse"}, {"PathDoNotExamine", "PathIgnore"},
                        {"MIMEDoNotAccept", "MIMEReject"}, {"MIMEDoNotSave", "MIMERefuse"}, {"MIMEDoNotExamine", "MIMEIgnore"}};
        JCLO jclo = new JCLO(ARGS, aliases);

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

        if (ARGS.Version) {
            System.out.println(Version.getVersion());
            System.exit(0);
        }

        if (ARGS.Help) {
            System.out.println("Arguments:\n" + jclo.usage() + "URLs...");
            System.exit(0);
        }

        setLogLevel();
        setDefaults();

        if (ARGS.FirefoxCookies != null) { loadFirefoxCookies(); }
        if (ARGS.NetscapeCookies != null) { loadNetscapeCookies(); }
        if (ARGS.PlistCookies != null) { loadPlistCookies(); }

        if (ARGS.CheckpointEvery != 0) { readCheckpointFile(); }

        doit();
    }

}
