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

        // FirefoxCookies.loadCookies() currently writes to REplican.COOKIES
        // For now, we delegate to the existing implementation
        // In Phase 3, we can refactor FirefoxCookies to accept a CookieManager
        FirefoxCookies.loadCookies(filePath);

        logger.info("Finished loading Firefox cookies from " + filePath);
    }
}
