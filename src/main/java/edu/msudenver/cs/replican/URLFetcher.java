package edu.msudenver.cs.replican;

import java.io.IOException;
import java.io.InputStream;

public interface URLFetcher {
    FetchResult fetch(String url) throws IOException;

    interface FetchResult {
        InputStream getInputStream();

        String getContentType();

        int getContentLength();

        long getLastModified();
    }
}
