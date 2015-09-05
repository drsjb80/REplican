import java.net.Authenticator;
import java.net.PasswordAuthentication;

import edu.mscd.cs.javaln.*;

class MyAuthenticator extends Authenticator
{
    private JavaLN logger = (JavaLN) JavaLN.getLogger ("REplican");
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

        logger.config (username);
        logger.config (password);

        password.getChars (0, password.length(), p, 0);

        return (new PasswordAuthentication (username, p));
    }
}
