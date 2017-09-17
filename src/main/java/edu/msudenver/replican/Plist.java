package edu.msudenver.replican;

/*
** parse a Safari plist file
*/

import org.apache.logging.log4j.Logger;
import java.net.URL;
import java.net.MalformedURLException;

import java.io.PushbackInputStream;
import java.io.IOException;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

class Plist extends DefaultHandler
{
    private static Logger logger = REplican.getLogger();
    private Cookies cookies;

    static InputSource getInputSource (String u)
    {
        URL url = null;
        try
        {
            url = new URL (u);
        }
        catch (MalformedURLException MUE)
        {
            logger.throwing (MUE);
        }

        PushbackInputStream pbis = null;

        try
        {
            pbis = new PushbackInputStream (url.openStream());

            int c;
            while ((c = pbis.read() ) != '<')
            {
                logger.debug ("Eating: " + c);
            }

            pbis.unread (c);
        }
        catch (IOException IOE)
        {
            logger.throwing (IOE);
        }

        return (new InputSource (pbis) );
    }

    public Plist (String u, Cookies cookies)
    {
        super();

        this.cookies = cookies;

        try
        {
            SAXParserFactory.newInstance().newSAXParser().
                parse (getInputSource (u), this);
        }
        catch (SAXParseException spe)
        {
            logger.warn ("In " + u + ", at line " + spe.getLineNumber() +
                ", column " + spe.getColumnNumber() + ", " + spe);
        }
        catch (ParserConfigurationException pce)
        {
            logger.throwing (pce);
        }
        catch (SAXException se)
        {
            logger.throwing (se);
        }
        catch (IOException IOE)
        {
            logger.throwing (IOE);
        }
    }

    private String domain;
    private String path;
    private String expires;
    private String name;
    private String value;
    private boolean d, p, m, n, v;

    private String current;

    public void startElement (String uri, String localName, String qName,
        Attributes attributes) throws SAXException
    {
        logger.trace ("uri = " + uri);
        logger.trace ("localName = " + localName);
        logger.trace ("qName = " + qName);

        if (qName.equals ("dict"))
        {
                current = domain = expires = name = path = "";
        }
    }

    public void endElement (String uri, String localName, String qName)
        throws SAXException
    {
        logger.trace ("current = " + current);

        if (qName.equals ("key"))
        {
            d = current.equals ("Domain");
            m = current.equals ("Expires");
            n = current.equals ("Name");
            p = current.equals ("Path");
            v = current.equals ("Value");
        }
        else if (qName.equals ("dict"))
        {
            logger.trace ("domain = " + domain);
            logger.trace ("expires = " + expires);
            logger.trace ("name = " + name);
            logger.trace ("path = " + path);
            logger.trace ("value = " + value);

            Date date = null;
            try
            {
                date = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").
                    parse (expires));
            }
            catch (ParseException PE)
            {
                logger.throwing (PE);
            }

            String exp =
                new SimpleDateFormat ("EEE, dd-MMM-yyyy hh:mm:ss zzz").
                    format (date);

            cookies.addCookie (domain, path, name + "=" + value +
                "; Expires=" + exp);
        }
        else if (d)
            domain = current;
        else if (m)
            expires = current;
        else if (n)
            name = current;
        else if (p)
            path = current;
        else if (v)
            value = current;

        current = "";
    }

    public void characters (char buf[], int offset, int len) throws SAXException
    {
        current += new String (buf, offset, len);
    }

    public static void main (String args[])
    {
        Cookies cookies = new Cookies();
        new Plist (args[0], cookies);
    }
}
