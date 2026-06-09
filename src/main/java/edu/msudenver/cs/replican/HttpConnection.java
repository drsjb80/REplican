package edu.msudenver.cs.replican;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

interface HttpConnection {
    // HTTP status codes
    int HTTP_OK = 200;
    int HTTP_MOVED_PERM = 301;
    int HTTP_MOVED_TEMP = 302;

    void connect() throws IOException;

    int getResponseCode() throws IOException;

    String getResponseMessage() throws IOException;

    String getHeaderField(String name);

    Map<String, java.util.List<String>> getHeaderFields();

    InputStream getInputStream() throws IOException;

    long getLastModified();

    void setRequestProperty(String key, String value);

    void disconnect();
}
