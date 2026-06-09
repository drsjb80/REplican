package edu.msudenver.cs.replican;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

class Plist {
    private final Logger logger = LogManager.getLogger(getClass());
    private final Cookies cookies;

    Plist(String fileUrl, Cookies cookies) throws IOException {
        this.cookies = cookies;
        loadCookies(fileUrl);
    }

    private void loadCookies(String fileUrl) throws IOException {
        URL url;
        try {
            url = new URL(fileUrl);
        } catch (MalformedURLException e) {
            logger.error("Invalid URL: " + fileUrl, e);
            return;
        }

        try {
            NSObject plist = PropertyListParser.parse(url.openStream());
            if (!(plist instanceof NSDictionary)) {
                logger.warn("Root object is not a dictionary");
                return;
            }

            NSDictionary root = (NSDictionary) plist;
            NSObject cookiesObj = root.objectForKey("Cookies");

            if (cookiesObj == null || !(cookiesObj instanceof NSArray)) {
                logger.warn("No Cookies array found in plist");
                return;
            }

            NSArray cookiesArray = (NSArray) cookiesObj;
            for (NSObject obj : cookiesArray.getArray()) {
                if (obj instanceof NSDictionary) {
                    processCookieDict((NSDictionary) obj);
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing plist from " + fileUrl, e);
        }
    }

    private void processCookieDict(NSDictionary dict) {
        String domain = getString(dict, "Domain");
        String path = getString(dict, "Path");
        String name = getString(dict, "Name");
        String value = getString(dict, "Value");
        Date expires = getDate(dict, "Expires");

        if (domain != null && path != null && name != null && value != null) {
            String cookieString = name + "=" + value;

            if (expires != null) {
                String expiresStr = new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss zzz").format(expires);
                cookieString += "; Expires=" + expiresStr;
            }

            cookies.addCookie(domain, path, cookieString);
        }
    }

    private String getString(NSDictionary dict, String key) {
        NSObject obj = dict.objectForKey(key);
        if (obj != null) {
            return obj.toJavaObject().toString();
        }
        return null;
    }

    private Date getDate(NSDictionary dict, String key) {
        NSObject obj = dict.objectForKey(key);
        if (obj != null) {
            Object javaObj = obj.toJavaObject();
            if (javaObj instanceof Date) {
                return (Date) javaObj;
            }
        }
        return null;
    }
}
