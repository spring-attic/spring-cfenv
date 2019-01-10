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

import org.junit.Test;

import org.springframework.cfenv.core.CfEnv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.cfenv.jdbc.MySqlJdbcUrlCreator.MYSQL_SCHEME;

/**
 * @author Mark Pollack
 */
public class MySqlJdbcTests extends AbstractJdbcTests {

	@Test
	public void mysqlServiceCreationWithLabelNoTags() {
		String name1 = "database-1";
		String name2 = "database-2";
		when(mockEnvironment.getenv("VCAP_SERVICES"))
				.thenReturn(getServicesPayload(
						getMysqlServicePayloadWithLabelNoTags("mysql-1", hostname, port, username, password, name1),
						getMysqlServicePayloadWithLabelNoTags("mysql-2", hostname, port, username, password, name2)));

		CfEnv cfEnv = new CfEnv(mockEnvironment);
		String jdbcUrlMysql1 = cfEnv.findJdbcUrlByName("mysql-1");
		String jdbcUrlMysql2 = cfEnv.findJdbcUrlByName("mysql-2");

		assertThat(getExpectedJdbcUrl(MYSQL_SCHEME, name1)).isEqualTo(jdbcUrlMysql1);
		assertThat(getExpectedJdbcUrl(MYSQL_SCHEME, name2)).isEqualTo(jdbcUrlMysql2);
	}

	private String getMysqlServicePayloadWithLabelNoTags(String serviceName,
			String hostname, int port,
			String user, String password, String name) {
		return getRelationalPayload("test-mysql-info-with-label-no-tags.json", serviceName,
				hostname, port, user, password, name);
	}
}
