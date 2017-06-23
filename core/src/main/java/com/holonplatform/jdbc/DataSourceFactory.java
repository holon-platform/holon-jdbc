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

import java.io.Serializable;
import java.util.ServiceLoader;

import javax.annotation.Priority;
import javax.sql.DataSource;

import com.holonplatform.core.config.ConfigPropertySet.ConfigurationException;

/**
 * Factory interface to create a {@link DataSource} instance according to the declared {@link #getDataSourceType()} type
 * name and using a {@link DataSourceConfigProperties} property set.
 * <p>
 * The DataSource type name corresponds to the one specified through the property
 * {@link DataSourceConfigProperties#TYPE}.
 * </p>
 * <p>
 * The {@link DataSourceFactory} implementations are used by {@link DataSourceBuilder} to create {@link DataSource}
 * instances and can be registered using default Java {@link ServiceLoader} extension, through a
 * <code>com.holonplatform.jdbc.DataSourceFactory</code> file under the <code>META-INF/services</code> folder.
 * </p>
 * <p>
 * The {@link Priority} annotation on {@link DataSourceFactory} class (where less priority value means higher priority
 * order) can be used to order the factories.
 * </p>
 * 
 * @since 5.0.0
 * 
 * @see DataSourceBuilder
 */
public interface DataSourceFactory extends Serializable {

	/**
	 * Get the symbolic DataSource type name to which this factory is bound
	 * @return DataSource type name
	 */
	String getDataSourceType();

	/**
	 * Build a DataSource instance using given configuration properties.
	 * @param configurationProperties Configuration properties
	 * @return DataSource instance configured according to given configuration properties
	 * @throws ConfigurationException If an error occurred
	 */
	DataSource build(DataSourceConfigProperties configurationProperties) throws ConfigurationException;

}
