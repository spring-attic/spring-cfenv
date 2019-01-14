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

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.springframework.cfenv.core.CfService;

/**
 * @author Mark Pollack
 */
public class CfJdbcUrlCreator {

	private List<CfService> cfServices;

	private List<JdbcUrlCreator> jdbcUrlCreators;

	public CfJdbcUrlCreator(List<CfService> cfServices) {
		this.cfServices = cfServices;
		jdbcUrlCreators = new ArrayList<>();
		Iterable<JdbcUrlCreator> jdbcUrlCreatorIterable = ServiceLoader.load(JdbcUrlCreator.class);
		for (JdbcUrlCreator jdbcUrlCreator : jdbcUrlCreatorIterable) {
			if (jdbcUrlCreator != null) {
				jdbcUrlCreators.add(jdbcUrlCreator);
			}
		}
	}

	public String getJdbcUrl() {
		for (CfService cfService : cfServices) {
			for (JdbcUrlCreator jdbcUrlCreator : jdbcUrlCreators) {
				if (jdbcUrlCreator.isDatabaseService(cfService)) {
					return jdbcUrlCreator.createJdbcUrl(cfService);
				}
			}
		}
		return null;
	}

	public CfJdbcService getJdbcService() {
		for (CfService cfService : cfServices) {
			for (JdbcUrlCreator jdbcUrlCreator : jdbcUrlCreators) {
				if (jdbcUrlCreator.isDatabaseService(cfService)) {
					CfJdbcService cfJdbcService = new CfJdbcService(cfService.getMap());
					String jdbcUrl = jdbcUrlCreator.createJdbcUrl(cfService);
					cfService.getCredentials().getDerivedCredentials().put("jdbcUrl", jdbcUrl);
					return cfJdbcService;
				}
			}
		}
		return null;
	}

}
