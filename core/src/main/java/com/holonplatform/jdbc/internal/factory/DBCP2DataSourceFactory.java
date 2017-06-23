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
package com.holonplatform.jdbc.internal.factory;

import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;

import com.holonplatform.core.config.ConfigPropertySet.ConfigurationException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.DataSourceFactory;
import com.holonplatform.jdbc.internal.DefaultDataSourceBuilderConfiguration;
import com.holonplatform.jdbc.internal.JdbcLogger;

/**
 * A {@link DataSourceFactory} to create Apache commons DBCP 2 pooling DataSources.
 * 
 * <p>
 * Supports DBCP specific properties using sub-prefix <code>dbcp</code>, i.e. using the property name pattern
 * <code>holon.datasource.dbcp.SPECIFIC_NAME</code>.
 * </p>
 *
 * @since 5.0.0
 */
public class DBCP2DataSourceFactory implements DataSourceFactory {

	private static final long serialVersionUID = 2130277599698672774L;

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
		return DataSourceBuilder.TYPE_DBCP;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.jdbc.DataSourceFactory#build(com.holonplatform.jdbc.DataSourceConfigProperties)
	 */
	@Override
	public DataSource build(DataSourceConfigProperties configurationProperties) throws ConfigurationException {

		final String dataContextId = configurationProperties.getDataContextId().orElse(null);

		final String url = configurationProperties.getConfigPropertyValue(DataSourceConfigProperties.URL, null);
		if (url == null) {
			throw new ConfigurationException(DefaultDataSourceBuilderConfiguration
					.buildMissingJdbcUrlMessage(getDataSourceType(), dataContextId));
		}

		final String driverClass = configurationProperties.getConfigPropertyValue(
				DataSourceConfigProperties.DRIVER_CLASS_NAME,
				configurationProperties.getDriverClassName()
						.orElseThrow(() -> new ConfigurationException(DefaultDataSourceBuilderConfiguration
								.buildMissingDriverClassMessage(getDataSourceType(), dataContextId))));

		try {
			// specific properties
			Map<String, String> dbcpProperties = configurationProperties.getSubPropertiesUsingPrefix("dbcp");
			Properties poolProperties = new Properties();
			poolProperties.putAll(dbcpProperties);

			BasicDataSource ds = BasicDataSourceFactory.createDataSource(poolProperties);

			ds.setDriverClassName(driverClass);
			ds.setUrl(url);

			// validation query
			configurationProperties.getConnectionValidationQuery().ifPresent(vq -> {
				ds.setTestOnBorrow(true);
				ds.setValidationQuery(vq);
			});

			// credentials
			ds.setUsername(configurationProperties.getConfigPropertyValue(DataSourceConfigProperties.USERNAME, null));
			ds.setPassword(configurationProperties.getConfigPropertyValue(DataSourceConfigProperties.PASSWORD, null));

			// pool size
			Integer minPoolSize = configurationProperties
					.getConfigPropertyValue(DataSourceConfigProperties.MIN_POOL_SIZE, null);
			if (minPoolSize != null && minPoolSize > 0) {
				ds.setInitialSize(minPoolSize);
				ds.setMinIdle(minPoolSize);
			}

			Integer maxPoolSize = configurationProperties.getConfigPropertyValue(
					DataSourceConfigProperties.MAX_POOL_SIZE, DataSourceConfigProperties.DEFAULT_MAX_POOL_SIZE);
			if (maxPoolSize != null && maxPoolSize > 0) {
				ds.setMaxTotal(maxPoolSize);
				if (ds.getMaxIdle() > maxPoolSize) {
					ds.setMaxIdle(maxPoolSize);
				}
			}

			// autocommit
			if (configurationProperties.isDisableAutoCommit()) {
				ds.setDefaultAutoCommit(Boolean.FALSE);
			}

			LOGGER.debug(() -> "(Data context id: " + dataContextId + "): "
					+ "DBCP2 DataSource setted up for jdbc url: " + url + " [Max pool size: " + maxPoolSize + "]");

			return ds;
		} catch (Exception e) {
			throw new ConfigurationException("Failed to configure [" + getDataSourceType() + "] DataSource", e);
		}
	}

}
