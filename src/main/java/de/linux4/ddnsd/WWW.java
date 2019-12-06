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

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class WWW {

	public final BasicCookieStore cookieStore = new BasicCookieStore();
	public final CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore)
			.build();

	public String getContent(String url) throws ClientProtocolException, IOException {
		HttpGet get = new HttpGet(url);
		CloseableHttpResponse response = httpClient.execute(get);
		return EntityUtils.toString(response.getEntity());
	}

	public int post(String url, Map<String, String> parameters) throws ClientProtocolException, IOException {
		RequestBuilder builder = RequestBuilder.post().setUri(url);
		for (String key : parameters.keySet()) {
			builder.addParameter(key, parameters.get(key));
		}
		HttpUriRequest post = builder.build();
		CloseableHttpResponse response = httpClient.execute(post);
		return response.getStatusLine().getStatusCode();
	}

	public String postGetContent(String url, Map<String, String> parameters)
			throws ClientProtocolException, IOException {
		RequestBuilder builder = RequestBuilder.post().setUri(url);
		for (String key : parameters.keySet()) {
			builder.addParameter(key, parameters.get(key));
		}
		HttpUriRequest post = builder.build();
		CloseableHttpResponse response = httpClient.execute(post);
		return EntityUtils.toString(response.getEntity());
	}

}
