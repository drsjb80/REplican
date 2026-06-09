package edu.msudenver.cs.replican;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class NetscapeCookieLoaderTest {
    private CookieLoader loader;
    private CookieManager manager;

    @BeforeEach
    void setUp() {
        loader = new NetscapeCookieLoader();
        manager = new CookiesAdapter(new Cookies());
    }

    @Test
    void loaderImplementsCookieLoader() {
        assertTrue(loader instanceof CookieLoader);
    }

    @Test
    void loadingEmptyFileDoesNotThrow(@TempDir File tempDir) throws IOException {
        File emptyFile = new File(tempDir, "cookies.txt");
        emptyFile.createNewFile();

        assertDoesNotThrow(() -> loader.load(emptyFile.getAbsolutePath(), manager));
    }

    @Test
    void loadingCommentOnlyFileDoesNotThrow(@TempDir File tempDir) throws IOException {
        File commentFile = new File(tempDir, "cookies.txt");
        try (FileWriter fw = new FileWriter(commentFile)) {
            fw.write("# This is a comment\n");
            fw.write("# Another comment\n");
        }

        assertDoesNotThrow(() -> loader.load(commentFile.getAbsolutePath(), manager));
    }

    @Test
    void loadingValidNetscapeCookieIncreaasesCookieCount(@TempDir File tempDir) throws IOException {
        File cookieFile = new File(tempDir, "cookies.txt");
        try (FileWriter fw = new FileWriter(cookieFile)) {
            fw.write(".example.com\tTRUE\t/\tFALSE\t1234567890\tSID\t31d4d96e407aad42\n");
        }

        int countBefore = manager.getCookieCount();
        loader.load(cookieFile.getAbsolutePath(), manager);
        int countAfter = manager.getCookieCount();

        assertTrue(countAfter > countBefore);
    }

    @Test
    void loadingMalformedLineSkips(@TempDir File tempDir) throws IOException {
        File cookieFile = new File(tempDir, "cookies.txt");
        try (FileWriter fw = new FileWriter(cookieFile)) {
            fw.write("invalid\tline\twith\tno\tenough\tfields\n");
            fw.write(".example.com\tTRUE\t/\tFALSE\t1234567890\tSID\t31d4d96e407aad42\n");
        }

        assertDoesNotThrow(() -> loader.load(cookieFile.getAbsolutePath(), manager));
        assertEquals(1, manager.getCookieCount());
    }
}
