package edu.msudenver.cs.ftpfile;

import java.net.URL;
import java.net.URLConnection;

public class Handler extends java.net.URLStreamHandler
{
    protected URLConnection openConnection (URL url)
    {
        return (new FtpURLConnection (url));
    }
}
