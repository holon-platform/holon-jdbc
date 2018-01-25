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
package com.holonplatform.jdbc;

import java.util.ServiceLoader;

import javax.annotation.Priority;
import javax.sql.DataSource;

import com.holonplatform.core.config.ConfigPropertySet.ConfigurationException;
import com.holonplatform.core.internal.utils.ClassUtils;
import com.holonplatform.jdbc.exceptions.DataSourceInitializationException;
import com.holonplatform.jdbc.internal.DefaultDataSourceBuilder;

/**
 * Builder to create and configure {@link DataSource} instances using a {@link DataSourceConfigProperties} configuration
 * properties source.
 * <p>
 * The {@link DataSource} instances are created and configured using a suitable {@link DataSourceFactory}, bound to a
 * symbolic type name which can be specified using the {@link DataSourceConfigProperties#TYPE} property. The
 * {@link DataSourceFactory} which corresponds to a {@link DataSource} type name must be registered to build the
 * DataSource using the {@link #registerFactory(DataSourceFactory)} method. Default factories can be registered using
 * default Java {@link ServiceLoader} extension, through a <code>com.holonplatform.jdbc.DataSourceFactory</code> file
 * under the <code>META-INF/services</code> folder.
 * </p>
 * <p>
 * The builder supports {@link DataSourcePostProcessor} registration to initialiaze and/or configure the created
 * {@link DataSource} instances. Default post processors can be registered using default Java {@link ServiceLoader}
 * extension, through a <code>com.holonplatform.jdbc.DataSourcePostProcessor</code> file under the
 * <code>META-INF/services</code> folder.
 * </p>
 * 
 * @since 5.0.0
 * 
 * @see DataSourceConfigProperties
 */
public interface DataSourceBuilder {

	/**
	 * Default DataSource type: BasicDataSource
	 * @see DataSourceConfigProperties#TYPE
	 */
	public static final String TYPE_BASIC = "com.holonplatform.jdbc.BasicDataSource";

	/**
	 * Default DataSource type: HikariCP DataSource
	 * @see DataSourceConfigProperties#TYPE
	 */
	public static final String TYPE_HIKARICP = "com.zaxxer.hikari.HikariDataSource";

	/**
	 * Default DataSource type: DBCP 2 DataSource
	 * @see DataSourceConfigProperties#TYPE
	 */
	public static final String TYPE_DBCP = "org.apache.commons.dbcp2.BasicDataSource";

	/**
	 * Default DataSource type: Tomcat DataSource
	 * @see DataSourceConfigProperties#TYPE
	 */
	public static final String TYPE_TOMCAT = "org.apache.tomcat.jdbc.pool.DataSource";

	/**
	 * Default DataSource type: JNDI DataSource
	 * @see DataSourceConfigProperties#TYPE
	 */
	public static final String TYPE_JNDI = "JNDI";

	/**
	 * Default {@link DataSourceBuilder} priority if not specified using {@link Priority} annotation.
	 */
	public static final int DEFAULT_PRIORITY = 10000;

	/**
	 * Register a {@link DataSourceFactory} to be used to build {@link DataSource} instances of the type returned by the
	 * {@link DataSourceFactory#getDataSourceType()} method.
	 * <p>
	 * Any previous binding with given type will be replaced by the given {@link DataSourceFactory}.
	 * </p>
	 * @param factory the factory to register (not null)
	 * @see DataSourceConfigProperties#TYPE
	 */
	void registerFactory(DataSourceFactory factory);

	/**
	 * Register a new {@link DataSourcePostProcessor} to initialiaze and/or configure the created {@link DataSource}
	 * instances.
	 * @param postProcessor Post processor to register (not null)
	 */
	void registerPostProcessor(DataSourcePostProcessor postProcessor);

	/**
	 * Build a DataSource instance using given configuration properties.
	 * @param configurationProperties Configuration properties
	 * @return DataSource instance configured according to given configuration properties
	 * @throws ConfigurationException Error configuring DataSource using given configuration properties
	 */
	DataSource build(DataSourceConfigProperties configurationProperties) throws ConfigurationException;

	/**
	 * Create a new {@link DataSourceBuilder} using the default ClassLoader. The builder will be initialized with
	 * default {@link DataSourceFactory}s and {@link DataSourcePostProcessor}s loaded using default Java
	 * {@link ServiceLoader} extensions.
	 * @return A new DataSourceBuilder instance
	 */
	static DataSourceBuilder create() {
		return new DefaultDataSourceBuilder(ClassUtils.getDefaultClassLoader());
	}

	/**
	 * Create a new {@link DataSourceBuilder}. The builder will be initialized with default {@link DataSourceFactory}s
	 * and {@link DataSourcePostProcessor}s loaded using default Java {@link ServiceLoader} extensions.
	 * @param classLoader The ClassLoader to use to initialize the builder (not null)
	 * @return A new DataSourceBuilder instance
	 */
	static DataSourceBuilder create(ClassLoader classLoader) {
		return new DefaultDataSourceBuilder(classLoader);
	}

	// ------- Direct builder

	/**
	 * Get a {@link Builder} to set configuration properties and directly obtain a {@link DataSource} instance.
	 * @return A new {@link DataSource} builder
	 */
	static Builder builder() {
		return new DefaultDataSourceBuilder.DefaultBuilder();
	}

	/**
	 * Direct {@link DataSource} instance builder.
	 */
	public interface Builder {

		/**
		 * Set the DataSource type name.
		 * @param typeName the DataSource type name (not null)
		 * @return this
		 */
		Builder type(String typeName);

		/**
		 * Set the DataSource type.
		 * @param type the DataSource type (not null)
		 * @return this
		 */
		Builder type(DataSourceType type);

		/**
		 * Set the DataSource instance name, if supported by concrete DataSource implementation.
		 * @param name the DataSource name
		 * @return this
		 */
		Builder name(String name);

		/**
		 * Set the JDBC driver class name.
		 * @param driverClassName the JDBC driver class name
		 * @return this
		 */
		Builder driverClassName(String driverClassName);

		/**
		 * Set the JDBC connection URL.
		 * @param url the JDBC connection URL
		 * @return this
		 */
		Builder url(String url);

		/**
		 * Set the JDBC connection username.
		 * @param username the JDBC connection username
		 * @return this
		 */
		Builder username(String username);

		/**
		 * Set the JDBC connection password.
		 * @param password the JDBC connection password
		 * @return this
		 */
		Builder password(String password);

		/**
		 * Set the database platform to which the DataSource is connected.
		 * <p>
		 * Can be used for example to auto-detect a suitable JDBC driver.
		 * </p>
		 * @param databasePlatform the database platform
		 * @return this
		 */
		Builder database(DatabasePlatform databasePlatform);

		/**
		 * Set the DataSource connection auto-commit mode.
		 * @param autoCommit <code>true</code> to enable connection auto-commit, <code>false</code> to disable
		 * @return this
		 */
		Builder autoCommit(boolean autoCommit);

		/**
		 * For connection pooling DataSources, set the lower limit of the connections pool.
		 * @param minPoolSize Minimum connection pool size
		 * @return this
		 */
		Builder minPoolSize(int minPoolSize);

		/**
		 * For connection pooling DataSources, set the upper limit of the connections pool.
		 * @param minPoolSize Maximum connection pool size
		 * @return this
		 */
		Builder maxPoolSize(int maxPoolSize);

		/**
		 * For connection pooling DataSources, set the connection validation query.
		 * @param validationQuery the connection validation query
		 * @return this
		 */
		Builder validationQuery(String validationQuery);

		/**
		 * Add a SQL intitialization script.
		 * <p>
		 * Supported SQL scripts format:
		 * <ul>
		 * <li>The semicolon punctuation mark (<code>;</code>) must be used as SQL statements separator</li>
		 * <li>Single line comments must be prefixed by <code>--</code></li>
		 * <li>Block comments must be delimited by <code>&#92;*</code> and <code>*&#47;</code></li>
		 * </ul>
		 * </p>
		 * @param sqlScript SQL intitialization script (not null)
		 * @return this
		 */
		Builder withInitScript(String sqlScript);

		/**
		 * Add a SQL intitialization script read from given classpath resource name (for example a file name).
		 * {@link ClassLoader#getResourceAsStream(String)} is used to load the resource, using the same conventions to
		 * locate the resource to load. UTF-8 is assumed as default encoding.
		 * <p>
		 * Supported SQL scripts format:
		 * <ul>
		 * <li>The semicolon punctuation mark (<code>;</code>) must be used as SQL statements separator</li>
		 * <li>Single line comments must be prefixed by <code>--</code></li>
		 * <li>Block comments must be delimited by <code>&#92;*</code> and <code>*&#47;</code></li>
		 * </ul>
		 * </p>
		 * @param sqlScriptFile SQL script resource name
		 * @return this
		 */
		Builder withInitScriptResource(String sqlScriptResourceName);

		/**
		 * Build the {@link DataSource}.
		 * @return the {@link DataSource} instance
		 * @throws DataSourceInitializationException If a {@link DataSource} initialization error occurs, for example an
		 *         error in SQL initialization scripts execution
		 */
		DataSource build();

	}

}
