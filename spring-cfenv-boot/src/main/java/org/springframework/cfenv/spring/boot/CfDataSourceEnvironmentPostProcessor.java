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

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.cfenv.jdbc.CfEnvJdbc;
import org.springframework.cfenv.jdbc.CfJdbcService;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

/**
 * @author Mark Pollack
 */
public class CfDataSourceEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered,
		ApplicationListener<ApplicationEvent> {

	private static DeferredLog DEFERRED_LOG = new DeferredLog();

	private static int invocationCount;

	// Before ConfigFileApplicationListener so values there can use these ones
	private int order = ConfigFileApplicationListener.DEFAULT_ORDER - 1;


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
		increaseInvocationCount();
		if (CloudPlatform.CLOUD_FOUNDRY.isActive(environment)) {
			CfEnvJdbc cfEnvJdbc = new CfEnvJdbc();
			CfJdbcService cfJdbcService;
			try {
				cfJdbcService = cfEnvJdbc.findJdbcService();
			}
			catch (Exception e) {
				if (invocationCount == 1) {
					DEFERRED_LOG.debug("Skipping execution of CfDataSourceEnvironmentPostProcessor. " + e.getMessage());
				}
				return;
			}
			if (cfJdbcService != null) {
				Map<String, Object> properties = new LinkedHashMap<>();
				properties.put("spring.datasource.url", cfJdbcService.getJdbcUrl());
				properties.put("spring.datasource.username", cfJdbcService.getJdbcUsername());
				properties.put("spring.datasource.password", cfJdbcService.getJdbcPassword());
				properties.put("spring.datasource.driver-class-name", cfJdbcService.getDriverClassName());

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
				if (invocationCount == 1) {
					DEFERRED_LOG.info("Setting spring.datasource.url property from bound service ["
							+ cfJdbcService.getName() + "]");
				}
			}
		}
		else {
			DEFERRED_LOG.debug("Not setting spring.datasource.url, not in Cloud Foundry Environment");
		}
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationPreparedEvent) {
			this.DEFERRED_LOG.switchTo(CfDataSourceEnvironmentPostProcessor.class);
		}

	}

	/**
	 * EnvironmentPostProcessors can end up getting called twice due to spring-cloud-commons functionality
	 */
	private void increaseInvocationCount() {
		synchronized (this) {
			invocationCount++;
		}
	}
}
