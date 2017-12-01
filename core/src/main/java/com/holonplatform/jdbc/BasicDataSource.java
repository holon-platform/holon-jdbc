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

import java.io.Closeable;
import java.sql.Driver;
import java.util.Properties;

import javax.sql.DataSource;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.jdbc.internal.DefaultBasicDataSource;

/**
 * Simple standard jdbc {@link DataSource} implementation, using the {@link java.sql.DriverManager} class and returning
 * a new {@link java.sql.Connection} from every {@code getConnection} call.
 * 
 * <p>
 * This DataSource is configured using standard connection properties and provides a fluent builder with methods to set
 * base connection parameters, such as JDBC connection URL, username, password and a specific JDBC Driver. If no
 * specific Driver class is configured, the JDBC DriverManager attempts to select an appropriate driver from the set of
 * registered JDBC drivers.
 * </p>
 * 
 * <p>
 * The DataSource is {@link Closeable} only for API consistency with other DataSource implementations, but the
 * <code>close()</code> method does nothing by now.
 * </p>
 * 
 * <p>
 * <b>NOTE: This DataSource does not pool Connections.</b> Can be useful for test or standalone environments outside
 * J2EE containers.
 * </p>
 * 
 * @since 5.0.4
 */
public interface BasicDataSource extends DataSource, Closeable {

	/**
	 * Get a builder to create and configure a {@link BasicDataSource}.
	 * @return BasicDataSource builder
	 */
	static Builder builder() {
		return new DefaultBasicDataSource.DefaultBuilder();
	}

	/**
	 * {@link BasicDataSource} builder.
	 */
	public interface Builder {

		/**
		 * Set the JDBC connection url
		 * @param url the JDBC connection URL (not null)
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
		 * Set JDBC Driver class name to use.
		 * @param driverClassName the JDBC Driver class name (not null)
		 * @return this
		 * @see #driverClass(Class)
		 */
		Builder driverClassName(String driverClassName);

		/**
		 * Set the JDBC Driver class to use.
		 * @param driverClass the JDBC Driver class (not null)
		 * @return this
		 * @see #driverClassName(String)
		 */
		Builder driverClass(Class<? extends Driver> driverClass);

		/**
		 * Set the {@link DatabasePlatform} from which to obtain the JDBC driver to use.
		 * <p>
		 * This is an alternative to direct driver setting using either {@link #driverClass(Class)} or
		 * {@link #driverClassName(String)}.
		 * </p>
		 * @param database Database platform (not null)
		 * @return this
		 */
		default Builder database(DatabasePlatform database) {
			ObjectUtils.argumentNotNull(database, "DatabasePlatform must be not null");
			return driverClassName(database.getDriverClassName());
		}

		/**
		 * Specify arbitrary connection properties as key/value pairs to be passed to the JDBC Driver.
		 * @param connectionProperties Connection properties
		 * @return this
		 */
		Builder connectionProperties(Properties connectionProperties);

		/**
		 * Build and return the configured {@link BasicDataSource} instance.
		 * @return the {@link BasicDataSource} instance
		 */
		BasicDataSource build();

	}

}
