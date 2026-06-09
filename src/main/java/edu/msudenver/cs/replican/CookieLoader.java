package edu.msudenver.cs.replican;

import java.io.IOException;

public interface CookieLoader {
    void load(String filePath, CookieManager manager) throws IOException;
}
