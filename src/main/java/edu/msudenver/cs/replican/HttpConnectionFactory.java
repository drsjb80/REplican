package edu.msudenver.cs.replican;

import java.io.IOException;
import java.net.URL;

interface HttpConnectionFactory {
    HttpConnection createConnection(String urlString) throws IOException;
}
