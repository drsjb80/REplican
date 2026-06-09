package edu.msudenver.cs.replican;

import lombok.NonNull;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.sql.*;

public class FirefoxCookieLoader implements CookieLoader {
    private final Logger logger = LogManager.getLogger(getClass());

    @Override
    public void load(@NonNull String filePath, @NonNull CookieManager manager) throws IOException {
        logger.info("Loading Firefox cookies from " + filePath);

        if (manager instanceof CookiesAdapter) {
            Cookies cookies = ((CookiesAdapter) manager).getCookies();
            loadCookies(filePath, cookies);
        } else {
            logger.warn("Firefox cookie loader requires CookiesAdapter, got " + manager.getClass().getName());
        }

        logger.info("Finished loading Firefox cookies from " + filePath);
    }

    private void loadCookies(final String file, @NonNull Cookies cookies) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + file);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return;
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM moz_cookies")) {

            while (rs.next()) {
                final String host = rs.getString("host");
                final String path = rs.getString("path");
                final String domain = rs.getString("baseDomain");
                final String name = rs.getString("name");
                final String value = rs.getString("value");
                final int expiry = rs.getInt("expiry");
                final boolean isSecure = rs.getInt("isSecure") != 0;

                final Cookie cookie = new Cookie(host, domain, path, expiry, isSecure, name, value);
                cookies.addCookie(cookie);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
