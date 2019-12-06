default:
	@mvn package

clean:
	@rm -rf debian/tmp
	@mvn clean

deb-pkg:
	dpkg-buildpackage

deb-files:
	@java -cp target/DDNSD.jar de.linux4.ddnsd.debian.Debian
	@chmod +x debian/tmp/DEBIAN/postinst
	@chmod +x debian/tmp/usr/bin/ddnsd-java
	@mkdir -p debian/tmp/opt/ddnsd-java/
	@cp target/DDNSD.jar debian/tmp/opt/ddnsd-java/
	dpkg-deb --build debian/tmp ../ddnsd-java_0.1_all.deb
