package edu.msudenver.replican;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.apache.logging.log4j.Logger;

class MyAuthenticator extends Authenticator
{
    private Logger logger = REplican.getLogger();
    private String username;
    private String password;

    public MyAuthenticator (String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    protected PasswordAuthentication getPasswordAuthentication()
    {
        char[] p = new char[password.length()];

        logger.info (username);
        logger.info (password);

        password.getChars (0, password.length(), p, 0);

        return (new PasswordAuthentication (username, p));
    }
}
