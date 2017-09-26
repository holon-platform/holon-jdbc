/*
 * Copyright 2016-2017 Axioma srl.
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
package com.holonplatform.jdbc.spring.boot.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotationMetadata;

import com.holonplatform.core.config.ConfigPropertyProvider;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.Logger.Level;
import com.holonplatform.jdbc.spring.EnableDataSource;
import com.holonplatform.jdbc.spring.SpringDataSourceConfigProperties;
import com.holonplatform.jdbc.spring.boot.DataSourcesAutoConfiguration;
import com.holonplatform.jdbc.spring.internal.DataSourceRegistrar;
import com.holonplatform.spring.EnvironmentConfigPropertyProvider;
import com.holonplatform.spring.internal.BeanRegistryUtils;
import com.holonplatform.spring.internal.PrimaryMode;
import com.holonplatform.spring.internal.SpringLogger;

/**
 * DataSource bean registrar using {@link DataSourcesAutoConfiguration} Spring boot auto-configuration.
 * 
 * @since 5.0.0
 */
public class DataSourcesAutoConfigurationRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = SpringLogger.create();

	/**
	 * Environment
	 */
	private Environment environment;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.EnvironmentAware#setEnvironment(org.springframework.core.env.Environment)
	 */
	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.
	 * core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

		// check configuration properties
		ConfigPropertyProvider configPropertyProvider = EnvironmentConfigPropertyProvider.create(environment);

		Set<String> names = configPropertyProvider.getPropertyNames().filter((n) -> n.startsWith("holon.datasource."))
				.collect(Collectors.toSet());

		if (!names.isEmpty()) {

			// get config properties by data context id

			Map<String, Properties> dataContextProperties = new HashMap<>();

			for (String name : names) {
				if (name != null && !name.trim().equals("")) {
					String dataContextId = getDataContextId(name).orElse(null);
					Properties properties = dataContextProperties.get(dataContextId);
					if (properties == null) {
						properties = new Properties();
						dataContextProperties.put(dataContextId, properties);
					}
					properties.put(name, configPropertyProvider.getProperty(name, String.class));
				}
			}

			if (!dataContextProperties.isEmpty()) {

				if (environment instanceof ConfigurableEnvironment) {
					// adds a property source to provide spring.datasource.initialize with false value to avoid
					// DataSource init from other auto-configuration class
					((ConfigurableEnvironment) environment).getPropertySources()
							.addFirst(new AvoidDataSourceInitializePropertySource());
				}

				for (final String dataContextId : dataContextProperties.keySet()) {
					String dsBeanName = BeanRegistryUtils.buildBeanName(dataContextId,
							EnableDataSource.DEFAULT_DATASOURCE_BEAN_NAME);
					if (!registry.containsBeanDefinition(dsBeanName)) {
						SpringDataSourceConfigProperties configuration = SpringDataSourceConfigProperties
								.builder(dataContextId).withPropertySource(dataContextProperties.get(dataContextId))
								.build();
						// if not already registered, create and register DataSource
						DataSourceRegistrar.registerDataSource(configuration, registry, dataContextId,
								PrimaryMode.AUTO);
					} else {
						if (LOGGER.isEnabled(Level.DEBUG)) {
							if (dataContextId == null || dataContextId.trim().equals("")) {
								LOGGER.debug(() -> "Skipping DataSource auto-configuration: a bean with name ["
										+ dsBeanName + "] is already registered");
							} else {
								LOGGER.debug(() -> "Skipping DataSource auto-configuration for data context id ["
										+ dataContextId + "]: a bean with name [" + dsBeanName
										+ "] is already registered");
							}
						}
					}
				}

			} else {
				LOGGER.debug(
						() -> "No valid holon.datasource.* property detected in environment, skipping DataSource auto configuration.");
			}

		} else {
			LOGGER.debug(
					() -> "No holon.datasource.* property detected in environment, skipping DataSource auto configuration.");
		}
	}

	/**
	 * Get the data context id part from given <code>holon.datasource</code> property name, if available.
	 * @param propertyName Property name
	 * @return Optional data context id
	 */
	private static Optional<String> getDataContextId(String propertyName) {
		String name = propertyName.substring("holon.datasource.".length());
		int idx = name.indexOf('.');
		if (idx > 0) {
			String id = name.substring(0, idx);
			if (id != null && !id.trim().equals("")) {
				return Optional.of(id);
			}
		}
		return Optional.empty();
	}

	/**
	 * Property source to provide spring.datasource.initialize with false value to avoid DataSource init from other
	 * auto-configuration class.
	 */
	static class AvoidDataSourceInitializePropertySource extends PropertySource<Properties> {

		public AvoidDataSourceInitializePropertySource() {
			super("holon_dataSources_autoConfig_propertySource", new Properties());
			getSource().put("spring.datasource.initialize", "false");
		}

		@Override
		public Object getProperty(String name) {
			return getSource().get(name);
		}

	}

}
