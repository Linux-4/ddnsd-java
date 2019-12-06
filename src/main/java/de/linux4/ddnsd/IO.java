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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;

public class IO {

	public static boolean isNotEmpty(File file) throws FileNotFoundException, IOException {
		return file.exists() && new BufferedReader(new FileReader(file)).readLine().length() > 0;
	}

	public static boolean containsIPv6(File file) throws FileNotFoundException, IOException {
		return file.exists() && isIPv6(new BufferedReader(new FileReader(file)).readLine());
	}

	public static boolean containsIP(File file) throws FileNotFoundException, IOException {
		return file.exists() && isIP(new BufferedReader(new FileReader(file)).readLine());
	}

	public static boolean isIP(String ip) {
		if (ip == null || ip.isEmpty()) {
			return false;
		}
		try {
			InetAddress.getByName(ip);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
	
	public static boolean isIPv4(String ip) {
		return isIP(ip) && !isIPv6(ip);
	}

	public static boolean isIPv6(String ip) {
		if (ip == null || ip.isEmpty()) {
			return false;
		}
		try {
			InetAddress inet = InetAddress.getByName(ip);
			return inet instanceof Inet6Address;
		} catch (Exception ex) {
			return false;
		}
	}

	public static void write(File file, String content) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter writer = new FileWriter(file);
		writer.write(content);
		writer.close();
	}

	public static String getContent(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()));
	}
	
	public static String getContent(InputStream in) throws IOException {
		return new String(IOUtils.toString(in, StandardCharsets.UTF_8));
	}

}
