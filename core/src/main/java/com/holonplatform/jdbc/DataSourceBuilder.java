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

}
