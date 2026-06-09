package edu.msudenver.cs.replican;

import edu.msudenver.cs.jclo.JCLO;
import lombok.NonNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.net.Authenticator;

class REplican {
    static final Logger LOGGER = LogManager.getLogger();
    static final REplicanArgs ARGS = new REplicanArgs();

    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
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


    public static void main(String[] arguments) {
        String[][] aliases = {
            {"PathDoNotAccept", "PathReject"},
            {"PathDoNotSave", "PathRefuse"},
            {"PathDoNotExamine", "PathIgnore"},
            {"MIMEDoNotAccept", "MIMEReject"},
            {"MIMEDoNotSave", "MIMERefuse"},
            {"MIMEDoNotExamine", "MIMEIgnore"}
        };
        JCLO jclo = new JCLO(ARGS, aliases);

        if (arguments.length == 0) {
            System.out.println("Arguments:\n" + jclo.usage() + "URLs...");
            System.exit(1);
        }

        try {
            jclo.parse(arguments);
        } catch (IllegalArgumentException e) {
            System.err.println(e);
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
        setupAuthenticator();
        replicate();
    }

    private static void setupAuthenticator() {
        String username = ARGS.Username;
        String password = ARGS.Password;
        if (username != null || password != null) {
            Authenticator.setDefault(new MyAuthenticator(username, password));
        }
    }

    private static void replicate() {
        if (ARGS.additional == null) {
            LOGGER.error("No URLs specified");
            System.exit(1);
        }

        ReplicationFactory factory = new ReplicationFactory();
        Replicator replicator = factory.createReplicator(ARGS);

        CookieManager cookies = new CookiesAdapter(new Cookies());
        factory.loadCookies(ARGS, cookies);

        try {
            for (String url : ARGS.additional) {
                replicator.addURL(url);
            }

            replicator.fetchAll();

            LOGGER.info("Replication complete: " + replicator.getFetchedCount() + " URLs fetched");
        } catch (Exception e) {
            LOGGER.error("Replication failed", e);
            System.exit(1);
        }
    }

}
