package edu.msudenver.cs.httpfile;

import java.net.URL;
import java.net.URLConnection;

class Handler extends java.net.URLStreamHandler
{
    protected URLConnection openConnection (URL url)
    {
        return (new HttpfileURLConnection (url));
    }
}
