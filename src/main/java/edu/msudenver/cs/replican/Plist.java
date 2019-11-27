package edu.msudenver.cs.replican;

/*
** parse a Safari plist file
*/

import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class Plist extends DefaultHandler {
    private final Logger logger = LogManager.getLogger(getClass());
    private final Cookies cookies;

    private InputSource getInputSource(final String u) {
        URL url = null;
        try {
            url = new URL(u);
        } catch (MalformedURLException MUE) {
            logger.throwing(MUE);
        }

        PushbackInputStream pbis = null;

        try {
            pbis = new PushbackInputStream(url.openStream());

            int c;
            while ((c = pbis.read()) != '<') {
                logger.debug("Eating: " + c);
            }

            pbis.unread(c);
        } catch (IOException IOE) {
            logger.throwing(IOE);
        }

        return (new InputSource(pbis));
    }

     Plist(String u, Cookies cookies) {
        super();

        this.cookies = cookies;

        try {
            SAXParserFactory.newInstance().newSAXParser().parse(getInputSource(u), this);
        } catch (SAXParseException spe) {
            logger.warn("In " + u + ", at line " + spe.getLineNumber() +
                    ", column " + spe.getColumnNumber() + ", " + spe);
        } catch (ParserConfigurationException | SAXException | IOException pce) {
            logger.throwing(pce);
        }
     }

    private String domain;
    private String path;
    private String expires;
    private String name;
    private String value;
    private boolean d, p, m, n, v;

    private String current;

    public void startElement(@NonNull final String uri, @NonNull final String localName, @NonNull final String qName, @NonNull final Attributes attributes) {
        logger.trace("uri = " + uri);
        logger.trace("localName = " + localName);
        logger.trace("qName = " + qName);

        if (qName.equals("dict")) {
            current = domain = expires = name = path = "";
        }
    }

    public void endElement(@NonNull final String uri, @NonNull final String localName, @NonNull final String qName) {
        logger.trace("current = " + current);

        // the value will come later
        if (qName.equals("key")) {
            d = current.equals("Domain");
            m = current.equals("Expires");
            n = current.equals("Name");
            p = current.equals("Path");
            v = current.equals("Value");
            current = "";
            return;
        }

        if (qName.equals("dict")) {
            logger.trace("domain = " + domain);
            logger.trace("expires = " + expires);
            logger.trace("name = " + name);
            logger.trace("path = " + path);
            logger.trace("value = " + value);

            Date date = null;
            try {
                date = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(expires));
            } catch (ParseException PE) {
                logger.throwing(PE);
            }

            String exp = new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss zzz").format(date);

            cookies.addCookie(domain, path, name + "=" + value + "; Expires=" + exp);
            return;
        }

        if (d) {
            domain = current;
        } else if (m) {
            expires = current;
        } else if (n) {
            name = current;
        } else if (p) {
            path = current;
        } else if (v) {
            value = current;
        }

        current = "";
    }

    public void characters(char[] buf, int offset, int len) {
        current += new String(buf, offset, len);
    }

    public static void main(String[] args) {
        Cookies cookies = new Cookies();
        new Plist(args[0], cookies);
    }
}
