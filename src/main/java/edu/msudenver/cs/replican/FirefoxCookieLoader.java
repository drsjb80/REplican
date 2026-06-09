package edu.msudenver.cs.replican;

import lombok.NonNull;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;

public class FirefoxCookieLoader implements CookieLoader {
    private final Logger logger = LogManager.getLogger(getClass());

    @Override
    public void load(@NonNull String filePath, @NonNull CookieManager manager) throws IOException {
        logger.info("Loading Firefox cookies from " + filePath);

        if (manager instanceof CookiesAdapter) {
            Cookies cookies = ((CookiesAdapter) manager).getCookies();
            FirefoxCookies.loadCookies(filePath, cookies);
        } else {
            logger.warn("Firefox cookie loader requires CookiesAdapter, got " + manager.getClass().getName());
        }

        logger.info("Finished loading Firefox cookies from " + filePath);
    }
}
