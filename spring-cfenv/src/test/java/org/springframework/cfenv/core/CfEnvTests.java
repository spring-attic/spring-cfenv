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

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.springframework.cfenv.util.AbstractTestSupport;
import org.springframework.cfenv.util.UriInfo;
import org.springframework.util.ResourceUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * @author Mark Pollack
 */
public class CfEnvTests extends AbstractTestSupport {

	@Test
	public void testCfService() {
		CfEnv cfEnv = createTestCfEnv();

		List<CfService> cfServices = cfEnv.findAllServices();
		assertThat(cfServices.size()).isEqualTo(2);

		CfService cfService = cfEnv.findServiceByTag("mysql");
		assertThat(cfService.getString("blah")).isNull();

		assertThat(cfService.getTags()).containsExactly("mysql", "relational");
		assertThat(cfService.getMap()).containsEntry("syslog_drain_url", null)
				.containsEntry("volume_mounts", new ArrayList<String>())
				.containsEntry("label", "p-mysql")
				.containsEntry("provider", null)
				.containsEntry("plan", "100mb")
				.containsEntry("name", "mysql")
				.containsKey("credentials");

		CfCredentials cfCredentials = cfService.getCredentials();
		assertMySqlCredentials(cfCredentials);

		// Query methods

		assertThat(cfService.existsByTagIgnoreCase()).isFalse();
		assertThat(cfService.existsByTagIgnoreCase((String[]) null)).isFalse();
		assertThat(cfService.existsByTagIgnoreCase("relational")).isTrue();
		assertThat(cfService.existsByTagIgnoreCase("ReLaTiOnAl")).isTrue();
		assertThat(cfService.existsByTagIgnoreCase("blah")).isFalse();
		assertThat(cfService.existsByTagIgnoreCase("blah", "relational")).isTrue();

		assertThat(cfService.existsByUriSchemeStartsWith()).isFalse();
		assertThat(cfService.existsByUriSchemeStartsWith((String[]) null)).isFalse();
		assertThat(cfService.existsByUriSchemeStartsWith("mysql")).isTrue();
		assertThat(cfService.existsByUriSchemeStartsWith("MYSQL")).isFalse();
		assertThat(cfService.existsByUriSchemeStartsWith("blah")).isFalse();
		assertThat(cfService.existsByUriSchemeStartsWith("blah", "mysql")).isTrue();

		assertThat(cfService.existsByCredentialsContainsUriField()).isFalse();
		assertThat(cfService.existsByCredentialsContainsUriField((String[]) null)).isFalse();
		assertThat(cfService.existsByCredentialsContainsUriField("mysql")).isFalse();
		// TODO sample data does not support testing for .isTrue

		// Test Redis Entries

		cfService = cfEnv.findServiceByTag("redis");
		assertThat(cfService.getTags()).containsExactly("pivotal", "redis");
		cfCredentials = cfService.getCredentials();
		Map<String, Object> credentialMap = cfCredentials.getMap();
		credentialMap = cfCredentials.getMap();
		assertThat(credentialMap).containsEntry("host", "10.0.4.30");
		assertThat(cfCredentials.getHost()).isEqualTo("10.0.4.30");

	}

	private void assertMySqlCredentials(CfCredentials cfCredentials) {
		Map<String, Object> credentialMap = cfCredentials.getMap();
		assertThat(credentialMap).containsEntry("hostname", "10.0.4.35")
				.containsEntry("port", 3306)
				.containsEntry("name", "mysql_name")
				.containsEntry("username", "mysql_username")
				.containsEntry("password", "mysql_password")
				.containsEntry("uri",
						"mysql://mysql_username:mysql_password@10.0.4.35:3306/cf_2e23d10a_8738_8c3c_66cf_13e44422698c?reconnect=true")
				.containsEntry("jdbcUrl",
						"jdbc:mysql://10.0.4.35:3306/cf_2e23d10a_8738_8c3c_66cf_13e44422698c?user=mysql_username&password=mysql_password");

		assertThat(cfCredentials.getUsername()).isEqualTo("mysql_username");
		assertThat(cfCredentials.getPassword()).isEqualTo("mysql_password");
		assertThat(cfCredentials.getHost()).isEqualTo("10.0.4.35");
		assertThat(cfCredentials.getPort()).isEqualTo("3306");
		assertThat(cfCredentials.getUri()).isEqualTo(
				"mysql://mysql_username:mysql_password@10.0.4.35:3306/cf_2e23d10a_8738_8c3c_66cf_13e44422698c?reconnect=true");

		UriInfo uriInfo = cfCredentials.getUriInfo("mysql");
		assertThat(uriInfo.getUsername()).isEqualTo("mysql_username");
		assertThat(uriInfo.getPassword()).isEqualTo("mysql_password");
		assertThat(uriInfo.getHost()).isEqualTo("10.0.4.35");
		assertThat(uriInfo.getPort()).isEqualTo(3306);
		// assertThat(cfCredentials.findJdbcUrl()).isEqualTo(
		// "jdbc:mysql://10.0.4.35:3306/cf_2e23d10a_8738_8c3c_66cf_13e44422698c?user=mysql_username&password=mysql_password");

	}

	@Test
	public void testFindServiceByName() {
		CfEnv cfEnv = createTestCfEnv();
		CfService cfService = cfEnv.findServiceByName("redis");
		assertThat(cfService.getLabel()).isEqualTo("p-redis");
		assertThat(cfService.getPlan()).isEqualTo("shared-vm");
		assertThat(cfService.getName()).isEqualTo("redis");

		cfService = cfEnv.findServiceByName("blah", "redis");
		assertThat(cfService.getLabel()).isEqualTo("p-redis");

		cfService = cfEnv.findServiceByName(".*sql");
		assertThat(cfService.getName()).isEqualTo("mysql");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByName("blah");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with name [blah]");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByName("");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with name []");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByName((String[]) null);
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with name [null]");

	}

	@Test
	public void testFindServiceByLabel() {
		CfEnv cfEnv = createTestCfEnv();
		CfService cfService = cfEnv.findServiceByLabel("p-redis");
		assertThat(cfService.getLabel()).isEqualTo("p-redis");
		assertThat(cfService.getPlan()).isEqualTo("shared-vm");
		assertThat(cfService.getName()).isEqualTo("redis");

		cfService = cfEnv.findServiceByLabel("blah", "p-redis");
		assertThat(cfService.getLabel()).isEqualTo("p-redis");
		assertThat(cfService.getName()).isEqualTo("redis");

		cfService = cfEnv.findServiceByLabel(".*redis");
		assertThat(cfService.getLabel()).isEqualTo("p-redis");
		assertThat(cfService.getName()).isEqualTo("redis");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByLabel("blah");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with label [blah]");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByLabel("");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with label []");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByLabel((String[]) null);
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with label [null]");

	}

	@Test
	public void testFindServiceByTag() {
		CfEnv cfEnv = createTestCfEnv();
		CfService cfService = cfEnv.findServiceByTag("redis");
		assertThat(cfService.getLabel()).isEqualTo("p-redis");
		assertThat(cfService.getPlan()).isEqualTo("shared-vm");
		assertThat(cfService.getName()).isEqualTo("redis");

		cfService = cfEnv.findServiceByTag("blah", "redis");
		assertThat(cfService.getLabel()).isEqualTo("p-redis");

		cfService = cfEnv.findServiceByTag(".*sql");
		assertThat(cfService.getName()).isEqualTo("mysql");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByTag("blah");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with tag [blah]");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByTag("");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with tag []");

		assertThatThrownBy(() -> {
			cfEnv.findServiceByTag((String[]) null);
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with tag [null]");
	}

	@Test
	public void testFindCredentialsByName() {
		CfEnv cfEnv = createTestCfEnv();

		CfCredentials cfCredentials = cfEnv.findCredentialsByName("mysql");
		assertMySqlCredentials(cfCredentials);

		cfCredentials = cfEnv.findCredentialsByName("blah", "mysql");
		assertMySqlCredentials(cfCredentials);

		cfCredentials = cfEnv.findCredentialsByName(".*sql");
		assertMySqlCredentials(cfCredentials);

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByName("blah");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with name [blah]");

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByName("");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with name []");

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByName((String[]) null);
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with name [null]");

	}

	@Test
	public void testFindCredentialsByLabel() {
		CfEnv cfEnv = createTestCfEnv();

		CfCredentials cfCredentials = cfEnv.findCredentialsByLabel("p-mysql");
		assertMySqlCredentials(cfCredentials);

		cfCredentials = cfEnv.findCredentialsByLabel("blah", "p-mysql");
		assertMySqlCredentials(cfCredentials);

		cfCredentials = cfEnv.findCredentialsByLabel(".*mysql");
		assertMySqlCredentials(cfCredentials);

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByLabel("blah");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with label [blah]");

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByLabel("");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with label []");

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByLabel((String[]) null);
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with label [null]");
	}

	@Test
	public void testFindCredentialsByTag() {
		CfEnv cfEnv = createTestCfEnv();

		CfCredentials cfCredentials = cfEnv.findCredentialsByTag("mysql");
		assertMySqlCredentials(cfCredentials);

		cfCredentials = cfEnv.findCredentialsByTag("blah", "mysql");
		assertMySqlCredentials(cfCredentials);

		cfCredentials = cfEnv.findCredentialsByTag(".*sql");
		assertMySqlCredentials(cfCredentials);

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByTag("blah");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with tag [blah]");

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByTag("");
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with tag []");

		assertThatThrownBy(() -> {
			cfEnv.findCredentialsByTag((String[]) null);
		}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No service with tag [null]");

	}

	@Test
	public void testMultipleMatchingServices() {
		CfEnv cfEnv = createTestCfEnv("vcap-services-multiple-mysql.json");
		List<CfService> services = cfEnv.findAllServices();
		assertThat(services.size()).isEqualTo(3);

		assertThatThrownBy(() -> {
			CfService service = cfEnv.findServiceByName("mysql.*");
		}).isInstanceOf(IllegalStateException.class).hasMessageContaining(
				"No unique service matching by name [mysql.*] was found.  Matching service names are [mysql, mysql2]");

		assertThatThrownBy(() -> {
			CfService service = cfEnv.findServiceByLabel("p-mysql");
		}).isInstanceOf(IllegalStateException.class).hasMessageContaining(
				"No unique service matching by label [p-mysql] was found.  Matching service names are [mysql, mysql2]");

		assertThatThrownBy(() -> {
			CfService service = cfEnv.findServiceByTag("mysql");
		}).isInstanceOf(IllegalStateException.class).hasMessageContaining(
				"No unique service matching by tag [mysql] was found.  Matching service names are [mysql, mysql2]");

	}

	private CfEnv createTestCfEnv(String fileName) {
		File file = null;
		try {
			file = ResourceUtils.getFile("classpath:" + fileName);

			String fileContents = new String(Files.readAllBytes(file.toPath()));
			when(mockEnvironmentAccessor.getenv(CfEnv.VCAP_SERVICES))
					.thenReturn(fileContents);
			return new CfEnv(mockEnvironmentAccessor);
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private CfEnv createTestCfEnv() {
		return createTestCfEnv("vcap-services.json");
	}

}
