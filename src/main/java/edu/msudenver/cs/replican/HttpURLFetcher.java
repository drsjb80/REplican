package edu.msudenver.cs.replican;

import lombok.NonNull;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;

public class HttpURLFetcher implements URLFetcher {
    private final Logger logger = LogManager.getLogger(getClass());

    @Override
    public FetchResult fetch(@NonNull String url) throws IOException {
        logger.traceEntry(url);

        YouAreEll yrl = new YouAreEll(url);
        InputStream is = yrl.getInputStream();

        FetchResult result = new FetchResult() {
            @Override
            public InputStream getInputStream() {
                return is;
            }

            @Override
            public String getContentType() {
                return yrl.getContentType();
            }

            @Override
            public int getContentLength() {
                return yrl.getContentLength();
            }

            @Override
            public long getLastModified() {
                return yrl.getLastModified();
            }
        };

        logger.traceExit(result);
        return result;
    }
}
