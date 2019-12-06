/*******************************************************************************
 * Copyright (C) 2019 Linux4
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.linux4.ddnsd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import ca.szc.configparser.Ini;
import de.linux4.ddnsd.secondary.HEDNS;
import de.linux4.ddnsd.secondary.PuckDNS;

public class DDNSD {

	public static final String V4_API = "http://v4.ident.me";
	public static final String V6_API = "http://v6.ident.me";

	public static final Path CONFIG_DIR = Paths.get("/etc/ddns/");
	public static final Path CONFIG = Paths.get(CONFIG_DIR.toString(), "ddnsd.ini");
	public static final int CONFIG_VERSION = 1;
	public static final Path OLDIP = Paths.get(CONFIG_DIR.toString(), ".oldip.ddns");
	public static final Path OLDIP6 = Paths.get(CONFIG_DIR.toString(), ".oldip6.ddns");

	public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

	public static void main(String[] args) throws InterruptedException, IOException {
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("--version") || args[0].equalsIgnoreCase("-version")) {
				System.out.println("DDNSD v" + DDNSD.class.getPackage().getImplementationVersion());
				return;
			}
		}
		new DDNSD(CONFIG);
	}

	private WWW www = new WWW();

	public DDNSD(Path config) throws IOException, InterruptedException {
		System.out.println("Starting DDNSD v" + DDNSD.class.getPackage().getImplementationVersion());
		if (IO.isNotEmpty(config.toFile())) {
			Ini ini = new Ini().read(config);

			if (ini.getSections().containsKey("ddnsd")) {
				Map<String, String> ddnsd = ini.getSections().get("ddnsd");
				int config_version = Integer.parseInt(ddnsd.get("config_version"));

				if (config_version > CONFIG_VERSION) {
					System.out.println("Trying to parse config version " + config_version + ", this might not work!");
				}

				int update_freq = Integer.parseInt(ddnsd.get("update_freq"));

				boolean use_puckdns = Boolean.valueOf(ddnsd.get("use_puckdns"));
				Map<String, String> puckdnsSection = ini.getSections().get("puckdns");
				int puckdns_mode = Integer.parseInt(puckdnsSection.get("mode"));
				PuckDNS puckdns = use_puckdns
						? new PuckDNS(puckdnsSection.get("username"), puckdnsSection.get("password"), puckdns_mode)
						: null;
				String[] puckdns_domains = puckdnsSection.get("domains").split(",");

				boolean use_hedns = Boolean.valueOf(ddnsd.get("use_hedns"));
				Map<String, String> hednsSection = ini.getSections().get("hedns");
				int hedns_mode = Integer.parseInt(hednsSection.get("mode"));
				HEDNS hedns = use_hedns
						? new HEDNS(hednsSection.get("username"), hednsSection.get("password"), hedns_mode)
						: null;
				String[] hedns_domains = hednsSection.get("domains").split(",");

				String post_update_cmd = ddnsd.get("post_update_cmd");

				puckdnsSection = null;
				hednsSection = null;
				ini = null;

				if (!IO.containsIP(OLDIP.toFile()) || IO.containsIPv6(OLDIP.toFile())) {
					IO.write(OLDIP.toFile(), www.getContent(V4_API));
				}

				if (!IO.containsIP(OLDIP6.toFile()) || !IO.containsIPv6(OLDIP6.toFile())) {
					IO.write(OLDIP6.toFile(), www.getContent(V6_API));
				}

				while (true) {
					String oldip = IO.getContent(OLDIP.toFile());
					String oldip6 = IO.getContent(OLDIP6.toFile());
					String ip = null;
					try {
						ip = www.getContent(V4_API);
					} catch (IOException e) {
						e.printStackTrace();
					}
					String ip6 = null;
					try {
						ip6 = www.getContent(V6_API);
					} catch (IOException e) {
						e.printStackTrace();
					}

					boolean ipv4 = true;
					if (!IO.isIP(ip)) {
						System.err.println("Failed to get valid IPv4 Address!");
						ip = oldip;
						ipv4 = false;
					} else if (IO.isIPv6(ip)) {
						System.err.println("Tried to get IPv4 Address but got IPv6 Address!");
						ip = oldip;
						ipv4 = false;
					}

					boolean ipv6 = true;
					if (!IO.isIP(ip6)) {
						System.err.println("Failed to get valid IPv6 Address!");
						ip6 = oldip6;
						ipv6 = false;
					} else if (!IO.isIPv6(ip6)) {
						System.err.println("Tried to get IPv6 Address but got IPv4 Address!");
						ip6 = oldip6;
						ipv6 = false;
					}

					if (!ip.equalsIgnoreCase(oldip) || !ip6.equalsIgnoreCase(oldip6)) {
						System.out.println("Detected IP change!");

						if (use_puckdns) {
							System.out.println("Starting PuckDNS updater");
							for (String domain : puckdns_domains) {
								for (int i = 0; i < 3; i++) {
									try {
										puckdns.update(domain, ipv4 ? ip : null, ipv6 ? ip6 : null);
										break;
									} catch (IOException e) {
										System.err.println(i < 2
												? "Retrying PuckDNS update of domain " + domain + " because of error: "
												: "PuckDNS update of domain " + domain + "exited with: ");
										e.printStackTrace();
									}
									Thread.sleep(3000);
								}
							}
						}

						if (use_hedns) {
							System.out.println("Starting HEDNS updater");
							for (String domain : hedns_domains) {
								for (int i = 0; i < 3; i++) {
									try {
										hedns.update(domain, ipv4 ? ip : null, ipv6 ? ip6 : null);
										break;
									} catch (IOException e) {
										System.err.println(i < 2
												? "Retrying HEDNS update of domain " + domain + " because of error: "
												: "HEDNS update of domain " + domain + " exited with: ");
									}
								}
							}
						}

						if (!ip.equalsIgnoreCase(oldip)) {
							System.out.println("Updating DNS zone (ipv4)");
							updateIP(oldip, ip, ddnsd.get("zones").split(","), !ip6.equalsIgnoreCase(oldip6));
							IO.write(OLDIP.toFile(), ip);
						}

						if (!ip6.equalsIgnoreCase(oldip6)) {
							System.out.println("Updating DNS zone (ipv6)");
							updateIP(oldip6, ip6, ddnsd.get("zones").split(","), true);
							IO.write(OLDIP6.toFile(), ip6);
						}

						Runtime.getRuntime().exec(post_update_cmd);
					}

					Thread.sleep(update_freq * 1000);
				}

			} else {
				genConfig(config);
			}
		} else {
			genConfig(config);
		}
	}

	private void updateIP(String oldip, String ip, String[] zones, boolean updateSerial) throws IOException {
		for (String zone : zones) {
			String[] broken = zone.split(":");
			String domain = broken[0];
			File file = new File(broken[1]);

			if (file.exists()) {
				String serial = getSerial(domain);
				String zoneContent = IO.getContent(file);
				zoneContent.replaceAll(oldip, ip);

				if (updateSerial) {
					if (serial.length() == 10) {
						String date = serial.substring(0, serial.length() - 2);
						int version = Integer.parseInt(serial.substring(serial.length() - 2));
						String now = DATE.format(LocalDateTime.now());

						String newSerial = serial;
						if (date.equalsIgnoreCase(now)) {
							if (version >= 99) {
								version = 1;
							} else {
								version++;
							}
							newSerial = now + (version >= 10 ? version : "0" + version);
						} else {
							newSerial = now + "01";
						}

						zoneContent.replaceFirst(serial, newSerial);
					} else {
						System.err.println("Serial of domain " + domain
								+ " does not have recommended format of YYYYMMDDVV, trying without updating serial");
					}
				}

				IO.write(file, zoneContent);
			} else {
				System.err.println("The given DNS Zone file (" + file.getPath() + ") does not exist!");
			}
		}
	}

	private void genConfig(Path config) throws IOException {
		IO.write(config.toFile(), IO.getContent(DDNSD.class.getResourceAsStream("ddnsd.ini")));
	}

	private String getSerial(String domain) {
		try {
			InitialDirContext iDirC = new InitialDirContext();
			Attributes attributes = iDirC.getAttributes("dns:/" + domain, new String[] { "SOA" });
			Attribute attributeSOA = attributes.get("SOA");
			return attributeSOA.get(0).toString().split(" ")[2];
		} catch (Exception ex) {
			return "1970010101";
		}
	}

}
