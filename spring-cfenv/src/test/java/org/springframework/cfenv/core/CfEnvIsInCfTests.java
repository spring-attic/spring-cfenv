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
package org.springframework.cfenv.core;

import java.util.Map;

import mockit.Mock;
import mockit.MockUp;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Pollack
 */
public class CfEnvIsInCfTests {

	@Test
	public void testIsInCf() {
		mockVcapApplication();
		CfEnv cfEnv = new CfEnv();
		assertThat(cfEnv.isInCf()).isTrue();
	}

	@Test
	public void testIsInCfFalse() {
		CfEnv cfEnv = new CfEnv();
		assertThat(cfEnv.isInCf()).isFalse();
	}

	private void mockVcapApplication() {
		Map<String, String> env = System.getenv();
		new MockUp<System>() {
			@Mock
			public String getenv(String name) {
				if (name.equalsIgnoreCase("VCAP_APPLICATION")) {
					return "{\"instance_id\":\"123\"";
				}
				return env.get(name);
			}
		};

	}

}
