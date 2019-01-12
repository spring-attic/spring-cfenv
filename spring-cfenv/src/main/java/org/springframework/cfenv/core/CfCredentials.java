/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cfenv.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.cfenv.util.UriInfo;

/**
 * @author Mark Pollack
 */
public class CfCredentials {

	private final Map<String, Object> credentailsData;

	public CfCredentials(Map<String, Object> credentailsData) {
		this.credentailsData = credentailsData;
	}

	public Map<String, Object> getMap() {
		return credentailsData;
	}

	/**
	 * Looks for the keys 'host' and 'hostname' in the credential map
	 * @return value of the host or hostname key.
	 */
	public String getHost() {
		return getString(new String[] { "host", "hostname" });
	}

	public String getPort() {
		return getString("port");
	}

	public String getName() {
		return getString("name");
	}

	/**
	 * Looks for the keys 'username' and 'user' in the credential map
	 * @return value of the username or user key.
	 */
	public String getUsername() {
		return getString(new String[] { "username", "user" });
	}

	public String getPassword() {
		return getString("password");
	}

	/**
	 * Return the URI field, by default look under the field name 'uri' and 'url', but also
	 * optionally look in field names that begin with the uriScheme and are suffixed with
	 * 'Uri', 'uri', 'Url' and 'url'
	 * @param uriSchemes optional list of uri scheme names to use as a prefix for an expanded
	 * search of the uri field
	 * @return the value of the uri field
	 */
	public String getUri(String... uriSchemes) {
		List<String> keys = new ArrayList<String>();
		keys.addAll(Arrays.asList("uri", "url"));
		for (String uriScheme : uriSchemes) {
			keys.add(uriScheme + "Uri");
			keys.add(uriScheme + "uri");
			keys.add(uriScheme + "Url");
			keys.add(uriScheme + "url");
		}
		return getString(keys.toArray(new String[keys.size()]));

	}

	/**
	 * Return UriInfo derived from URI field
	 * @param uriScheme a uri scheme name to use as a prefix to search of the uri field
	 * @return the UriInfo object
	 */
	public UriInfo getUriInfo(String uriScheme) {
		String uri = getUri(uriScheme);
		UriInfo uriInfo;
		if (uri == null) {
			String hostname = getHost();
			String port = getPort();
			String username = getUsername();
			String password = getPassword();
			String databaseName = getName();
			uriInfo = new UriInfo(uriScheme, hostname, Integer.valueOf(port), username, password, databaseName);
		}
		else {
			uriInfo = new UriInfo(uri);
		}
		return uriInfo;
	}

	public String getString(String... keys) {
		if (this.credentailsData != null) {
			for (String key : keys) {
				if (this.credentailsData.containsKey(key)) {
					return this.credentailsData.get(key).toString();
				}
			}
		}
		return null;
	}

}
