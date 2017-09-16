package edu.mscd.cs.httpfile;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class HandlerFactory implements URLStreamHandlerFactory
{
    public URLStreamHandler createURLStreamHandler (String protocol)
    {
	if (protocol.toLowerCase().equals ("httpfile"))
	    return (new Handler());

	return (null);
    }
}
