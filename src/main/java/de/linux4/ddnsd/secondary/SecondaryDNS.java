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

public abstract class SecondaryDNS {

	protected final String username;
	protected final String password;
	protected final int mode;
	
	public SecondaryDNS(String username, String password, int mode) {
		this.username = username;
		this.password = password;
		this.mode = mode;
	}
	
	public abstract boolean login() throws IOException;
	
	public abstract boolean update(String domain, String ip, String ip6) throws IOException;
	
}
