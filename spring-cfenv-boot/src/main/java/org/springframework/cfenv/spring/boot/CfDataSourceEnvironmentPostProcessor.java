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
package org.springframework.cfenv.spring.boot;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.cfenv.core.CfEnv;
import org.springframework.core.Ordered;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

/**
 * @author Mark Pollack
 */
public class CfDataSourceEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	private static final Log logger = LogFactory
			.getLog(CfDataSourceEnvironmentPostProcessor.class);

	// Before ConfigFileApplicationListener so values there can use these ones
	private int order = ConfigFileApplicationListener.DEFAULT_ORDER - 1;

	public CfDataSourceEnvironmentPostProcessor() {
		System.out.println("Created CfDataSourceEnvironmentPostProcessor");
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application) {
		if (CloudPlatform.CLOUD_FOUNDRY.isActive(environment)) {
			CfEnv cfEnv = new CfEnv();
			// TODO detect if more than one db service is bound
			String jdbcUrl = cfEnv.findJdbcUrl();
			Map<String, Object> properties = new HashMap<>();
			System.out.println("Setting spring.datasource.url to " + jdbcUrl);
			logger.info("Setting spring.datasource.url to " + jdbcUrl);
			properties.put("spring.datasource.url", jdbcUrl);
			MutablePropertySources propertySources = environment.getPropertySources();
			if (propertySources.contains(
					CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME)) {
				propertySources.addAfter(
						CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME,
						new MapPropertySource("cfenvjdbc", properties));
			}
			else {
				propertySources
						.addFirst(new MapPropertySource("cfenvjdbc", properties));
			}
		}
		else {
			System.out.println("Not in Cloud Foundry Environment");
		}
	}
}
