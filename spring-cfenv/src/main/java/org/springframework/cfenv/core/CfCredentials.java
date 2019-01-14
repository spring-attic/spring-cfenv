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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mark Pollack
 */
public class CfCredentials {

	private final Map<String, Object> credentailsData;

	private UriInfo uriInfo;

	private Map<String, String> derivedCredentials = new HashMap<>();

	public CfCredentials(Map<String, Object> credentailsData) {
		this.credentailsData = credentailsData;
	}

	private synchronized UriInfo createOrGetUriInfo() {
		try {
			if (uriInfo == null) {
				if (credentailsData.containsKey("uri") || credentailsData.containsKey("url")) {
					uriInfo = new UriInfo(getString(new String[] { "uri", "url" }));
				}
			}
		}
		catch (Exception e) {

		}
		return uriInfo;
	}

	public Map<String, Object> getMap() {
		return credentailsData;
	}

	public Map<String, String> getDerivedCredentials() {
		return derivedCredentials;
	}

	/**
	 * Looks for the keys 'host' and 'hostname' in the credential map
	 * @return value of the host or hostname key if present, null otherwise.
	 */
	public String getHost() {
		String host = getString(new String[] { "host", "hostname" });
		if (host != null) {
			return host;
		}
		UriInfo uriInfo = createOrGetUriInfo();
		return (uriInfo != null) ? uriInfo.getHost() : null;
	}

	public String getPort() {
		String port = getString("port");
		if (port != null) {
			return port;
		}
		UriInfo uriInfo = createOrGetUriInfo();
		return (uriInfo != null) ? String.valueOf(uriInfo.getPort()) : null;
	}

	public String getName() {
		return getString("name");
	}

	/**
	 * Looks for the keys 'username' and 'user' in the credential map
	 * @return value of the username or user key.
	 */
	public String getUsername() {
		String username = getString(new String[] { "username", "user" });
		if (username != null) {
			return username;
		}
		UriInfo uriInfo = createOrGetUriInfo();
		return (uriInfo != null) ? uriInfo.getUsername() : null;
	}

	public String getPassword() {
		String password = getString("password");
		if (password != null) {
			return password;
		}
		UriInfo uriInfo = createOrGetUriInfo();
		return (uriInfo != null) ? uriInfo.getPassword() : null;
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
	 * Return UriInfo derived from the field 'uri' or 'url'
	 * @return the UriInfo object
	 */
	public UriInfo getUriInfo() {
		return createOrGetUriInfo();
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
