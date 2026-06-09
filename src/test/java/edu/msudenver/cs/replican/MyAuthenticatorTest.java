package edu.msudenver.cs.replican;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.PasswordAuthentication;

import static org.junit.jupiter.api.Assertions.*;

public class MyAuthenticatorTest {
    private MyAuthenticator auth;
    private static final String TEST_USER = "testuser";
    private static final String TEST_PASS = "testpass123";

    @BeforeEach
    void setUp() {
        auth = new MyAuthenticator(TEST_USER, TEST_PASS);
    }

    @Test
    void authenticatorCanBeCreated() {
        assertNotNull(auth);
    }

    @Test
    void getPasswordAuthenticationReturnsValidObject() {
        PasswordAuthentication pa = auth.getPasswordAuthentication();
        assertNotNull(pa);
    }

    @Test
    void getPasswordAuthenticationReturnsCorrectUsername() {
        PasswordAuthentication pa = auth.getPasswordAuthentication();
        assertEquals(TEST_USER, pa.getUserName());
    }

    @Test
    void getPasswordAuthenticationReturnsPasswordAsCharArray() {
        PasswordAuthentication pa = auth.getPasswordAuthentication();
        char[] pwd = pa.getPassword();
        assertNotNull(pwd);
        assertEquals(TEST_PASS.length(), pwd.length);
    }

    @Test
    void getPasswordAuthenticationWithCorrectPassword() {
        PasswordAuthentication pa = auth.getPasswordAuthentication();
        String recoveredPassword = new String(pa.getPassword());
        assertEquals(TEST_PASS, recoveredPassword);
    }

    @Test
    void getPasswordAuthenticationWithEmptyPassword() {
        MyAuthenticator emptyAuth = new MyAuthenticator(TEST_USER, "");
        PasswordAuthentication pa = emptyAuth.getPasswordAuthentication();
        assertEquals(0, pa.getPassword().length);
    }

    @Test
    void getPasswordAuthenticationWithSpecialCharacters() {
        String specialPass = "p@$$w0rd!#%&";
        MyAuthenticator specialAuth = new MyAuthenticator(TEST_USER, specialPass);
        PasswordAuthentication pa = specialAuth.getPasswordAuthentication();
        assertEquals(specialPass, new String(pa.getPassword()));
    }

    @Test
    void getPasswordAuthenticationWithNullUsername() {
        MyAuthenticator nullUserAuth = new MyAuthenticator(null, TEST_PASS);
        PasswordAuthentication pa = nullUserAuth.getPasswordAuthentication();
        assertNull(pa.getUserName());
    }

    @Test
    void getPasswordAuthenticationMultipleCallsReturnDifferentCharArrays() {
        PasswordAuthentication pa1 = auth.getPasswordAuthentication();
        PasswordAuthentication pa2 = auth.getPasswordAuthentication();
        assertNotSame(pa1.getPassword(), pa2.getPassword());
    }
}
