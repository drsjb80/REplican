package edu.msudenver.cs.replican;

import org.apache.logging.log4j.Logger;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by beatys on 9/16/17.
 */

/*
CREATE TABLE "moz_cookies" (id INTEGER PRIMARY KEY,
    baseDomain TEXT,
    originAttributes TEXT NOT NULL DEFAULT '',
    name TEXT,
    value TEXT,
    host TEXT,
    path TEXT,
    expiry INTEGER,
    lastAccessed INTEGER,
    creationTime INTEGER,
    isSecure INTEGER,
    isHttpOnly INTEGER,
    inBrowserElement INTEGER DEFAULT 0,
    CONSTRAINT moz_uniqueid UNIQUE (name, host, path, originAttributes));
*/

public class SQLite {
    private Logger logger = REplican.getLogger();

    public void readDB(String url) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return;
        }

        String sql = "SELECT * FROM moz_cookies";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Cookie cookie = new Cookie();

                String host = rs.getString("host");
                String path = rs.getString("path");
                String domain = rs.getString("baseDomain");
                String name = rs.getString("name");
                String value = rs.getString("value");
                int expiry = rs.getInt("expiry");
                // int lastAccessed = rs.getInt("lastAccessed");
                // int creationTime = rs.getInt("creationTime");
                int isSecure = rs.getInt("isSecure");
                // int isHttpOnly = rs.getInt("isHttpOnly");

                String cookieString = "";
                if (domain != null) cookieString += "Domain=" + domain + ";";
                if (name != null && value != null) cookieString += name + "=" + value + ";";
                if (expiry != 0) cookieString += "Max-Age=" + expiry+ ";";
                if (isSecure == 1) cookieString += "Secure;";
                cookie.addToValues(host, path, cookieString);
                System.out.println (host + " " + path + " " + cookieString);
                System.out.println(cookie);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String args[]) {
        new SQLite().readDB("jdbc:sqlite:/Users/beatys/Library/Application Support/Firefox/Profiles/rnwwcxjq.default/cookies.sqlite");
    }
}
