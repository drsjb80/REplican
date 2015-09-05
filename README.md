# REplican
REplican is a powerful web replicator based on regular expressions.

Regular expressions allow either great specificity or generality, giving
you a lot of power to retrieve whatever you wish from the web.

Usage: java REplican-*VERSION*.jar [args...] URL [URL...]

N.B.: the URL(s) are automatically added to PathAccept.

## Arguments
There are a lot of arguments to let you tune exactly what is examined and
saved

### Simple arguments
Argument|Explanation
--------|-----------
--PathAccept=*RE*   | a *RE* of URL paths to accept.
--PathReject=*RE*   | a *RE* of URL paths to reject.
--PathExamine=*RE*  | a *RE* of URL paths to examine for other links. These are automatically added to PathAccept.
--PathIgnore=*RE*   | a *RE* of URL paths to ignore for other links.
--PathSave=*RE* | a *RE* of URL paths to save. These are automatically added to PathAccept.
--PathRefuse=*RE*   | a *RE* of URL paths not to save.
--MIMEAccept=*RE*   | a *RE* of MIME types to accept.
--MIMEReject=*RE*   | a *RE* of MIME types to reject.
--MIMEExamine=*RE*  | a *RE* of MIME types to examine for other links.
--MIMEIgnore=*RE*   | a *RE* of MIME types to ignore for other links.
--MIMESave=*RE*     | a *RE* of MIME types to save.
--MIMERefuse=*RE*   | a *RE* of MIME types not to save.
--LogLevel=LEVEL    | Possible values for LEVEL are: SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL, OFF. Default: INFO.
--Username=NAME | A username to provide if requested by server.
--Password=PASSWORD | A password to provide if requested by server.
--Overwrite[=*TRUE*/*FALSE*]    | Whether or not to overwrite files if they already exist locally. Default: FALSE
--SetLastModified[=*TRUE*/*FALSE*]  | Set the date on the file created to the date on the associated URL.
--IfModifiedSince[=*TRUE*/*FALSE*]  | Only overwrite an existing file if the associated URL is newer. Assumes all files were created using the --SetLastModified flag and the --Overwrite flag is used. Essentially, if you want to keep date information, always use all three flags (--Overwrite, --SetLastModified, and --IfModifiedSince) when replicating.
--PrintAccept[=*TRUE*/*FALSE*] --PrintReject[=*TRUE*/*FALSE*] --PrintExamine[=*TRUE*/*FALSE*] --PrintIgnore[=*TRUE*/*FALSE*] --PrintSave[=*TRUE*/*FALSE*] --PrintRefuse[=*TRUE*/*FALSE*] --PrintAdd[=*TRUE*/*FALSE*] | Whether to display the results of specific operations. By default, only --PrintSave is active.
--Help  | Print the command line options and exit.
--Version   | Print the version and exit.
--IfNewerThan=file  | Save if remote is newer than file
--Directory=*String*  | The directory to replicate to, instead of the current working directory.
--UserAgent=*String*  | Set the User-Agent identifier. Default: Java/VERSION
--LoadCookies=*String...* | Load cookies from file(s).
--SaveCookies=*String*    | Save cookies to file.
--IgnoreCookies[=*TRUE*/*FALSE*]    | If you want to ignore all cookies. Default: FALSE
--IndexName=*String*  | The file name to save paths ending with a '/'. Default: index.html
--FollowRedirects[=*TRUE*/*FALSE*]  | Follow redirections. Default: TRUE.
--SaveProgress[=*TRUE*/*FALSE*] | Show a progress bar for saves. Default: FALSE.
--StopOn=HTTP return code...    | The return codes REplican should stop on.  Examples include 403, 404, etc.
--CheckpointEvery=# | Write a checkpoint file every # changes (additions, downloads, etc.). REplican checks for the file on startup and uses it to initialize its state. This is very useful if one has a lot of files to process and the machine crashes, the network dies, etc.; REplican does not have to start over examining the entire web site(s).
--CheckpointFile=*String* | The name of the checkpoint file (default: REplican.cp).
--PauseBetween=#    | Pause for # milliseconds between each request.
--PauseAfterSave=#  | Pause for # milliseconds after each file saved.
--Interesting=*RE*...   | Any number of regular expressions that match patterns inside <...> pairs for consideration by REplican. The capturing group(s) contain the URLs to be considered. If you specify any, they override all the defaults, which are: `[hH][rR][eE][fF]\s*=\s*[\"']?([^\"'#>]*)` and `[sS][rR][cC]\s*=\s*[\"']?([^\"'#>]*)`

### Paired arguments
Several argurments must always come in pairs as both values are used. The first is a regular expression to match and the second is a replacement string.

```
--FilenameRewrite=*RE*
--FilenameRewrite=*String*
```
For example, if you want to remove anything after a
'.wmv', you can do:
```
--FilenameRewrite="\\.wmv.*"
--FilenameRewrite=".wmv"
```
If you want to combine all found in a content distribution network into one directory:
```
--FilenameRewrite="[1234].cdn.site.com"
--FilenameRewrite="cdn.site.com"
```
If you want to remove certain characters from saved files:
```
--FilenameRewrite="%20"
--FilenameRewrite=" "
--FilenameRewrite="\\&"
--FilenameRewrite="AMP"
--FilenameRewrite="\\="
--FilenameRewrite="EQ"
--FilenameRewrite="\\?"
--FilenameRewrite="QUES"
```
```
--URLRewrite=*RE*
--URLRewrite=*String*
```
Rewrite the URLs as with the
FilenameRewrite above. Useful if a site dynamically rewrites or randomizes
URLs
```
--URLFixUp=*RE*
--URLFixUp=*String*
```
Fix up URLs before matching to reduce the complexity of regular expressions to match URLs. The defaults are:
```
--URLFixUp="[\s]+"
--URLFixUp=" "
```
which removes white space and condense multiple spaces into a single space.

## Method
First, a URL is checked to see whether it should be accepted or rejected.
The initial URL(s) from the command line are accepted by default.

You control acceptance via the --PathAccept, --PathReject, --MIMEAccept,
and --MINEReject command line regular expression arguments.  The Path
arguments take regular expressions that match URLs; MIME that match MIME
types. Accept's are matched before Reject's, so the Reject's
can be a subset of what is Accept'ed.

Once a URL is accepted, it is added to the list to be examined or saved.

The --PathExamine, --PathIgnore, --MIMEExamine, and --MIMEIgnore arguments
control whether a given document is searched for links to other documents and
objects to be saved.

The --PathSave, --PathRefuse, --MIMESave, and --MIMERefuse regular
expressions decide on whether to save a URL to the local machine.
Therefore, you can examine certain types of files, and save other types.

If you specify nothing, --MIMEExamine is set to "text/.*" and
--PathSave is set to ".*"; i.e.: look at all MIME text files and save
everything.

If you want to examine or save a URL, you must accept it and not reject it.

With the usage of regular expressions to drive REplican, there is no
"recursive" command line argument needed -- if a link is found that matches
the criteria, it is examined and/or saved.

## Regular Expressions
REplican uses the full expressiveness of Java's regular expressions, an
introduction to which can be found at:
http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html.

Typically, you want to use '\.' in the matching regular expressions for
site and extension specifiers as the dot ('.') is used in regular
expressions to mean 'any character'. '.*' is used to specify 'zero or more
of any character'. The exception to the '\.' rule are the initial URL's
(listed last on the command line) as they do not require escaping the dots
with a backslash. 

## Examples
Save a single file that is the one URL specified:
```
$ java -jar ~/src/REplican/REplican-VERSION.jar http://coloradmin.com/steve/resume/
--PathSave=[http://coloradmin\.com/steve/resume/]
Saving path: http://coloradmin.com/steve/resume/
Saving to coloradmin.com/steve/resume/index.html
```

Save all the PDFs from a site, but nothing else, printing lots of status:
```
java -jar ~/src/REplican/REplican-VERSION.jar \
--PathAccept='http://coloradmin\.com/steve/.*' \
--PathSave='.*\.pdf' \
--PathExamine='.*\.[hH][tT][mM][lL]*' \
--PathExamine='http://coloradmin.com/steve/' \
--PrintAll \
http://coloradmin.com/steve/
```

## SOCKS
Built into Java (not just REplican), is the ability to have all network
traffic be proxied through a SOCKS proxy. There are two command line
arguments to java to make this work:
```
java -DsocksProxyHost=hostname -DsocksProxyPort=portnumber -jar REplican-VERSION.jar [...]
```
Important: as with all other options to java, these must appear before the
class or jar file on the command line or they will be taken as arguments to
the program. Also, if Java cannot make a connection via SOCKS it will try
without using SOCKS, which might not be what you want.

## Detailed Steps
For each URL

1. Check against PathAccept and PathReject.
  * If accepted and not rejected, add to list of URL's to consider.
  * When fetched, open URL and check against MIMEAccept and MIMEIgnore. 
  * If no PathAccept, PathReject, MIMEAccept, and MIMEReject are
    given, PathAccept is set to only the initial URL(s) specified on the
    command line so that REplican doesn't attempt to fetch the entire web.
2. If PathExamine or MIMEExamine match and PathIgnore or MIMEIgnore don't,
fetch the URL and look for other URL's to fetch as specified by the
Interesting *RE*.  Those found are put on a list for later retrieval. 
  * If nothing about examining or ignoring is specified on the
    command line, MIMEExamine is set to "text/.*", which is a good general
    value as one typically does not want to examine images, videos,
    applications, etc.
3. PathSave, MIMESave, PathRefuse, and MIMERefuse are consulted. 
  * If nothing is specified for these on the command line, everything
    accepted is saved. 
