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
    static REplicanArgs ARGS;

    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }



    private static void setLogLevel(REplicanArgsMutable args) {
        Level level = Level.OFF;

        if (args.LogLevel == null) {
            level = Level.WARN;
        } else {
            switch (args.LogLevel) {
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

    private static void setDefaults(REplicanArgsMutable args) {
        if (args.Interesting == null) {
            final String urlref = "\\s*=\\s*[\"']?([^\"'>]*)";
            final String href = "[hH][rR][eE][fF]";
            final String src = "[sS][rR][cC]";

            args.Interesting = new String[]{href + urlref, src + urlref};
        }

        if (args.URLFixUp == null) {
            args.URLFixUp = new String[]{"\\s+", " ", "\\\\", "", "\'", "%27"};
        }

        // if they don't specify anything, look at only text.
        if (args.MIMEExamine == null
                && args.MIMEIgnore == null
                && args.PathExamine == null
                && args.PathIgnore == null) {
            args.MIMEExamine = new String[]{"text/.*"};
            if (args.PrintExamine) {
                LOGGER.warn("--MIMEExamine=" + java.util.Arrays.toString(args.MIMEExamine));
            }
        }

        // if they don't specify anything, save only what is specified on
        // the command line.
        if (args.MIMESave == null
                && args.MIMERefuse == null
                && args.PathSave == null
                && args.PathRefuse == null) {
            if (args.additional == null) {
                LOGGER.error("No URLs specified");
                System.exit(1);
            }

            args.PathSave = new String[args.additional.length];

            for (int i = 0; i < args.additional.length; i++) {
                args.PathSave[i] = escapeURL(args.additional[i]);
            }

            if (args.PrintSave) {
                LOGGER.warn("--PathSave=" + java.util.Arrays.toString(args.PathSave));
            }
        }

        if (args.PrintAll) {
            args.PrintAccept = args.PrintReject =
                    args.PrintSave = args.PrintRefuse =
                            args.PrintExamine = args.PrintIgnore =
                                    args.PrintRedirects = true;
        }

        /*
         ** make sure we accept everything we examine, save, and the initial
         ** URLs
         */
        args.PathAccept = Utils.combineArrays(args.PathAccept, args.PathExamine);
        args.PathAccept = Utils.combineArrays(args.PathAccept, args.PathSave);
        args.PathAccept = Utils.combineArrays(args.PathAccept, args.additional);
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

        REplicanArgsMutable argsMutable = new REplicanArgsMutable();
        JCLO jclo = new JCLO(argsMutable, aliases);

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

        if (argsMutable.Version) {
            System.out.println(Version.getVersion());
            System.exit(0);
        }

        if (argsMutable.Help) {
            System.out.println("Arguments:\n" + jclo.usage() + "URLs...");
            System.exit(0);
        }

        setLogLevel(argsMutable);
        setDefaults(argsMutable);
        ARGS = REplicanArgs.fromMutable(argsMutable);

        setupAuthenticator();
        replicate();
    }

    private static void setupAuthenticator() {
        String username = ARGS.username();
        String password = ARGS.password();
        if (username != null || password != null) {
            Authenticator.setDefault(new MyAuthenticator(username, password));
        }
    }

    private static void replicate() {
        if (ARGS.additional() == null) {
            LOGGER.error("No URLs specified");
            System.exit(1);
        }

        ReplicationFactory factory = new ReplicationFactory();
        Replicator replicator = factory.createReplicator(ARGS);

        CookieManager cookies = new CookiesAdapter(new Cookies());
        factory.loadCookies(ARGS, cookies);

        try {
            for (String url : ARGS.additional()) {
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
