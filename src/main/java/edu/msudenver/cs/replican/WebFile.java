package edu.msudenver.cs.replican;

import java.net.URL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;

import java.net.MalformedURLException;
import org.apache.logging.log4j.Logger;

public class WebFile
{
    private final REplicanArgs args;
    private final YouAreEll yrl;
    private final Logger logger = REplican.getLogger();
    private File file;
    private BufferedOutputStream bos;

    File getFile() { return (file); }
    BufferedOutputStream getBOS() { return (bos); }

    WebFile (YouAreEll yrl, REplicanArgs args)
    {
        this.yrl = yrl;
        this.args = args;
        createFile ();
    }

    private boolean dealWithExistingFile (long LastModified)
    {
        logger.traceEntry (String.valueOf(LastModified));

        if (! args.Overwrite)
        {
            logger.warn ("Not overwriting: " + file);
            return (false);
        }

        if (args.IfModifiedSince)
        {
            if (LastModified > 0)
            {
                logger.trace ("file: " + file.lastModified());
                logger.trace ("URL: " + LastModified);

                if (file.lastModified() <= LastModified)
                {
                    if (args.PrintSkip)
                        logger.info ("Not modified: " + file);
                    return (false);
                }
            }
            else
            {
                logger.info ("No last-modified information: " + file);
            }
        }

        return (true);
    }

    private String getFilePath (String s)
    {
        logger.traceEntry (s);

        URL url = null;

        try
        {
            url = new URL (s);
        }
        catch (MalformedURLException MUE)
        {
            logger.throwing (MUE);
            return (null);
        }

        String hostname = url.getHost();
        String filename = url.getFile();

        if (filename.endsWith ("/"))
            filename += args.IndexName;

        if (hostname == null)
            hostname = "localhost";

        logger.debug (hostname);
        logger.debug (filename);

        String path = hostname + filename;

        String dir = args.Directory;
        if (dir != null)
        {
            String separator = System.getProperty ("file.separator");
            if (! dir.endsWith (separator))
                dir += separator;

            path = dir + path;
        }

        logger.traceExit (path);
        return (path);
    }

    private String getDirectoryPath()
    {
        String path = getFilePath (yrl.getURL());

        path = Utils.replaceAll (path, args.FilenameRewrite);
        path = path.replaceFirst ("^~", System.getProperty("user.home"));

        if (args.PrintSavePath)
            logger.info ("Saving to: " + path);

        file = new File (path);

        String directoryPath = null;
        if (path.indexOf ('/') == -1)
        {
            directoryPath = "./";
        }
        else
        {
            directoryPath = path.replaceAll ("/[^/]*$", "");
        }
        return (directoryPath);
    }

    // return true if we don't need to reread
    private boolean checkIfNewerThan()
    {
        if (args.IfNewerThan != null)
        {
            File newerThan = new File (args.IfNewerThan);
            logger.debug ("file time = " + newerThan.lastModified());
            logger.debug ("url time = " + yrl.getLastModified());
            if (yrl.getLastModified() < newerThan.lastModified())
            {
                if (args.PrintSkip)
                    logger.info ("Skipping becauser older than " +
                        args.IfNewerThan);
                return (true);
            }
        }

        return (false);
    }

    // return true if we don't need to reread
    private boolean checkExistingFile()
    {
        if (args.OverwriteIfLarger || args.OverwriteIfSmaller)
        {
            boolean larger = yrl.getContentLength() > file.length();
            boolean smaller = yrl.getContentLength() < file.length();

            if ((args.OverwriteIfLarger && larger) ||
                (args.OverwriteIfSmaller && smaller))
            {
                logger.info ("Overwriting because " + 
                    yrl.getContentLength() + " is " +
                    (args.OverwriteIfLarger ? "larger" : "smaller") +
                    " than " + file.length());
                return (false);
            }
            else
            {
                logger.info ("Not overwriting because " + 
                    yrl.getContentLength() + " is not " +
                    (args.OverwriteIfLarger ? "larger" : "smaller") +
                    " than " + file.length());
                return (true);
            }
        }
        else if (! dealWithExistingFile (yrl.getLastModified()))
        {
            return (true);
        }

        return (false);
    }

    private void createFile ()
    {
        File directory = new File (getDirectoryPath());

        logger.debug ("file: '" + file + "'");
        logger.debug ("directory: " + directory);

        if (! directory.exists())
        {
            logger.debug ("Attempting to make: " + directory);

            if (! directory.mkdirs ())
            {
                logger.warn ("Couldn't create directory: " + directory);
                return;
            }
        }
        else
        {
            logger.debug ("Directory: " + directory + " already exists");
        }

        if (checkIfNewerThan()) return;

        if (file.exists())
        {
            logger.debug ("File exists");
            if (checkExistingFile()) return;
        }

        try
        {
            bos = new BufferedOutputStream (new FileOutputStream (file));
        }
        catch (Exception e)
        {
            logger.throwing (e);
            return;
        }

        logger.debug ("Opened: " + file);
    }
}
