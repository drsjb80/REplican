package edu.msudenver.cs.replican;

import lombok.NonNull;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class NetscapeCookieLoader implements CookieLoader {
    private final Logger logger = LogManager.getLogger(getClass());

    @Override
    public void load(@NonNull String filePath, @NonNull CookieManager manager) throws IOException {
        logger.info("Loading Netscape cookies from " + filePath);

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\t");
                if (parts.length < 7) {
                    logger.warn("Skipping malformed cookie line: " + line);
                    continue;
                }

                String domain = parts[0];
                String path = parts[2];
                String name = parts[5];
                String value = parts[6];

                manager.addCookie(domain, path, name + "=" + value);
            }
        }

        logger.info("Finished loading Netscape cookies from " + filePath);
    }
}
