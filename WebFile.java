import java.net.URL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;

import java.net.MalformedURLException;

import edu.mscd.cs.jclo.JCLO;
import edu.mscd.cs.javaln.JavaLN;

public class WebFile
{
    private REplicanArgs args;
    private YouAreEll yrl;
    private JavaLN logger = (JavaLN) JavaLN.getLogger ("REplican");
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
        logger.entering (LastModified);

        if (! args.Overwrite)
        {
            logger.warning ("Not overwriting: " + file);
            return (false);
        }

        if (args.IfModifiedSince)
        {
            if (LastModified > 0)
            {
                logger.finest ("file: " + file.lastModified());
                logger.finest ("URL: " + LastModified);

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
        logger.entering (s);

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

        logger.fine (hostname);
        logger.fine (filename);

        String path = hostname + filename;

        String dir = args.Directory;
        if (dir != null)
        {
            String separator = System.getProperty ("file.separator");
            if (! dir.endsWith (separator))
                dir += separator;

            path = dir + path;
        }

        logger.exiting (path);
        return (path);
    }

    private String getDirectoryPath()
    {
        String path = getFilePath (yrl.getURL());

        path = REplican.replaceAll (path, args.FilenameRewrite);
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
            logger.fine ("file time = " + newerThan.lastModified());
            logger.fine ("url time = " + yrl.getLastModified());
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

        logger.fine ("file: '" + file + "'");
        logger.fine ("directory: " + directory);

        if (! directory.exists())
        {
            logger.fine ("Attempting to make: " + directory);

            if (! directory.mkdirs ())
            {
                logger.warning ("Couldn't create directory: " + directory);
                return;
            }
        }
        else
        {
            logger.fine ("Directory: " + directory + " already exists");
        }

        if (checkIfNewerThan()) return;

        if (file.exists())
        {
            logger.fine ("File exists");
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

        logger.fine ("Opened: " + file);
    }
}
