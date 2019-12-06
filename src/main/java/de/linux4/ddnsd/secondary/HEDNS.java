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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.linux4.ddnsd.IO;
import de.linux4.ddnsd.WWW;

public class HEDNS extends SecondaryDNS {

	private final WWW www = new WWW();

	public HEDNS(String username, String password, int mode) {
		super(username, password, mode);
	}

	@Override
	public boolean login() throws IOException {
		www.getContent("https://dns.he.net/"); // needed to get CGISESSID cookie
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("email", username);
		parameters.put("pass", password);
		return isLoggedIn(Jsoup.parse(www.postGetContent("https://dns.he.net/index.cgi", parameters)));
	}

	private boolean isLoggedIn(Document doc) {
		return doc.getElementsByAttributeValue("name", "account").size() > 0;
	}

	private boolean isDeleted(Document doc, String domain) {
		return doc.getElementsByAttributeValue("name", domain.toLowerCase()).size() == 0;
	}

	private boolean isAdded(Document doc, String domain) {
		return !isDeleted(doc, domain);
	}

	@Override
	public boolean update(String domain, String ip, String ip6) throws IOException {
		int mode = this.mode == 0 && IO.isIPv6(ip6) ? 2 : 1;
		if ((mode == 1 && !IO.isIPv4(ip)) || (mode == 2 && !IO.isIPv6(ip6))) {
			System.err.println("Tried to use HEDNS updater but no valid ip for mode = " + mode + " found!");
			return false;
		}

		if (login()) {
			HashMap<String, String> parameters = new HashMap<String, String>();
			Document doc = Jsoup.parse(www.getContent("https://dns.he.net/"));
			Elements elements = doc.getElementsByAttributeValue("name", domain.toLowerCase());
			String id = "0";
			for (Element element : elements) {
				if (element.hasAttr("value")) {
					id = element.attr("value");
				}
			}
			parameters.put("account", doc.getElementsByAttributeValue("name", "account").get(0).attr("value"));
			parameters.put("delete_id", id);
			parameters.put("remove_domain", "" + 1);
			if (isDeleted(Jsoup.parse(www.postGetContent("https://dns.he.net/index.cgi", parameters)), domain)) {
				parameters.clear();
				parameters.put("add_slave", domain);
				parameters.put("master1", mode == 1 ? ip : ip6);
				if (isAdded(Jsoup.parse(www.postGetContent("https://dns.he.net/index.cgi", parameters)), domain)) {
					System.out.println("HEDNS successfully updated " + domain);
					return true;
				} else {
					System.err.println("HEDNS failed updating " + domain + " (add failed)");
					return false;
				}
			} else {
				System.err.println("HEDNS failed updating " + domain + " (delete failed)");
				return false;
			}
		}
		System.err.println("HEDNS login failed");
		return false;
	}

}
