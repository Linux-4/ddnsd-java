package de.linux4.ddnsd.debian;

import java.io.File;
import java.io.IOException;

import de.linux4.ddnsd.DDNSD;
import de.linux4.ddnsd.IO;

public class Debian {

	public static final String PACKAGE = "ddnsd-java";
	public static final String VERSION = DDNSD.class.getPackage().getImplementationVersion();
	public static final String SECTION = "utils";
	public static final String PRIORITY = "optional";
	public static final String MAINTAINER = "Tim Zimmermann <tim.zimmermann@linux4.de>";
	public static final String DEPENDS = "default-jre-headless (>=2:1.8), systemd";
	public static final String DOWNLOAD_SIZE = "1.8M";
	public static final String INSTALLED_SIZE = "2.2M";
	public static final String DESCRIPTION = "DDNSD is a background service to dynamically update your IP-Adress in a DNS Zone file. It is compatible with most linux / unix based operating systems.";

	public static void main(String[] args) throws IOException {
		File tmp = new File("debian/tmp");
		tmp.mkdirs();
		File debianDir = new File(tmp, "DEBIAN");
		debianDir.mkdirs();
		IO.write(new File("debian/files"), PACKAGE + "_" + VERSION + "_all.deb utils optional\n" + PACKAGE + "_"
				+ VERSION + "_all.buildinfo utils optional");
		IO.write(new File(debianDir, "control"),
				"Package: " + PACKAGE + "\nVersion: " + VERSION + "\nMaintainer: " + MAINTAINER + "\nDepends: "
						+ DEPENDS + "\nArchitecture: all\nSource: " + PACKAGE + "\nSection: " + SECTION + "\nPriority: "
						+ PRIORITY + "\nDownload-Size: " + DOWNLOAD_SIZE + "\nInstalled-Size: " + INSTALLED_SIZE
						+ "\nDescription: " + DESCRIPTION + "\n");
		IO.write(new File(debianDir, "conffiles"),
				DDNSD.CONFIG.toString() + "\n" + DDNSD.OLDIP.toString() + "\n" + DDNSD.OLDIP6.toString() + "\n");
		IO.write(new File(debianDir, "postinst"),
				"/bin/systemctl enable " + PACKAGE + ".service\n/usr/sbin/service " + PACKAGE + " restart");

		File configDir = new File(tmp, DDNSD.CONFIG_DIR.toString());
		configDir.mkdirs();
		IO.write(new File(tmp, DDNSD.CONFIG.toString()), IO.getContent(DDNSD.class.getResourceAsStream("/ddnsd.ini")));
		IO.write(new File(tmp, DDNSD.OLDIP.toString()), "");
		IO.write(new File(tmp, DDNSD.OLDIP6.toString()), "");

		File usrBin = new File(tmp, "usr/bin");
		usrBin.mkdirs();
		IO.write(new File(usrBin, PACKAGE), "#!/bin/bash\n\njava -jar /opt/" + PACKAGE + "/DDNSD.jar \"$@\"");

		File libSystemdSystem = new File(tmp, "lib/systemd/system");
		libSystemdSystem.mkdirs();
		IO.write(new File(libSystemdSystem, PACKAGE + ".service"),
				"[Unit]\n" + "Description=DDNS Daemon\n" + "After=network.target\n" + "\n" + "[Service]\n"
						+ "ExecStart=/usr/bin/" + PACKAGE + "\n" + "\n" + "[Install]\n" + "WantedBy=multi-user.target\n"
						+ "");
	}

}
