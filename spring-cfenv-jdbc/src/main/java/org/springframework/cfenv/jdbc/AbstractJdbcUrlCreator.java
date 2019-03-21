/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cfenv.jdbc;

import org.springframework.cfenv.core.CfCredentials;
import org.springframework.cfenv.core.CfService;

/**
 * @author Mark Pollack
 */
public abstract class AbstractJdbcUrlCreator implements JdbcUrlCreator {

	public static final String JDBC_PREFIX = "jdbc:";

	protected boolean jdbcUrlMatchesScheme(CfService cfService, String... uriSchemes) {
		CfCredentials cfCredentials = cfService.getCredentials();
		String jdbcUrl = (String) cfCredentials.getMap().get("jdbcUrl");
		if (jdbcUrl != null) {
			for (String uriScheme : uriSchemes) {
				if (jdbcUrl.startsWith(JDBC_PREFIX + uriScheme + ":")) {
					return true;
				}
			}
		}
		return false;
	}

}
