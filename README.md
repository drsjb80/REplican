# REplican
REplican is a powerful web replicator based on regular expressions.

REplican is a powerful web replicator based on regular expressions. Regular
expressions allow either great specificity or generality, giving you a lot
of power to retrieve whatever you wish from the web.

Usage: java REplican-1.5.1.jar [args...] URL [URL...]

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
--Directory=String  | The directory to replicate to, instead of the current working directory.
--UserAgent=String  | Set the User-Agent identifier. Default: Java/version
--LoadCookies=String... | Load cookies from file(s).
--SaveCookies=String    | Save cookies to file.
--IgnoreCookies[=*TRUE*/*FALSE*]    | If you want to ignore all cookies. Default: FALSE
--IndexName=String  | The file name to save paths ending with a '/'. Default: index.html
--FollowRedirects[=*TRUE*/*FALSE*]  | Follow redirections. Default: TRUE.
--SaveProgress[=*TRUE*/*FALSE*] | Show a progress bar for saves. Default: FALSE.
--StopOn=HTTP return code...    | The return codes REplican should stop on.  Examples include 403, 404, etc.
--CheckpointEvery=# | Write a checkpoint file every # changes (additions, downloads, etc.). REplican checks for the file on startup and uses it to initialize its state. This is very useful if one has a lot of files to process and the machine crashes, the network dies, etc.; REplican does not have to start over examining the entire web site(s).
--CheckpointFile=String | The name of the checkpoint file (default: REplican.cp).
--PauseBetween=#    | Pause for # milliseconds between each request.
--PauseAfterSave=#  | Pause for # milliseconds after each file saved.
--Interesting=*RE*gular expression...   | Any number of regular expressions that match patterns inside <...> pairs for consideration by REplican. The capturing group(s) contain the URLs to be considered. If you specify any, they override all the defaults, which are: `[hH][rR][eE][fF]\s*=\s*[\"']?([^\"'#>]*)` and `[sS][rR][cC]\s*=\s*[\"']?([^\"'#>]*)`


### Paired arguments
Several argurments must always come in pairs as both values are used. The first is a regular expression to match and the second is a replacement string.

*
```
--FilenameRewrite=String
--FilenameRewrite=String
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
*
```
--URLRewrite=String
--URLRewrite=String
```
Rewrite the URLs as with the
FilenameRewrite above. Useful if a site dynamically rewrites or randomizes
URLs
*
--URLFixUp=String --URLFixUp=String
Fix up URLs before matching to reduce the complexity of regular expressions to match URLs. The defaults are:
```
--URLFixUp="[\s]+"
--URLFixUp=" "
```
which removes white space and condense multiple spaces into a single space.
