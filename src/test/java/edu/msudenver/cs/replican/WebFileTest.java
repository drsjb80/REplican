package edu.msudenver.cs.replican;

import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;

import static org.junit.Assert.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebFileTest {
    private YouAreEll yrl = new YouAreEll("http://localhost:3000");
    private WebFile webFile = new WebFile(yrl);

    @Test
    public void createFile() {
    }

    @Test
    public void getFilePath() throws Throwable {
        assertEquals ("localhost", P38.call("getFilePath", webFile, new Object[]{"http://localhost:3000"}));
        assertEquals ("localhost", P38.call("getFilePath", webFile, new Object[]{"http://localhost"}));
        assertEquals ("localhost/foo", P38.call("getFilePath", webFile, new Object[]{"http://localhost/foo"}));
        assertEquals ("localhost/" + REplican.args.IndexName, P38.call("getFilePath", webFile, new Object[]{"http://localhost/"}));
        REplican.args.Directory = "/tmp";
        assertEquals ("/tmp/localhost", P38.call("getFilePath", webFile, new Object[]{"http://localhost"}));
    }

    @Test(expected = MalformedURLException.class)
    public void getFilePathBadURL() throws Throwable {
        assertEquals ("localhost", P38.call("getFilePath", webFile, new Object[]{"bad://localhost:3000"}));
    }

    @Test
    public void openFile() throws Throwable {
        File file = (File) P38.call("openFile", new WebFile(new YouAreEll("file:pom.xml")));
        assertEquals("pom.xml", file.toString());
        file = (File) P38.call("openFile", new WebFile(new YouAreEll("file:~pom.xml")));
        assertEquals(System.getProperty("user.home") + "/pom.xml", file.toString());
    }

    /*
            Socket socket = mock(Socket.class);
            when(socket.getInputStream()).thenReturn(inputstream);
        when(socket.getOutputStream()).thenReturn(outputstream);

     */
}