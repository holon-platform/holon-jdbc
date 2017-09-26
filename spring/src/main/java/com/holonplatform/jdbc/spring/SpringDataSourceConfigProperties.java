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
package com.holonplatform.jdbc.spring;

import com.holonplatform.core.config.ConfigProperty;
import com.holonplatform.core.internal.config.DefaultConfigPropertySet;
import com.holonplatform.jdbc.DataSourceConfigProperties;

/**
 * {@link DataSourceConfigProperties} extension with additional Spring-related DataSource configuration properties.
 *
 * @since 5.0.0
 */
public interface SpringDataSourceConfigProperties extends DataSourceConfigProperties {

	/**
	 * Marks DataSource as primary in multiple data sources environments.
	 */
	static final ConfigProperty<Boolean> PRIMARY = ConfigProperty.create("primary", Boolean.class);

	/**
	 * Populate the database after DataSource initialization using <code>datacontextid-data-*.sql</code> scripts.
	 */
	static final ConfigProperty<Boolean> INITIALIZE = ConfigProperty.create("initialize", Boolean.class);

	/**
	 * Schema (DDL) script resource reference
	 */
	static final ConfigProperty<String> SCHEMA_SCRIPT = ConfigProperty.create("schema", String.class);

	/**
	 * Data (DML) script resource reference
	 */
	static final ConfigProperty<String> DATA_SCRIPT = ConfigProperty.create("data", String.class);

	/**
	 * Do not stop if an error occurs while initializing the database (default is false)
	 */
	static final ConfigProperty<Boolean> CONTINUE_ON_ERROR = ConfigProperty.create("continue-on-error", Boolean.class);

	/**
	 * Statement separator in SQL initialization scripts. Default is <code>;</code>.
	 */
	static final ConfigProperty<String> SEPARATOR = ConfigProperty.create("separator", String.class);

	/**
	 * SQL scripts encoding.
	 */
	static final ConfigProperty<String> SQL_SCRIPT_ENCODING = ConfigProperty.create("sql-script-encoding",
			String.class);

	/**
	 * Gets whether the DataSource should be marked as primary
	 * @return True if primary
	 */
	default boolean isPrimary() {
		return getConfigPropertyValue(PRIMARY, Boolean.FALSE);
	}

	/**
	 * Gets whether the DataSource should initialize the database using data scripts
	 * @return True if initialize
	 */
	default boolean isInitialize() {
		return getConfigPropertyValue(INITIALIZE, Boolean.TRUE);
	}

	/**
	 * Gets whether to stop or not if an error occurs while initializing the database.
	 * @return True to not stop
	 */
	default boolean isContinueOnError() {
		return getConfigPropertyValue(CONTINUE_ON_ERROR, Boolean.FALSE);
	}

	/**
	 * Builder to create property set instances bound to a property data source.
	 * @param dataContextId Optional data context id to which DataSource is bound
	 * @return ConfigPropertySet builder
	 */
	static Builder<SpringDataSourceConfigProperties> builder(String dataContextId) {
		return new DefaultConfigPropertySet.DefaultBuilder<>(new SpringDataSourceConfigPropertiesImpl(dataContextId));
	}

	/**
	 * Builder to create property set instances bound to a property data source, without data context id specification.
	 * @return ConfigPropertySet builder
	 */
	static Builder<SpringDataSourceConfigProperties> builder() {
		return new DefaultConfigPropertySet.DefaultBuilder<>(new SpringDataSourceConfigPropertiesImpl(null));
	}

	/**
	 * Default implementation
	 */
	static class SpringDataSourceConfigPropertiesImpl extends DataSourceConfigPropertiesImpl
			implements SpringDataSourceConfigProperties {

		/**
		 * Constructor
		 * @param dataContextId Optional data context id
		 */
		public SpringDataSourceConfigPropertiesImpl(String dataContextId) {
			super(dataContextId);
		}

	}

}
