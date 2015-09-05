BASE	= $*
TARGET	= $@
DEPENDS	= $<
NEWER	= $?

HOST=192.168.27.7

VERSION	= $(shell cat ../VERSION)
OS	= `uname`

# STAT=stat --format "%s"
STAT=stat -f "%B"

HTTPFILE = ../edu/mscd/cs/httpfile

JAR	= ../REplican-$(VERSION).jar

test tests : testbasic testoverwrite testsetlastmodified testifmodified \
	testexamine testmime testpath testcookie testexpired testbaddomain \
	testgooddomain testloadcookies testloadandsavecookies testsavecookies \
	teststopon testcookies testmodified

testbasic : $(JAR)
	rm -rf $(HOST)
	java -jar $(JAR) --PathAccept=http://$(HOST)/~beaty/Dossier/vitae.html \
        http://$(HOST)/~beaty/Dossier/vitae.html > /tmp/$(TARGET).good 2>&1
	diff $(TARGET).good /tmp/$(TARGET).good
	rm /tmp/$(TARGET).good
	test -f $(HOST)/~beaty/Dossier/vitae.html
	rm -rf $(HOST)

testoverwrite : $(JAR)
	mkdir -p $(HOST)/~beaty/Dossier
	touch $(HOST)/~beaty/Dossier/vitae.html
	java -jar $(JAR) --Overwrite=TRUE \
        --PathAccept=http://$(HOST)/~beaty/Dossier/vitae.html \
	    http://$(HOST)/~beaty/Dossier/vitae.html > /tmp/$(TARGET).good 2>&1
	diff $(TARGET).good /tmp/$(TARGET).good
	rm /tmp/$(TARGET).good
	test -s $(HOST)/~beaty/Dossier/vitae.html

#	--LogLevel=FINE \

testsetlastmodified : $(JAR)
	java -jar $(JAR) \
	    --Overwrite \
	    --SetLastModified \
	    --Directory=trash \
	    httpfile:Headers/LastModified.httpfile
	$(STAT) trash/Headers/LastModified.httpfile | diff - \
		Headers/LastModified.good \

testifmodified : $(JAR)
	java -jar $(JAR) \
	    --Overwrite \
	    --IfModifiedSince \
	    --SetLastModified \
        --PrintReject \
        --PathAccept=http://$(HOST)/~beaty/Dossier/vitae.html \
	    http://$(HOST)/~beaty/Dossier/vitae.html \
		> /tmp/$(TARGET).good 2>&1
	diff $(TARGET).good /tmp/$(TARGET).good
	rm /tmp/$(TARGET).good

testexamine : $(JAR)
	rm -f $(HOST)/~beaty/Dossier/vitae.html
	java -jar $(JAR) \
	    --PathIgnore=".*html" \
	    http://$(HOST)/~beaty/Dossier/vitae.html \
		> /tmp/$(TARGET).good 2>&1
	diff $(TARGET).good /tmp/$(TARGET).good
	rm /tmp/$(TARGET).good

# --LogLevel=FINEST \

testmime : $(JAR)
	java -jar $(JAR) \
	    --MIMEAccept='text/html' \
	    --MIMEIgnore='.*' --MIMERefuse='.*' \
	    http://$(HOST)/~beaty/Dossier/vitae.html \
		> /tmp/$(TARGET).good 2>&1
	diff $(TARGET).good /tmp/$(TARGET).good
	rm /tmp/$(TARGET).good

testargs : $(JAR)
	java -jar $(JAR) -DsocksProxyHost=localhost

testpath : $(JAR)
	rm -rf $(HOST)
	java -jar $(JAR) \
	--PathAccept='http://emess\.mscd\.edu/~beaty/Dossier/.*\.html' \
	--PathAccept='http://emess\.mscd\.edu/~beaty/Dossier/.*\.pdf' \
	--PathSave='.*\.pdf' \
	--PathExamine='.*\.[hH][tT][mM][lL]*' \
	http://$(HOST)/~beaty/Dossier/vitae.html 2>&1 \
		| sort > /tmp/$(TARGET).good 
	diff $(TARGET).good /tmp/$(TARGET).good
	rm /tmp/$(TARGET).good

testcookie : $(JAR)
	java -jar $(JAR) \
	--SaveCookies=Cookies/CookieGood.out \
	--Directory=trash \
	httpfile:Cookies/CookieGood.httpfile
	diff Cookies/CookieGood.out Cookies/CookieGood.good

testexpired : $(JAR)
	java -jar $(JAR) \
	--SaveCookies=Cookies/CookieExpired.out \
	--Directory=trash \
	httpfile:Cookies/CookieExpired.httpfile
	diff Cookies/CookieExpired.out Cookies/CookieExpired.good

testbaddomain : $(JAR)
	java -jar $(JAR) \
	--Directory=trash \
	httpfile:Cookies/BadDomain.httpfile \
		> /tmp/$(TARGET).good 2>&1
	diff $(TARGET).good /tmp/$(TARGET).good
	rm /tmp/$(TARGET).good

testgooddomain : $(JAR)
	rm -f trash/Cookies/GoodDomain.httpfile
	java -jar $(JAR) \
	--Directory=trash \
	httpfile:Cookies/GoodDomain.httpfile \
		> /tmp/$(TARGET).good 2>&1
	diff $(TARGET).good /tmp/$(TARGET).good
	rm /tmp/$(TARGET).good

testbadpath : $(JAR)
	java -jar $(JAR) \
	--Directory=trash \
	httpfile:Cookies/BadPath.httpfile \
		> /tmp/$(TARGET).good 2>&1
	diff $(TARGET).good /tmp/$(TARGET).good
	rm /tmp/$(TARGET).good

testgoodpath : $(JAR)
	java -jar $(JAR) \
	--Directory=trash \
	httpfile:Cookies/GoodPath.httpfile \
		> /tmp/$(TARGET).good 2>&1
	diff $(TARGET).good /tmp/$(TARGET).good
	rm /tmp/$(TARGET).good

testloadcookies : $(JAR)
	java -jar $(JAR) \
	--Overwrite \
	--LoadCookies=Cookies/LoadCookies.in \
	--Directory=trash \
	http://$(HOST)/~beaty/cgi-bin/testcookies.cgi
	diff trash/$(HOST)/~beaty/cgi-bin/testcookies.cgi \
	    Cookies/LoadCookies.good

testloadandsavecookies : $(JAR)
	java -jar $(JAR) \
	--LoadCookies=Cookies/LoadAndSaveCookies.in \
	--SaveCookies=Cookies/LoadAndSaveCookies.out \
	--PathRefuse='.*' \
	httpfile:empty.httpfile
	diff Cookies/LoadAndSaveCookies.in \
	    Cookies/LoadAndSaveCookies.out

testsavecookies : $(JAR)
	java -jar $(JAR) \
	--PathAccept="http://$(HOST)/~beaty/cgi-bin/.*.cgi" \
	--Overwrite \
	--SaveCookies=Cookies/SaveCookies.out \
	--Directory=trash \
	http://$(HOST)/~beaty/cgi-bin/setcookies.cgi
	diff Cookies/SaveCookies.out Cookies/SaveCookies.good
	diff trash/$(HOST)/~beaty/cgi-bin/testcookies.cgi \
	    Cookies/LoadCookies.good

teststopon : $(JAR)
	java -jar $(JAR) --StopOn=200 http://www.mscd.edu/ \
		> /tmp/$(TARGET).good 2>&1
	diff $(TARGET).good /tmp/$(TARGET).good
	rm /tmp/$(TARGET).good

testcookies : $(JAR)
	java -jar $(JAR) \
	--Overwrite \
	--PathAccept='http://$(HOST)/~beaty/cgi-bin/.*' \
	http://$(HOST)/~beaty/cgi-bin/setcookies.cgi
	diff $(HOST)/~beaty/cgi-bin/testcookies.cgi $(TARGET).good

FILE=httpfile:empty.httpfile 

testmodified : $(JAR)
	rm -f tmp/empty.httpfile
	java -jar $(JAR) --Directory=tmp $(FILE) 2> /tmp/$(TARGET).good
	ls -l tmp/empty.httpfile
	java -jar $(JAR) --Directory=tmp $(FILE) 2>> /tmp/$(TARGET).good
	ls -l tmp/empty.httpfile
	java -jar $(JAR) --SetLastModified --Directory=tmp $(FILE) \
		2>> /tmp/$(TARGET).good
	ls -l tmp/empty.httpfile
	java -jar $(JAR) --Overwrite --SetLastModified --Directory=tmp $(FILE) \
		2>> /tmp/$(TARGET).good
	ls -l tmp/empty.httpfile
	java -jar $(JAR) --IfModifiedSince --Overwrite --SetLastModified \
		--Directory=tmp $(FILE) 2>> /tmp/$(TARGET).good
	ls -l tmp/empty.httpfile
	diff $(TARGET).good /tmp/$(TARGET).good
	rm /tmp/$(TARGET).good

ftp : $(JAR)
	java -jar $(JAR) \
	    ftp://anonymous@clem.mscd.edu/outgoing/csi1300/Web/index.htm \
		> /tmp/$(TARGET).good 2>&1
	diff $(TARGET).good /tmp/$(TARGET).good
	rm /tmp/$(TARGET).good
	rm -rf clem.mscd.edu

ftpbadpasswd : $(JAR)
	java -jar $(JAR) \
	    ftp://clem.mscd.edu/outgoing/csi1300/Web/index.htm \
		> /tmp/$(TARGET).good 2>&1
	diff $(TARGET).good /tmp/$(TARGET).good
	rm /tmp/$(TARGET).good

ftpbadpath : $(JAR)
	java -jar $(JAR) \
	    ftp://anonymous@clem.mscd.edu/outgoing/csi1300/Web/index.html \
		> /tmp/$(TARGET).good 2>&1
	diff $(TARGET).good /tmp/$(TARGET).good
	rm /tmp/$(TARGET).good

https : $(JAR)
	java -jar $(JAR) https://sourceforge.net \
		> /tmp/$(TARGET).good 2>&1
	diff $(TARGET).good /tmp/$(TARGET).good
	rm /tmp/$(TARGET).good
	rm -rf sourceforge.net
