package edu.mscd.cs.ftp;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class HandlerFactory implements URLStreamHandlerFactory
{
    public URLStreamHandler createURLStreamHandler (String protocol)
    {
	if (protocol.toLowerCase().equals ("ftp"))
	    return (new Handler());

	return (null);
    }
}
