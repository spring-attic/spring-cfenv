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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.cfenv.jdbc.CfJdbcUrlCreator;
import org.springframework.cfenv.util.EnvironmentAccessor;

/**
 * @author Mark Pollack
 */
public class CfEnv {

	public static final String VCAP_APPLICATION = "VCAP_APPLICATION";

	public static final String VCAP_SERVICES = "VCAP_SERVICES";

	public final EnvironmentAccessor environmentAccessor;

	/* TODO consider supporting multiple json libraries? */
	private ObjectMapper objectMapper = new ObjectMapper();

	private List<CfService> cfServices = new ArrayList<>();

	public CfEnv() {
		this(new EnvironmentAccessor());
	}

	public CfEnv(EnvironmentAccessor environmentAccessor) {
		this.environmentAccessor = environmentAccessor;
		try {
			String vcapServicesJson = this.environmentAccessor.getenv(VCAP_SERVICES);
			if (vcapServicesJson != null && vcapServicesJson.length() > 0) {
				Map<String, List<Map<String, Object>>> rawServices = this.objectMapper.readValue(vcapServicesJson,
						new TypeReference<Map<String, List<Map<String, Object>>>>() {
						}

				);
				for (Map.Entry<String, List<Map<String, Object>>> entry : rawServices.entrySet()) {
					for (Map<String, Object> serviceData : entry.getValue()) {
						cfServices.add(new CfService(serviceData));
					}
				}
			}
		}
		catch (Exception e) {
			throw new IllegalStateException("Could not access/parse " + VCAP_SERVICES + " environment variable.", e);
		}
	}

	public List<CfService> findAllServices() {
		return cfServices;
	}

	public List<CfService> findServicesByName(String... spec) {
		List<CfService> cfServices = new ArrayList<>();
		for (CfService cfService : this.cfServices) {
			if (spec != null) {
				for (String regex : spec) {
					String name = cfService.getName();
					if (name != null && name.length() > 0) {
						if (name.matches(regex)) {
							cfServices.add(cfService);
						}
					}
				}
			}
		}
		return cfServices;
	}

	public CfService findServiceByName(String... spec) {
		List<CfService> cfServices = findServicesByName(spec);
		if (cfServices.size() > 0) {
			// TODO - shoud throw an exception instead if there is more than one, I believe service
			// name is always unique, but regex can allow for more matches.
			return cfServices.stream().findFirst().get();
		}
		String message = (spec == null) ? "null" : String.join(", ", spec);
		throw new IllegalArgumentException("No service with name [" + message + "] was found");
	}

	public CfService findServiceByLabel(String... spec) {
		for (CfService cfService : this.cfServices) {
			if (spec != null) {
				for (String regex : spec) {
					String name = cfService.getLabel();
					if (name != null && name.length() > 0) {
						if (name.matches(regex)) {
							return cfService;
						}
					}
				}
			}
		}
		String message = (spec == null) ? "null" : String.join(", ", spec);
		throw new IllegalArgumentException("No service with label [" + message + "] was found");
	}

	public CfService findServiceByTag(String... spec) {
		for (CfService cfService : this.cfServices) {
			if (spec != null) {
				for (String regex : spec) {
					List<String> tags = cfService.getTags();
					for (String tag : tags) {
						if (tag != null && tag.length() > 0) {
							if (tag.matches(regex)) {
								return cfService;
							}
						}
					}
				}
			}
		}
		String message = (spec == null) ? "null" : String.join(", ", spec);
		throw new IllegalArgumentException("No service with tag [" + message + "] was found");
	}

	public CfCredentials findCredentialsByName(String... spec) {
		CfService cfService = findServiceByName(spec);
		return cfService.getCredentials();
	}

	public CfCredentials findCredentialsByLabel(String... spec) {
		CfService cfService = findServiceByLabel(spec);
		return cfService.getCredentials();
	}

	public CfCredentials findCredentialsByTag(String... spec) {
		CfService cfService = findServiceByTag(spec);
		return cfService.getCredentials();
	}

	public String findJdbcUrl() {
		CfJdbcUrlCreator cfJdbcEnv = new CfJdbcUrlCreator(this.findAllServices());
		return cfJdbcEnv.getJdbcUrl();
	}

	public String findJdbcUrlByName(String... spec) {
		CfJdbcUrlCreator cfJdbcEnv = new CfJdbcUrlCreator(this.findServicesByName(spec));
		return cfJdbcEnv.getJdbcUrl();
	}

	/**
	 * Checks that the value of the environment variable VCAP_APPLICATION is not null, usually
	 * indicating that this application is running inside of Cloud Foundry
	 * @return {@code true} if the environment variable VCAP_APPLICATION is not null,
	 * {@code false} otherwise.
	 */
	public boolean isInCf() {
		return this.environmentAccessor.getenv(VCAP_APPLICATION) != null;
	}

}
