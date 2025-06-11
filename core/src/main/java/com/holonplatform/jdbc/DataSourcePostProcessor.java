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

import java.io.Serializable;
import java.util.ServiceLoader;

import jakarta.annotation.Priority;
import javax.sql.DataSource;

import com.holonplatform.core.config.ConfigPropertySet.ConfigurationException;

/**
 * Interface which can be used to perform additional initialization and configuration on a {@link DataSource} instance
 * created by a {@link DataSourceFactory}.
 * <p>
 * Default {@link DataSourcePostProcessor} implementations can be registered using default Java {@link ServiceLoader}
 * extension, through a <code>com.holonplatform.jdbc.DataSourcePostProcessor</code> file under the
 * <code>META-INF/services</code> folder.
 * </p>
 * <p>
 * The {@link Priority} annotation on {@link DataSourcePostProcessor} class (where less priority value means higher
 * priority order) can be used to give a post processors execution order.
 * </p>
 * 
 * @since 5.0.0
 *
 * @see DataSourceFactory
 * @see DataSourceBuilder
 */
public interface DataSourcePostProcessor extends Serializable {

	/**
	 * Post process a {@link DataSource} instance.
	 * @param dataSource DataSource instance
	 * @param typeName DataSource type name
	 * @param configurationProperties DataSource configuration properties
	 * @throws ConfigurationException If an error occurred
	 */
	void postProcessDataSource(DataSource dataSource, String typeName,
			DataSourceConfigProperties configurationProperties) throws ConfigurationException;

}
