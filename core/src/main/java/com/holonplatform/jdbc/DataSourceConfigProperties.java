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
package com.holonplatform.jdbc;

import java.util.Optional;

import javax.sql.DataSource;

import com.holonplatform.core.config.ConfigProperty;
import com.holonplatform.core.config.ConfigPropertySet;
import com.holonplatform.core.datastore.DataContextBound;
import com.holonplatform.core.internal.config.DefaultConfigPropertySet;

/**
 * A {@link ConfigPropertySet} for {@link DataSource} configuration, using {@link #DEFAULT_NAME} as property prefix.
 * 
 * <p>
 * When DataSourceConfigProperties are used in conjunction with a data context id to discriminate multiple
 * {@link DataSource} configurations, configuration properties must be written using that id as prefix, for example, if
 * data context id is <code>myid</code>: <br>
 * <code>holon.datasource.myid.url=...</code>
 * </p>
 *
 * @since 5.0.0
 */
public interface DataSourceConfigProperties extends ConfigPropertySet, DataContextBound {

	/**
	 * Configuration property set default name
	 */
	static final String DEFAULT_NAME = "holon.datasource";

	/**
	 * Default max pool size for pooling DataSource
	 */
	static final int DEFAULT_MAX_POOL_SIZE = 10;

	/**
	 * DataSource type
	 */
	static final ConfigProperty<String> TYPE = ConfigProperty.create("type", String.class);

	/**
	 * JDBC Driver class name
	 */
	static final ConfigProperty<String> DRIVER_CLASS_NAME = ConfigProperty.create("driver-class-name", String.class);

	/**
	 * JDBC connection url
	 */
	static final ConfigProperty<String> URL = ConfigProperty.create("url", String.class);

	/**
	 * JDBC connection username
	 */
	static final ConfigProperty<String> USERNAME = ConfigProperty.create("username", String.class);

	/**
	 * JDBC connection password
	 */
	static final ConfigProperty<String> PASSWORD = ConfigProperty.create("password", String.class);

	/**
	 * DataSource name
	 */
	static final ConfigProperty<String> NAME = ConfigProperty.create("name", String.class);

	/**
	 * Database platform to use. Must be one of the names enumerated in {@link DatabasePlatform}.
	 * <p>
	 * Auto-detected by default.
	 * </p>
	 */
	static final ConfigProperty<DatabasePlatform> PLATFORM = ConfigProperty.create("platform", DatabasePlatform.class);

	/**
	 * Enable/Disable auto-commit for JDBC driver
	 */
	static final ConfigProperty<Boolean> AUTOCOMMIT = ConfigProperty.create("auto-commit", Boolean.class);

	/**
	 * DataSource min pool size (for pooling DataSource types)
	 */
	static final ConfigProperty<Integer> MIN_POOL_SIZE = ConfigProperty.create("min-pool-size", Integer.class);

	/**
	 * DataSource max pool size (for pooling DataSource types)
	 */
	static final ConfigProperty<Integer> MAX_POOL_SIZE = ConfigProperty.create("max-pool-size", Integer.class);

	/**
	 * Optional connection validation query for pooling DataSources
	 */
	static final ConfigProperty<String> VALIDATION_QUERY = ConfigProperty.create("validation-query", String.class);

	/**
	 * JNDI lookup name. If this property is setted, driver class, url, username and password are ignored.
	 */
	static final ConfigProperty<String> JNDI_NAME = ConfigProperty.create("jndi-name", String.class);

	/**
	 * Gets whether to disable connection auto-commit
	 * @return True to disable connection auto-commit
	 */
	default boolean isDisableAutoCommit() {
		return !getConfigPropertyValue(AUTOCOMMIT, Boolean.TRUE);
	}

	/**
	 * Gets configured {@link DatabasePlatform} or try to detect it from connection url, if available
	 * @return DatabasePlatform
	 */
	default DatabasePlatform getDatabasePlatform() {
		DatabasePlatform database = getConfigPropertyValue(DataSourceConfigProperties.PLATFORM, null);
		if (database == null) {
			// try to get it from connection url
			String url = getConfigPropertyValue(DataSourceConfigProperties.URL, null);
			if (url != null) {
				database = DatabasePlatform.fromUrl(url);
			}
		}
		return database;
	}

	/**
	 * Try to obtain the JDBC Driver class name, either from {@link #DRIVER_CLASS_NAME} property or using the default
	 * driver class name for the configured database platform, if available.
	 * @return Driver class name, empty if not available
	 */
	default Optional<String> getDriverClassName() {
		// from property
		String driverClass = getConfigPropertyValue(DataSourceConfigProperties.DRIVER_CLASS_NAME, null);
		// try to detect from platform
		if (driverClass == null || driverClass.trim().equals("")) {
			DatabasePlatform database = getDatabasePlatform();
			if (database != null) {
				driverClass = database.getDriverClassName();
			}
		}
		return Optional.ofNullable(driverClass);
	}

	/**
	 * Gets connection validation query, if configured or if a default validation query is available
	 * @return Connection validation query, empty if not available
	 */
	default Optional<String> getConnectionValidationQuery() {
		// from property
		String validationQuery = getConfigPropertyValue(DataSourceConfigProperties.VALIDATION_QUERY, null);
		// try to detect from platform
		if (validationQuery == null || validationQuery.trim().equals("")) {
			DatabasePlatform database = getDatabasePlatform();
			if (database != null) {
				validationQuery = database.getValidationQuery();
			}
		}
		return Optional.ofNullable(validationQuery);
	}

	/**
	 * Builder to create property set instances bound to a property data source.
	 * @param dataContextId Optional data context id to which DataSource is bound
	 * @return ConfigPropertySet builder
	 */
	static Builder<DataSourceConfigProperties> builder(String dataContextId) {
		return new DefaultConfigPropertySet.DefaultBuilder<>(new DataSourceConfigPropertiesImpl(dataContextId));
	}

	/**
	 * Builder to create property set instances bound to a property data source, without data context id specification.
	 * @return ConfigPropertySet builder
	 */
	static Builder<DataSourceConfigProperties> builder() {
		return new DefaultConfigPropertySet.DefaultBuilder<>(new DataSourceConfigPropertiesImpl(null));
	}

	/**
	 * Default implementation
	 */
	static class DataSourceConfigPropertiesImpl extends DefaultConfigPropertySet implements DataSourceConfigProperties {

		private final String dataContextId;

		/**
		 * Constructor
		 * @param dataContextId Optional data context id
		 */
		public DataSourceConfigPropertiesImpl(String dataContextId) {
			super((dataContextId != null && !dataContextId.trim().equals("")) ? (DEFAULT_NAME + "." + dataContextId)
					: DEFAULT_NAME);
			this.dataContextId = (dataContextId != null && !dataContextId.trim().equals("")) ? dataContextId : null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.datastore.DataContextBound#getDataContextId()
		 */
		@Override
		public Optional<String> getDataContextId() {
			return Optional.ofNullable(dataContextId);
		}

	}

}
