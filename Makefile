BASE	= $*
TARGET	= $@
DEPENDS	= $<
NEWER	= $?

.SUFFIXES: .java .class

# gcj -source 1.2 -target 1.2 $(DEPENDS)

.java.class : 
	javac $(DEPENDS)

VERSION	= $(shell cat VERSION)
V	= $(shell cat VERSION | sed -e 's/\.//g')

HTTPFILE= edu/mscd/cs/httpfile
FTPURL	= edu/mscd/cs/ftp

SOURCES	= $(shell ls *.java) $(shell ls $(HTTPFILE)/*.java) \
	  $(shell ls $(FTPURL)/*.java)

CLASSES = $(SOURCES:.java=.class)

JAR	= REplican-$(VERSION).jar

ALL	= $(JAR) $(CLASSES) bin/REplican-$(VERSION) bin/REplican$(V).exe \
		bin/launch4j.xml

all : $(ALL)

clean :
	rm -f $(ALL)

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
	bin/launch4l $(JAR)
	mv REplican-$(VERSION) $(TARGET)
	chmod 755 $(TARGET)

bin/launch4j.xml : bin/launch4j VERSION
	sed -e "s/VERSION/$(V)/" -e "s,JAR,$(JAR)," < $(DEPENDS) > $(TARGET)

bin/REplican$(V).exe : $(JAR) bin/launch4j.xml
	~/src/launch4j/launch4j bin/launch4j.xml
