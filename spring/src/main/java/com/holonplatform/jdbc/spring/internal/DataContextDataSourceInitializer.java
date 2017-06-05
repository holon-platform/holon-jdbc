/*
 * Copyright 2000-2016 Holon TDCN.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.jdbc.spring.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.config.SortedResourcesFactoryBean;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.StringUtils;

import com.holonplatform.core.internal.Logger;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.DatabasePlatform;
import com.holonplatform.jdbc.spring.SpringDataSourceConfigProperties;
import com.holonplatform.spring.internal.SpringLogger;

/**
 * Bean to handle {@link DataSource} initialization by running {@literal datacontextid-schema-*.sql} on
 * {@link PostConstruct} and {@literal datacontextid-data-*.sql} SQL scripts on a
 * {@link DataContextDataSourceInitializedEvent}.
 * 
 * @since 5.0.0
 */
class DataContextDataSourceInitializer implements ApplicationListener<DataContextDataSourceInitializedEvent> {

	private static final Logger LOGGER = SpringLogger.create();

	public static final String BEAN_NAME = DataContextDataSourceInitializer.class.getName();

	private static final String DEFAULT_DATA_CONTEXT_ID = "$!DEFAULT!$";

	@Autowired
	private ApplicationContext applicationContext;

	private final String dataSourceBeanName;
	private final SpringDataSourceConfigProperties configuration;

	private boolean initialized = false;

	public DataContextDataSourceInitializer(String dataSourceBeanName, SpringDataSourceConfigProperties configuration) {
		super();
		this.dataSourceBeanName = dataSourceBeanName;
		this.configuration = configuration;
	}

	@PostConstruct
	public void init() {
		if (!configuration.isInitialize()) {
			LOGGER.debug(() -> "Initialization disabled (not running DDL scripts) for data context id: ["
					+ getDataContextId().orElse("DEFAULT") + "]");
			return;
		}
		DataSource dataSource = null;
		try {
			dataSource = applicationContext.getBean(dataSourceBeanName, DataSource.class);
		} catch (@SuppressWarnings("unused") NoSuchBeanDefinitionException | BeanNotOfRequiredTypeException e) {
			// ignore
		}
		if (dataSource == null) {
			LOGGER.debug(() -> "No DataSource found using bean name " + dataSourceBeanName + ": skip initialization");
			return;
		}
		runSchemaScripts(dataSource);
	}

	private void runSchemaScripts(DataSource dataSource) {
		List<Resource> scripts = getScripts(
				configuration.getConfigPropertyValue(SpringDataSourceConfigProperties.SCHEMA_SCRIPT, null), "schema");
		if (!scripts.isEmpty()) {
			runScripts(scripts, dataSource);
			try {
				this.applicationContext.publishEvent(new DataContextDataSourceInitializedEvent(dataSource,
						getDataContextId().orElse(DEFAULT_DATA_CONTEXT_ID)));
				// The listener might not be registered yet, so don't rely on it.
				if (!this.initialized) {
					runDataScripts(dataSource);
					this.initialized = true;
				}
			} catch (IllegalStateException ex) {
				LOGGER.warn("Could not send event to complete DataSource initialization (" + ex.getMessage() + ")");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(DataContextDataSourceInitializedEvent event) {
		if (!configuration.isInitialize()) {
			LOGGER.debug(() -> "Initialization disabled (not running data scripts) for data context id: ["
					+ getDataContextId().orElse("DEFAULT") + "]");
			return;
		}
		if (!this.initialized) {
			if (getDataContextId().orElse(DEFAULT_DATA_CONTEXT_ID).equals(event.getDataContextId())) {
				runDataScripts((DataSource) event.getSource());
				this.initialized = true;
			}
		}
	}

	private Optional<String> getDataContextId() {
		return configuration.getDataContextId();
	}

	private void runDataScripts(DataSource dataSource) {
		List<Resource> scripts = getScripts(
				configuration.getConfigPropertyValue(SpringDataSourceConfigProperties.DATA_SCRIPT, null), "data");
		runScripts(scripts, dataSource);
	}

	private List<Resource> getScripts(String locations, String fallback) {
		String scriptLocations = locations;
		if (scriptLocations == null) {
			StringBuilder sb = new StringBuilder();

			DatabasePlatform db = configuration.getConfigPropertyValue(DataSourceConfigProperties.PLATFORM, null);
			String platform = (db != null) ? db.name() : null;

			if (getDataContextId().isPresent()) {
				if (platform != null) {
					sb.append("classpath*:" + getDataContextId().get() + "-" + fallback + "-" + platform + ".sql,");
				}
				sb.append("classpath*:" + getDataContextId().get() + "-" + fallback + ".sql");
			} else {
				if (platform != null) {
					sb.append("classpath*:" + fallback + "-" + platform + ".sql,");
				}
				sb.append("classpath*:" + fallback + ".sql");
			}

			scriptLocations = sb.toString();
		}
		return getResources(scriptLocations);
	}

	private List<Resource> getResources(String locations) {
		return getResources(Arrays.asList(StringUtils.commaDelimitedListToStringArray(locations)));
	}

	private List<Resource> getResources(List<String> locations) {
		SortedResourcesFactoryBean factory = new SortedResourcesFactoryBean(this.applicationContext, locations);
		try {
			factory.afterPropertiesSet();
			List<Resource> resources = new ArrayList<>();
			for (Resource resource : factory.getObject()) {
				if (resource.exists()) {
					resources.add(resource);
				}
			}
			return resources;
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to load resources from " + locations, ex);
		}
	}

	private void runScripts(List<Resource> resources, DataSource dataSource) {
		if (resources.isEmpty()) {
			return;
		}
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.setContinueOnError(configuration.isContinueOnError());
		populator.setSeparator(configuration.getConfigPropertyValue(SpringDataSourceConfigProperties.SEPARATOR, ";"));
		String encoding = configuration.getConfigPropertyValue(SpringDataSourceConfigProperties.SQL_SCRIPT_ENCODING,
				null);
		if (encoding != null) {
			populator.setSqlScriptEncoding(encoding);
		}
		for (Resource resource : resources) {
			populator.addScript(resource);
		}
		DatabasePopulatorUtils.execute(populator, dataSource);
	}

}
