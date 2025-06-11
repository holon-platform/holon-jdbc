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
package com.holonplatform.jdbc.internal.factory;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import jakarta.annotation.Priority;
import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolConfiguration;

import com.holonplatform.core.config.ConfigPropertySet.ConfigurationException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.DataSourceFactory;
import com.holonplatform.jdbc.DatabasePlatform;
import com.holonplatform.jdbc.internal.DefaultDataSourceBuilderConfiguration;
import com.holonplatform.jdbc.internal.JdbcLogger;

/**
 * A {@link DataSourceFactory} to create Tomcat jdbc pooling DataSources.
 * 
 * <p>
 * Supports Tomcat jdbc specific properties using sub-prefix <code>tomcat</code>, i.e. using the property name pattern
 * <code>holon.datasource.tomcat.SPECIFIC_NAME</code>.
 * </p>
 *
 * @since 5.0.0
 */
@Priority(Integer.MAX_VALUE)
public class TomcatJdbcDataSourceFactory implements DataSourceFactory {

	private static final long serialVersionUID = -9155424231641969517L;

	/**
	 * Logger
	 */
	private static final Logger LOGGER = JdbcLogger.create();

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.jdbc.DataSourceFactory#getDataSourceType()
	 */
	@Override
	public String getDataSourceType() {
		return DataSourceBuilder.TYPE_TOMCAT;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.jdbc.DataSourceFactory#build(com.holonplatform.jdbc.DataSourceConfigProperties)
	 */
	@Override
	public DataSource build(DataSourceConfigProperties configurationProperties) throws ConfigurationException {

		final String dataContextId = configurationProperties.getDataContextId().orElse(null);

		LOGGER.debug(() -> "Building Tomcat DataSource [dataContextId=" + dataContextId + "]");

		final String url = configurationProperties.getConfigPropertyValue(DataSourceConfigProperties.URL, null);
		if (url == null) {
			throw new ConfigurationException(DefaultDataSourceBuilderConfiguration
					.buildMissingJdbcUrlMessage(getDataSourceType(), dataContextId));
		}

		LOGGER.debug(() -> "Tomcat DataSource JDBC connection URL: " + url);

		final Optional<DatabasePlatform> platform = Optional.ofNullable(DatabasePlatform.fromUrl(url));

		LOGGER.debug(
				() -> "Detected Database platform: " + platform.map(p -> p.name()).orElse("[Failed to auto detect"));

		final String driverClass = configurationProperties.getConfigPropertyValue(
				DataSourceConfigProperties.DRIVER_CLASS_NAME,
				platform.map(p -> p.getDriverClassName())
						.orElseThrow(() -> new ConfigurationException(DefaultDataSourceBuilderConfiguration
								.buildMissingDriverClassMessage(getDataSourceType(), dataContextId))));

		LOGGER.debug(() -> "Tomcat DataSource JDBC driver class name: " + driverClass);

		try {
			// specific properties
			final Map<String, String> tomcatProperties = configurationProperties.getSubPropertiesUsingPrefix("tomcat");
			Properties poolProperties = new Properties();
			poolProperties.putAll(tomcatProperties);

			LOGGER.debug(() -> "Tomcat DataSource properties: " + tomcatProperties);

			PoolConfiguration config = org.apache.tomcat.jdbc.pool.DataSourceFactory
					.parsePoolProperties(poolProperties);

			config.setDriverClassName(driverClass);
			config.setUrl(url);

			// name
			String name = configurationProperties.getConfigPropertyValue(DataSourceConfigProperties.NAME, null);
			if (name != null) {
				config.setName(name);
			}

			// validation query
			platform.map(p -> p.getValidationQuery()).ifPresent(vq -> {
				config.setTestOnBorrow(true);
				config.setValidationQuery(vq);
			});

			// credentials
			config.setUsername(
					configurationProperties.getConfigPropertyValue(DataSourceConfigProperties.USERNAME, null));
			config.setPassword(
					configurationProperties.getConfigPropertyValue(DataSourceConfigProperties.PASSWORD, null));

			// pool size
			Integer minPoolSize = configurationProperties
					.getConfigPropertyValue(DataSourceConfigProperties.MIN_POOL_SIZE, null);
			if (minPoolSize != null && minPoolSize > 0) {
				config.setInitialSize(minPoolSize);
				config.setMinIdle(minPoolSize);
			}

			Integer maxPoolSize = configurationProperties.getConfigPropertyValue(
					DataSourceConfigProperties.MAX_POOL_SIZE, DataSourceConfigProperties.DEFAULT_MAX_POOL_SIZE);
			if (maxPoolSize != null && maxPoolSize > 0) {
				config.setMaxActive(maxPoolSize);
				if (config.getMaxIdle() > maxPoolSize) {
					config.setMaxIdle(maxPoolSize);
				}
			}

			// autocommit
			if (configurationProperties.isDisableAutoCommit()) {
				config.setDefaultAutoCommit(Boolean.FALSE);
			}

			final org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource(config);

			LOGGER.debug(() -> "(Data context id: " + dataContextId + "): "
					+ "Tomcat DataSource setted up for jdbc url: " + url + " [Max pool size: " + maxPoolSize + "]");

			return ds;
		} catch (Exception e) {
			throw new ConfigurationException("Failed to configure [" + getDataSourceType() + "] DataSource", e);
		}
	}

}
