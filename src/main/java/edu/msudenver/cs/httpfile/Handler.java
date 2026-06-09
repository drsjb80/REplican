package edu.msudenver.cs.httpfile;

import java.net.URL;
import java.net.URLConnection;

class Handler extends java.net.URLStreamHandler
{
    //THREADSAFE_LEVEL_GREY
    protected URLConnection openConnection (URL url)
    {
        return (new HttpfileURLConnection (url));
    }
}
