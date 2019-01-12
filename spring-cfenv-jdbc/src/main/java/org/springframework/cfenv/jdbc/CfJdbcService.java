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
package org.springframework.cfenv.jdbc;

import org.springframework.cfenv.core.CfService;
import org.springframework.cfenv.util.UriInfo;

/**
 * Wrapper for a relational database service.
 * @author Mark Pollack
 */
public class CfJdbcService {

	private  CfService cfService;
	private String jdbcUrl;

	public CfJdbcService(CfService cfService, String jdbcUrl) {
		this.cfService = cfService;
		this.jdbcUrl = jdbcUrl;
	}

	public CfService getCfService() {
		return cfService;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public UriInfo getUriInfo() {
		String jdbcUrlWithoutPrefix = jdbcUrl.substring(5);
		String scheme = jdbcUrlWithoutPrefix.substring(0,  jdbcUrlWithoutPrefix.indexOf(":"));
		return this.cfService.getCredentials().getUriInfo(scheme);
	}

	public String getDriverClassname() {
		//TODO return based on scheme name and driver class path availability
		return null;
	}
}
