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
package de.linux4.ddnsd.secondary;

import java.io.IOException;
import java.util.HashMap;

import de.linux4.ddnsd.IO;
import de.linux4.ddnsd.WWW;

public class PuckDNS extends SecondaryDNS {

	private final WWW www = new WWW();

	public PuckDNS(String username, String password, int mode) {
		super(username, password, mode);
	}

	@Override
	public boolean login() throws IOException {
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("username", username);
		parameters.put("password", password);
		return www.post("https://puck.nether.net/dns/login", parameters) == 302;
	}

	@Override
	public boolean update(String domain, String ip, String ip6) throws IOException {
		int mode = this.mode == 0 && IO.isIPv6(ip6) ? 2 : 1;
		if ((mode == 1 && !IO.isIPv4(ip)) || (mode == 2 && !IO.isIPv6(ip6))) {
			System.err.println("Tried to use PuckDNS updater but no valid ip for mode = " + mode + " found!");
			return false;
		}
		if (login()) {
			System.out.println("IF1");
			HashMap<String, String> parameters = new HashMap<String, String>();
			parameters.put("domainname", domain);
			parameters.put("masterip", mode == 1 ? ip : ip6);
			parameters.put("aa", "Y");
			parameters.put("submit", "Submit");
			if (www.post("https://puck.nether.net/dns/dnsinfo/edit/" + domain, parameters) == 302) {
				System.out.println("PuckDNS successfully updated " + domain);
				return true;
			} else {
				System.err.println("PuckDNS failed updating " + domain);
				return false;
			}
		}
		System.err.println("PuckDNS login failed");
		return false;
	}

}
