BASE	= $*
TARGET	= $@
DEPENDS	= $<
NEWER	= $?

.SUFFIXES: .java .class

# gcj -source 1.2 -target 1.2 $(DEPENDS)

.java.class : 
	# javac -Xlint:unchecked $(DEPENDS)
	# javac -source 1.6 -target 1.6 $(DEPENDS)
	javac $(DEPENDS)

VERSION	= $(shell cat VERSION)
V	= $(shell cat VERSION | sed -e 's/\.//g')

HTTPFILE= edu/mscd/cs/httpfile
FTPURL	= edu/mscd/cs/ftp

SOURCES	= REplican.java DelimitedBufferedInputStream.java Cookie.java \
	  Cookies.java Utils.java YouAreElham.java WebFile.java \
	  MyAuthenticator.java REplicanArgs.java Plist.java \
	  $(HTTPFILE)/Handler.java $(HTTPFILE)/HandlerFactory.java \
	  $(HTTPFILE)/HttpfileURLConnection.java \
	  $(FTPURL)/Handler.java $(FTPURL)/HandlerFactory.java \
	  $(FTPURL)/FtpURLConnection.java \
	  Version.java

CLASSES = $(SOURCES:.java=.class) Cookie.class

MISC	= Makefile Manifest index index.html DOIT UPLOAD \
	  tests/*.good tests/*/*.good tests/Makefile \
	  tests/*.httpfile tests/*/*.httpfile tests/*/*.in \
	  VERSION bin/launch4j bin/Makefile bin/launch4l

FILES	= $(SOURCES) $(CLASSES) $(JARS) $(MISC) 

JAR	= REplican-$(VERSION).jar

ALL	= $(JAR) $(CLASSES) bin/REplican-$(VERSION) bin/REplican$(V).exe \
		bin/launch4j.xml

all : $(ALL)

clean :
	rm -f $(ALL)
	rm -rf trash

$(JAR) : $(CLASSES) 
	jar -cmf Manifest $(TARGET) $(CLASSES) edu/mscd/cs/javaln edu/mscd/cs/jclo

index.html : index Makefile VERSION
	sed -e "s/JAR/$(JAR)/" < index > $(TARGET)

checkstyle.out :
	checkstyle -c ~/src/beaty_checks.xml $(SOURCES) > checkstyle.out

docs : FRC
	javadoc -d docs edu.mscd.cs.httpfile

FRC :

Version.java : VERSION
	echo 'public class Version { public static String getVersion() { return ("$(VERSION)"); } public static void main (String args[]) { System.out.println (getVersion()); }}' > $(TARGET)

bin/REplican-$(VERSION) : $(JAR)
	cd bin; make `basename $(TARGET)`

bin/REplican$(V).exe : $(JAR)
	cd bin; make `basename $(TARGET)`
