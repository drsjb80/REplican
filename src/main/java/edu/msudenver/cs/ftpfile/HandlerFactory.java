package edu.msudenver.cs.ftpfile;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

class HandlerFactory implements URLStreamHandlerFactory {
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.toLowerCase().equals("ftp"))
            return (new Handler());

        return (null);
    }
}
