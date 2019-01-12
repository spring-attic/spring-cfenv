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
package org.springframework.cfenv.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jens Deppe
 */
public class UriInfoTests {

	@Test
	public void createUri() {
		String uri = "mysql://joe:joes_password@localhost:1527/big_db";
		UriInfo result = new UriInfo(uri);

		assertUriInfoEquals(result, "localhost", 1527, "joe", "joes_password", "big_db", null);
		assertThat(uri).isEqualTo(result.getUriString());
	}

	@Test
	public void createUriWithQuery() {
		String uri = "mysql://joe:joes_password@localhost:1527/big_db?p1=v1&p2=v2";
		UriInfo result = new UriInfo(uri);

		assertUriInfoEquals(result, "localhost", 1527, "joe", "joes_password", "big_db", "p1=v1&p2=v2");
		assertThat(uri).isEqualTo(result.getUriString());
	}

	@Test
	public void createNoUsernamePassword() {
		String uri = "mysql://localhost:1527/big_db";
		UriInfo result = new UriInfo(uri);

		assertUriInfoEquals(result, "localhost", 1527, null, null, "big_db", null);
		assertThat(uri).isEqualTo(result.getUriString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void createWithUsernameNoPassword() {
		String uri = "mysql://joe@localhost:1527/big_db";
		new UriInfo(uri);
	}

	@Test
	public void createWithExplicitParameters() {
		String uri = "mysql://joe:joes_password@localhost:1527/big_db";
		UriInfo result = new UriInfo("mysql", "localhost", 1527, "joe", "joes_password", "big_db");

		assertUriInfoEquals(result, "localhost", 1527, "joe", "joes_password", "big_db", null);
		assertThat(uri).isEqualTo(result.getUriString());
	}

	private void assertUriInfoEquals(UriInfo result, String host, int port,
			String username, String password, String path, String query) {
		assertThat(host).isEqualTo(result.getHost());
		assertThat(port).isEqualTo(result.getPort());
		assertThat(username).isEqualTo(result.getUsername());
		assertThat(password).isEqualTo(result.getPassword());
		assertThat(path).isEqualTo(result.getPath());
		assertThat(query).isEqualTo(result.getQuery());
	}
}
