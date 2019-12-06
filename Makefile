default:
	@mvn package

clean:
	@mvn clean

deb-pkg:
	dpkg-buildpackage

deb-files:
	@cp target/DDNSD.jar debian/tmp/opt/ddnsd-java/
	dpkg-deb --build debian/tmp ../ddnsd-java_0.1_all.deb
