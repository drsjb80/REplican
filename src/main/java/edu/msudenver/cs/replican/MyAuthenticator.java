package edu.msudenver.cs.replican;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.apache.logging.log4j.Logger;

class MyAuthenticator extends Authenticator
{
    private final Logger logger = REplican.logger;
    private final String username;
    private final String password;

    // THREADSAFE_LEVEL_GREY
    public MyAuthenticator (String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    // THREADSAFE_LEVEL_GREY
    protected PasswordAuthentication getPasswordAuthentication()
    {
        char[] p = new char[password.length()];

        logger.info (username);
        logger.info (password);

        password.getChars (0, password.length(), p, 0);

        return (new PasswordAuthentication (username, p));
    }
}
