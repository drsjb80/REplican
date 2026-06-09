package edu.msudenver.cs.httpfile;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class HandlerFactory implements URLStreamHandlerFactory
{
    //THREADSAFE_LEVEL_GREY
    public URLStreamHandler createURLStreamHandler (String protocol)
    {
	if (protocol.toLowerCase().equals ("httpfile"))
	    return (new Handler());

	return (null);
    }
}
