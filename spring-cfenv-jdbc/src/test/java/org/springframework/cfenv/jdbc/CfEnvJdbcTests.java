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

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;

import org.springframework.cfenv.core.CfEnv;
import org.springframework.util.ResourceUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Mark Pollack
 */
public class CfEnvJdbcTests extends AbstractTestSupport {

	private static final String mysqlJdbcUrl = "jdbc:mysql://10.0.4.35:3306/cf_2e23d10a_8738_8c3c_66cf_13e44422698c?user=mysql_username&password=mysql_password";

	@Test
	public void testCfService() {
		CfEnvJdbc cfEnvJdbc = createTestCfEnvJdbc();
		assertThat(cfEnvJdbc.findJdbcUrl()).isEqualTo(mysqlJdbcUrl);
		assertThat(cfEnvJdbc.findJdbcUrlByName("mysql")).isEqualTo(mysqlJdbcUrl);
		assertThat(cfEnvJdbc.findJdbcUrlByName("blah")).isNull();
		assertThat(cfEnvJdbc.findJdbcUrlByName((String[]) null)).isNull();
	}

	private CfEnvJdbc createTestCfEnvJdbc() {
		File file = null;
		try {
			file = ResourceUtils.getFile("classpath:vcap-services-jdbc.json");

			String fileContents = new String(Files.readAllBytes(file.toPath()));
			when(mockEnvironmentAccessor.getenv(CfEnv.VCAP_SERVICES))
					.thenReturn(fileContents);
			return new CfEnvJdbc(mockEnvironmentAccessor);
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
