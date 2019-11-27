package edu.msudenver.cs.replican;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class FirefoxCookies {
    private final Logger logger = LogManager.getLogger(getClass());

    static void loadCookies(final String file) {
        // /Users/beatys/Library/Application Support/Firefox/Profiles/rnwwcxjq.default/cookies.sqlite

        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + file);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return;
        }

        try {
            final String sql = "SELECT * FROM moz_cookies";
            final Statement stmt = conn.createStatement();
            final ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                final String host = rs.getString("host");
                final String path = rs.getString("path");
                final String domain = rs.getString("baseDomain");
                final String name = rs.getString("name");
                final String value = rs.getString("value");
                final int expiry = rs.getInt("expiry");
                final boolean isSecure = rs.getInt("isSecure") != 0;

                final Cookie cookie = new Cookie(host, domain, path, expiry, isSecure, name, value);
                REplican.COOKIES.addCookie(cookie);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
