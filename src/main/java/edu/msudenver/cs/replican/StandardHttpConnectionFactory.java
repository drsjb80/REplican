package edu.msudenver.cs.replican;

import java.io.IOException;
import java.net.URL;

class StandardHttpConnectionFactory implements HttpConnectionFactory {
    @Override
    public HttpConnection createConnection(String urlString) throws IOException {
        return new HttpURLConnection(new URL(urlString).openConnection());
    }
}
