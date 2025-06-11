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

import java.util.Optional;

import jakarta.annotation.Priority;
import javax.sql.DataSource;

import com.holonplatform.core.config.ConfigPropertySet.ConfigurationException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.jdbc.BasicDataSource;
import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.DataSourceFactory;
import com.holonplatform.jdbc.DatabasePlatform;
import com.holonplatform.jdbc.internal.DefaultBasicDataSource;
import com.holonplatform.jdbc.internal.DefaultDataSourceBuilderConfiguration;
import com.holonplatform.jdbc.internal.JdbcLogger;

/**
 * A {@link DataSourceFactory} to create {@link DefaultBasicDataSource} instances.
 *
 * @since 5.0.0
 */
@Priority(Integer.MAX_VALUE)
public class BasicDataSourceFactory implements DataSourceFactory {

	private static final long serialVersionUID = 4048019642491719115L;

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
		return DataSourceBuilder.TYPE_BASIC;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.jdbc.DataSourceFactory#build(com.holonplatform.jdbc.DataSourceConfigProperties)
	 */
	@Override
	public DataSource build(DataSourceConfigProperties configurationProperties) throws ConfigurationException {

		final String dataContextId = configurationProperties.getDataContextId().orElse(null);

		LOGGER.debug(() -> "Building Basic DataSource [dataContextId=" + dataContextId + "]");

		final String url = configurationProperties.getConfigPropertyValue(DataSourceConfigProperties.URL, null);
		if (url == null) {
			throw new ConfigurationException(DefaultDataSourceBuilderConfiguration
					.buildMissingJdbcUrlMessage(getDataSourceType(), dataContextId));
		}

		LOGGER.debug(() -> "Basic DataSource JDBC connection URL: " + url);

		final Optional<DatabasePlatform> platform = Optional.ofNullable(DatabasePlatform.fromUrl(url));

		LOGGER.debug(
				() -> "Detected Database platform: " + platform.map(p -> p.name()).orElse("[Failed to auto detect"));

		final String driverClassName = configurationProperties.getConfigPropertyValue(
				DataSourceConfigProperties.DRIVER_CLASS_NAME,
				platform.map(p -> p.getDriverClassName())
						.orElseThrow(() -> new ConfigurationException("Cannot auto detect JDBC driver class to use, "
								+ "please specify driver class name using config property "
								+ DataSourceConfigProperties.DRIVER_CLASS_NAME.getKey() + " (Data context: "
								+ dataContextId + ")")));

		LOGGER.debug(() -> "Basic DataSource JDBC driver class name: " + driverClassName);

		BasicDataSource ds = BasicDataSource.builder().url(url).driverClassName(driverClassName)
				.username(configurationProperties.getConfigPropertyValue(DataSourceConfigProperties.USERNAME, null))
				.password(configurationProperties.getConfigPropertyValue(DataSourceConfigProperties.PASSWORD, null))
				.build();

		LOGGER.debug(
				() -> "(Data context id: " + dataContextId + "): " + "BasicDataSource setted up for jdbc url: " + url);

		return ds;
	}

}
