package edu.msudenver.cs.replican;

import lombok.NonNull;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Replicator {
    private final ReplicationContext context;
    private final Logger logger = LogManager.getLogger(getClass());

    public Replicator(@NonNull ReplicationContext context) {
        this.context = context;
    }

    public void addURL(@NonNull String url) {
        logger.traceEntry(url);

        final boolean accept = Utils.blurf(
            context.getConfig().getPathAccept(),
            context.getConfig().getPathReject(),
            url,
            true
        );

        if (context.getConfig().isPrintAccept() && accept) {
            logger.info("Accepting path: " + url);
        }

        if (context.getConfig().isPrintReject() && !accept) {
            logger.info("Rejecting path: " + url);
        }

        if (accept) {
            String rewrittenURL = url;
            if (context.getConfig().getURLRewrite() != null) {
                rewrittenURL = Utils.replaceAll(url, context.getConfig().getURLRewrite());
            }

            if (context.getQueue().addIfNew(rewrittenURL)) {
                if (context.getConfig().isPrintAdd()) {
                    logger.info("Adding: " + rewrittenURL);
                }
            }

            int checkpointEvery = context.getConfig().getCheckpointEvery();
            if (checkpointEvery != 0 && context.getQueue().size() % checkpointEvery == 0) {
                logger.debug("Checkpoint triggered at " + context.getQueue().size() + " URLs");
            }
        }

        logger.traceExit();
    }

    public void addURLsFromHTML(@NonNull String baseURL, @NonNull List<String> htmlLines) {
        logger.traceEntry(baseURL);
        logger.traceEntry(htmlLines.toString());

        for (String line : htmlLines) {
            String fixedLine = line;
            if (context.getConfig().getURLFixUp() != null) {
                fixedLine = Utils.replaceAll(line, context.getConfig().getURLFixUp());
            }
            logger.trace(fixedLine);

            String newBase = Utils.newBase(fixedLine);
            if (newBase != null) {
                logger.debug("Setting base to " + newBase);
            }

            List<String> interesting = context.getQueue().getUnfetchedURLs(); // placeholder
            if (context.getConfig().getInteresting() != null) {
                interesting = Utils.interesting(context.getConfig().getInteresting(), fixedLine);
            }

            for (String possible : interesting) {
                logger.trace(possible);
                if (possible != null) {
                    try {
                        URL u;
                        if (newBase != null) {
                            u = new URL(new URL(newBase), possible);
                        } else if (baseURL != null) {
                            u = new URL(new URL(baseURL), possible);
                        } else {
                            u = new URL(possible);
                        }
                        addURL(u.toString());
                    } catch (MalformedURLException mue) {
                        logger.error("Invalid URL: " + mue.getMessage());
                    }
                }
            }
        }

        logger.traceExit();
    }

    public void fetchAll() throws IOException {
        logger.traceEntry();

        boolean done = false;

        while (!done) {
            done = true;
            List<String> unfetched = context.getQueue().getUnfetchedURLs();

            for (String url : unfetched) {
                done = false;
                try {
                    fetch(url);
                } catch (IOException e) {
                    logger.error("Failed to fetch " + url + ": " + e.getMessage());
                }
                context.getQueue().markFetched(url);

                if (context.getConfig().getPauseBetween() != 0) {
                    Utils.snooze(context.getConfig().getPauseBetween());
                }
            }
        }

        logger.traceExit();
    }

    private void fetch(@NonNull String url) throws IOException {
        logger.traceEntry(url);

        try {
            URLFetcher.FetchResult result = context.getFetcher().fetch(url);

            boolean shouldExamine = Utils.blurf(
                context.getConfig().getPathExamine(),
                context.getConfig().getPathIgnore(),
                url,
                false
            ) && Utils.blurf(
                context.getConfig().getMIMEExamine(),
                context.getConfig().getMIMEIgnore(),
                result.getContentType(),
                false
            );

            boolean shouldSave = Utils.blurf(
                context.getConfig().getPathSave(),
                context.getConfig().getPathRefuse(),
                url,
                false
            ) && Utils.blurf(
                context.getConfig().getMIMESave(),
                context.getConfig().getMIMERefuse(),
                result.getContentType(),
                false
            );

            if (shouldExamine || shouldSave) {
                List<String> htmlLines = readLines(result.getInputStream());
                if (shouldExamine) {
                    addURLsFromHTML(url, htmlLines);
                }
                if (shouldSave) {
                    logger.info("Saving: " + url);
                }
            }

            if (context.getConfig().getPauseAfterSave() != 0) {
                Utils.snooze(context.getConfig().getPauseAfterSave());
            }
        } catch (IOException e) {
            logger.throwing(e);
            throw e;
        }

        logger.traceExit();
    }

    private List<String> readLines(java.io.InputStream is) throws IOException {
        List<String> lines = new ArrayList<>();
        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    public int getQueueSize() {
        return context.getQueue().size();
    }

    public int getFetchedCount() {
        return context.getQueue().getFetchedCount();
    }

    public int getUnfetchedCount() {
        return context.getQueue().getUnfetchedCount();
    }
}
