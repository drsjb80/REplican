package edu.msudenver.cs.ftpfile;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.Socket;

import java.util.logging.Logger;

// http://www.faqs.org/rfcs/rfc959.html
// http://java.sun.com/developer/onlineTraining/protocolhandlers/

/*
 * $ telnet clem.mscd.edu 21
 * Trying 147.153.1.3...
 * Connected to clem.mscd.edu.
 * Escape character is '^]'.
 * 220 clem.mscd.edu FTP server (Compaq Tru64 UNIX Version 5.60) ready.
 * USER anonymous
 * 331 Guest login ok, send ident as password.
 * PASS beatys@mscd.edu
 * 230 Guest login ok, access restrictions apply.
 * PASV
 * 227 Entering Passive Mode (147,153,1,3,6,192)
 * HELP
 * 214-The following commands are recognized (* =>'s unimplemented).
 *    USER    PORT    STOR    MSAM*   RNTO    NLST    MKD     CDUP    LPSV 
 *    PASS    PASV    APPE    MRSQ*   ABOR    SITE    XMKD    XCUP    EPRT 
 *    ACCT    TYPE    MLFL*   MRCP*   DELE    SYST    RMD     STOU    EPSV 
 *    SMNT*   STRU    MAIL*   ALLO    CWD     STAT    XRMD    SIZE 
 *    REIN*   MODE    MSND*   REST    XCWD    HELP    PWD     MDTM 
 *    QUIT    RETR    MSOM*   RNFR    LIST    NOOP    XPWD    LPRT 
 * 214 End of help
 */
public class FtpURLConnection extends URLConnection {
    private static final Logger logger = Logger.getLogger("global");
    private InputStream cis;    // command input stream
    private OutputStream cos;
    private InputStream dis;    // data input stream
    private OutputStream dos;
    private final URL url;
    private String user;
    private String pass;
    private Socket commandSocket;
    private Socket dataSocket;
    private int responseCode;
    private String responseMessage;

    /*
     * getAuthority = anonymous:beatys%40mscd.edu@clem.mscd.edu
     * getDefaultPort = 21
     * getPort = -1
     * getQuery = null
     * getFile = /file
     * getHost = clem.mscd.edu
     * getPath = /file
     * getUserInfo = anonymous:beaty%40mscd.edu
     * decode = anonymous:beaty@mscd.edu
     * hostname = emess.mscd.edu
     */
    public FtpURLConnection(URL url) {
        super(url);

        logger.setLevel(java.util.logging.Level.FINEST);
        java.util.logging.ConsoleHandler ch =
                new java.util.logging.ConsoleHandler();
        ch.setLevel(java.util.logging.Level.FINEST);
        logger.addHandler(ch);

        logger.finest(url.toString());
        this.url = url;

        // set the base class variables
        allowUserInteraction = false;
        connected = false;
        doInput = true;
        doOutput = false;
        allowUserInteraction = false;

        setUserPass(url);
    }

    public InputStream getInputStream() {
        return (dis);
    }

    public int getResponseCode() {
        return (responseCode);
    }

    public String getResponseMessage() {
        return (responseMessage);
    }

    private void setUserPass(URL url) {
        String localUser = System.getProperty("user.name");
        if (localUser == null)
            localUser = "noone";

        String localHost;
        try {
            localHost = java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhe) {
            logger.finer(uhe.toString());
            localHost = "localhost.localdomain";
        }

        String userInfo = url.getUserInfo();
        if (userInfo == null)    // nothing specified
        {
            user = "anonymous";
            pass = localUser + "@" + localHost;
        } else {
            if (userInfo.indexOf(':') == -1)    // only user specified
            {
                user = userInfo;
                pass = localUser + "@" + localHost;
            } else {
                String up[] = userInfo.split(":");

                if (up.length == 2)        // user:password or :password
                {
                    user = up[0].equals("") ? localUser : up[0];
                    pass = up[1];
                } else if (up.length == 1)    // user:
                {
                    user = up[0];
                    pass = localUser + "@" + localHost;
                }
            }
        }
    }

    private String sendCommand(String command, char expect) throws IOException {
        logger.finest(command);
        logger.finest("" + expect);

        if (command != null) {
            cos.write(URLDecoder.decode(command, "UTF-8").getBytes());
            cos.flush();
        }

        boolean done = false;
        String line = "";

        while (!done) {
            int c;
            line = "";

            while ((c = cis.read()) != -1) {
                // System.out.print ((char) c);
                if (c == '\r')
                    continue;
                if (c == '\n')
                    break;
                line += (char) c;
            }

            logger.finest("response = " + line);

            done = line.length() == 3 || line.charAt(3) != '-';
        }

        String a[] = line.split(" ", 2);
        responseCode = Integer.parseInt(a[0]);
        if (a.length > 1)
            responseMessage = a[1];

        if (line.charAt(0) != expect) {
            disconnect();
            throw new IOException("FTP error: " + line);
        }

        logger.finest(line);
        return (line);
    }

    private void connectToDataSocket() throws
            IOException {
        String p = sendCommand("PASV\r\n", '2');
        logger.finest(p);

        // 227 Entering Passive Mode (147,153,1,3,7,113)
        p = p.replaceFirst(".*\\(", "");
        p = p.replaceFirst("\\).*", "");
        String q[] = p.split(",");

        String ipAddress = q[0] + "." + q[1] + "." + q[2] + "." + q[3];
        InetAddress ia = InetAddress.getByName(ipAddress);

        int port = Integer.parseInt(q[4]) * 256 + Integer.parseInt(q[5]);

        logger.finest(ia.toString());
        logger.finest(new Integer(port).toString());

        dataSocket = new Socket(ia, port);
        dis = dataSocket.getInputStream();
        dos = dataSocket.getOutputStream();

        logger.finest(dataSocket.toString());
        logger.finest(dis.toString());
        logger.finest(dos.toString());
        connected = true;
    }

    public void connect() throws IOException {
        int port = url.getPort() != -1 ? url.getPort() : 21;
        commandSocket = new Socket(url.getHost(), port);
        cis = commandSocket.getInputStream();
        cos = commandSocket.getOutputStream();

        sendCommand(null, '2');

        String toSend[] = {
                "USER " + user + "\r\n",
                "PASS " + pass + "\r\n",
                "TYPE I\r\n",
        };

        char expect[] = {'3', '2', '2'};

        // String r = "";

        for (int i = 0; i < toSend.length; i++) {
            sendCommand(toSend[i], expect[i]);
        }

        connectToDataSocket();

        sendCommand("RETR " + url.getPath() + "\r\n", '1');
    }

    private void disconnect() throws IOException {
        try {
            sendCommand("QUIT\r\n", '2');

            if (cis != null) cis.close();
            if (cos != null) cos.close();
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (commandSocket != null) commandSocket.close();
            if (dataSocket != null) dataSocket.close();
        } finally {
            connected = false;
            cis = null;
            cos = null;
            dis = null;
            dos = null;
            commandSocket = null;
            dataSocket = null;
        }
    }

    public static void main(String args[]) throws IOException {
        logger.setLevel(java.util.logging.Level.FINEST);
        java.util.logging.ConsoleHandler ch =
                new java.util.logging.ConsoleHandler();
        ch.setLevel(java.util.logging.Level.FINEST);
        logger.addHandler(ch);

        for (String arg : args) {
            FtpURLConnection fuc = new FtpURLConnection(new URL(arg));

            fuc.connect();

            BufferedInputStream bis =
                    new BufferedInputStream(fuc.getInputStream());

            int c;
            while ((c = bis.read()) != -1) {
                System.out.print((char) c);
            }

            fuc.disconnect();
        }
    }
}
