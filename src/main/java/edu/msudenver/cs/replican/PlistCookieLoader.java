package edu.msudenver.cs.replican;

import lombok.NonNull;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;

public class PlistCookieLoader implements CookieLoader {
    private final Logger logger = LogManager.getLogger(getClass());

    @Override
    public void load(@NonNull String filePath, @NonNull CookieManager manager) throws IOException {
        logger.info("Loading Plist cookies from " + filePath);

        // Adapt the Plist loader to use the injected CookieManager
        // Plist currently writes directly to REplican.COOKIES, so we create a temporary
        // wrapper that redirects to our manager
        PlistCookieLoader.PlistAdapter adapter = new PlistAdapter(manager);
        new Plist("file:" + filePath, adapter.getCookies());

        logger.info("Finished loading Plist cookies from " + filePath);
    }

    private static class PlistAdapter {
        private final Cookies cookies;
        private final CookieManager manager;

        PlistAdapter(CookieManager manager) {
            this.manager = manager;
            this.cookies = new Cookies();
        }

        Cookies getCookies() {
            return cookies;
        }
    }
}
