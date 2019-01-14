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

import org.springframework.cfenv.core.UriInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.cfenv.jdbc.MySqlJdbcUrlCreator.MYSQL_SCHEME;

/**
 * @author Mark Pollack
 */
public class MySqlJdbcTests extends AbstractJdbcTests {


	@Test
	public void mysqlServiceCreation() {
		String name1 = "database-1";
		String name2 = "database-2";

		mockVcapServices(getServicesPayload(
				getMysqlServicePayload("mysql-1", hostname, port, username, password, name1),
				getMysqlServicePayload("mysql-2", hostname, port, username, password, name2)));

		assertJdbcUrls(name1, name2);


	}

	@Test
	public void mysqlServiceCreationWithLabelNoTags() {
		String name1 = "database-1";
		String name2 = "database-2";
		mockVcapServices(getServicesPayload(
						getMysqlServicePayloadWithLabelNoTags("mysql-1", hostname, port, username, password, name1),
						getMysqlServicePayloadWithLabelNoTags("mysql-2", hostname, port, username, password, name2)));

		assertJdbcUrls(name1, name2);

	}

	@Test
	public void mysqlServiceCreationNoLabelNoTags() {
		String name1 = "database-1";
		String name2 = "database-2";
		mockVcapServices(getServicesPayload(
						getMysqlServicePayloadNoLabelNoTags("mysql-1", hostname, port, username, password, name1),
						getMysqlServicePayloadNoLabelNoTags("mysql-2", hostname, port, username, password, name2)));

		assertJdbcUrls(name1, name2);

	}

	@Test
	public void mysqlServiceCreationNoLabelNoTagsWithSpecialChars() {
		String name = "database";
		String userWithSpecialChars = "u%u:u+";
		String passwordWithSpecialChars = "p%p:p+";

		mockVcapServices(getServicesPayload(
						getMysqlServicePayloadNoLabelNoTags("mysql", hostname, port, userWithSpecialChars,
								passwordWithSpecialChars, name)));

		CfEnvJdbc cfEnvJdbc = new CfEnvJdbc();
		String jdbcUrlMysql = cfEnvJdbc.findJdbcUrlByName("mysql");

		assertThat(getExpectedMysqlJdbcUrl(hostname, port, name, userWithSpecialChars, passwordWithSpecialChars))
				.isEqualTo(jdbcUrlMysql);
	}

	@Test
	public void mysqlServiceCreationWithLabelNoUri() {
		String name1 = "database-1";
		String name2 = "database-2";
		mockVcapServices(getServicesPayload(
						getMysqlServicePayloadWithLabelNoUri("mysql-1", hostname, port, username, password, name1),
						getMysqlServicePayloadWithLabelNoUri("mysql-2", hostname, port, username, password, name2)));

		assertJdbcUrls(name1, name2);
	}

	@Test
	public void mysqlServiceCreationWithJdbcUrl() {
		String name1 = "database-1";
		String name2 = "database-2";
		mockVcapServices(getServicesPayload(
						getMysqlServicePayloadWithJdbcUrl("mysql-1", hostname, port, username, password, name1),
						getMysqlServicePayloadWithJdbcUrl("mysql-2", hostname, port, username, password, name2)));

		assertJdbcUrls(name1, name2);
	}

	@Test
	public void mysqlServiceCreationWithJdbcUrlAndSpecialChars() {
		String userWithSpecialChars = "u%u:u+";
		String passwordWithSpecialChars = "p%p:p+";
		String name = "database";
		mockVcapServices(getServicesPayload(
						getMysqlServicePayloadWithJdbcUrl("mysql", hostname, port, userWithSpecialChars,
								passwordWithSpecialChars, name)));

		CfEnvJdbc cfEnvJdbc = new CfEnvJdbc();
		String jdbcUrlMysql = cfEnvJdbc.findJdbcUrlByName("mysql");

		assertThat(getExpectedMysqlJdbcUrl(hostname, port, name, userWithSpecialChars, passwordWithSpecialChars))
				.isEqualTo(jdbcUrlMysql);
	}

	@Test
	public void mysqlServiceCreationWithJdbcUrlOnly() {
		String name1 = "database-1";
		String name2 = "database-2";
		mockVcapServices(getServicesPayload(
						getMysqlServicePayloadWithJdbcUrlOnly("mysql-1", hostname, port, username, password, name1),
						getMysqlServicePayloadWithJdbcUrlOnly("mysql-2", hostname, port, username, password, name2)));

		assertJdbcUrls(name1, name2);
	}

	@Test
	public void mysqlServiceCreationWithJdbcUrlOnlyWithSpecialChars() {
		String name = "database";
		String userWithSpecialChars = "u%u:u+";
		String passwordWithSpecialChars = "p%p:p+";
		mockVcapServices(getServicesPayload(
						getMysqlServicePayloadWithJdbcUrlOnly("mysql", hostname, port, userWithSpecialChars,
								passwordWithSpecialChars, name)));

		CfEnvJdbc cfEnvJdbc = new CfEnvJdbc();
		String jdbcUrlMysql = cfEnvJdbc.findJdbcUrlByName("mysql");
		assertThat(getExpectedMysqlJdbcUrl(hostname, port, name, userWithSpecialChars, passwordWithSpecialChars))
				.isEqualTo(jdbcUrlMysql);
	}

	// Utility methods

	private void assertJdbcUrls(String name1, String name2) {
		CfEnvJdbc cfEnvJdbc = new CfEnvJdbc();
		String jdbcUrlMysql1 = cfEnvJdbc.findJdbcUrlByName("mysql-1");
		String jdbcUrlMysql2 = cfEnvJdbc.findJdbcUrlByName("mysql-2");

		assertThat(getExpectedJdbcUrl(MYSQL_SCHEME, name1)).isEqualTo(jdbcUrlMysql1);
		assertThat(getExpectedJdbcUrl(MYSQL_SCHEME, name2)).isEqualTo(jdbcUrlMysql2);
	}

	private String getExpectedMysqlJdbcUrl(String hostname, int port, String name, String user,
			String password) {
		return String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s", hostname, port,
				name, UriInfo.urlEncode(user), UriInfo.urlEncode(password));
	}

	private String getMysqlServicePayload(String serviceName,
			String hostname, int port,
			String user, String password, String name) {
		return getRelationalPayload("test-mysql-info.json", serviceName,
				hostname, port, user, password, name);
	}


	private String getMysqlServicePayloadWithLabelNoTags(String serviceName,
			String hostname, int port,
			String user, String password, String name) {
		return getRelationalPayload("test-mysql-info-with-label-no-tags.json", serviceName,
				hostname, port, user, password, name);
	}

	private String getMysqlServicePayloadNoLabelNoTags(String serviceName,
			String hostname, int port,
			String user, String password, String name) {
		return getRelationalPayload("test-mysql-info-no-label-no-tags.json", serviceName,
				hostname, port, user, password, name);
	}

	private String getMysqlServicePayloadWithLabelNoUri(String serviceName,
			String hostname, int port,
			String user, String password, String name) {
		return getRelationalPayload("test-mysql-info-with-label-no-uri.json", serviceName,
				hostname, port, user, password, name);
	}

	private String getMysqlServicePayloadWithJdbcUrl(String serviceName,
			String hostname, int port,
			String user, String password, String name) {
		return getRelationalPayload("test-mysql-info-jdbc-url.json", serviceName,
				hostname, port, user, password, name);
	}

	private String getMysqlServicePayloadWithJdbcUrlOnly(String serviceName,
			String hostname, int port,
			String user, String password, String name) {
		return getRelationalPayload("test-mysql-info-jdbc-url-only.json", serviceName,
				hostname, port, user, password, name);
	}

}
