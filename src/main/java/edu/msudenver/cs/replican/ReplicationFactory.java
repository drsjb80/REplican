package edu.msudenver.cs.replican;

import lombok.NonNull;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ReplicationFactory {
    private final Logger logger = LogManager.getLogger(getClass());

    public Replicator createReplicator(@NonNull REplicanArgs args) {
        logger.traceEntry();

        ConfigProvider config = new REplicanConfigProvider(args);
        CookieManager cookies = new CookiesAdapter(new Cookies());
        URLQueue queue = new URLQueue();
        URLFetcher fetcher = new HttpURLFetcher();
        FileSaver saver = new WebFileSaver(args.Directory, args.IndexName);

        ReplicationContext context = new ReplicationContext(
            config,
            cookies,
            queue,
            fetcher,
            saver,
            LogManager.getLogger()
        );

        Replicator replicator = new Replicator(context);
        logger.traceExit(replicator);
        return replicator;
    }

    public void loadCookies(@NonNull REplicanArgs args, @NonNull CookieManager manager) {
        logger.traceEntry();

        CookieLoader netscapeLoader = new NetscapeCookieLoader();
        if (args.NetscapeCookies != null) {
            for (String file : args.NetscapeCookies) {
                try {
                    netscapeLoader.load(file, manager);
                } catch (Exception e) {
                    logger.error("Failed to load Netscape cookies from " + file, e);
                }
            }
        }

        CookieLoader plistLoader = new PlistCookieLoader();
        if (args.PlistCookies != null) {
            for (String file : args.PlistCookies) {
                try {
                    plistLoader.load(file, manager);
                } catch (Exception e) {
                    logger.error("Failed to load Plist cookies from " + file, e);
                }
            }
        }

        CookieLoader firefoxLoader = new FirefoxCookieLoader();
        if (args.FirefoxCookies != null) {
            for (String file : args.FirefoxCookies) {
                try {
                    firefoxLoader.load(file, manager);
                } catch (Exception e) {
                    logger.error("Failed to load Firefox cookies from " + file, e);
                }
            }
        }

        logger.traceExit();
    }
}
