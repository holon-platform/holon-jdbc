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
package com.holonplatform.jdbc.spring.boot;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.spring.boot.internal.DataSourcesAutoConfigurationRegistrar;

/**
 * Spring boot auto-configuration to register {@link DataSource} beans using <code>holon.datasource.*</code>
 * configuration properties.
 * 
 * <p>
 * This auto-configuration supports multiple DataSource registration using different data context id names. The data
 * context id of every DataSource to register must be specified right after <code>holon.datasource.</code> and before
 * {@link DataSourceConfigProperties} configuration properties. For example, to register two DataSources, bound to
 * <code>one</code> and <code>two</code> data context ids: <br>
 * <code>holon.datasource.one.url=...</code> <code>holon.datasource.two.url=...</code>
 * </p>
 * 
 * <p>
 * To every registered {@link DataSource} bean is assigned a qualifier with the same name of the DataSource data context
 * id.
 * </p>
 * 
 * <p>
 * The actual {@link DataSource} implementation to be used can be specified using a symbolic type name, specified
 * through the {@link DataSourceConfigProperties#TYPE} configuration property. By default, the following types are
 * supported:
 * <ul>
 * <li><code>com.holonplatform.jdbc.BasicDataSource</code>: Create <code>BasicDataSource</code> instances, to be used
 * typically for testing purposes. It is a simple DataSource implementation, using the {@link java.sql.DriverManager}
 * class and returning a new {@link java.sql.Connection} from every <code>getConnection</code> call.</li>
 * <li><code>com.zaxxer.hikari.HikariDataSource</code>: Create HikariCP connection pooling DataSource instances. The
 * HikariCP library dependency must be available in classpath. All default configuration properties are supported, and
 * additional Hikari-specific configuration properties can be specified using the <code>hikari</code> prefix before the
 * actual property name, for example: <code>holon.datasource.hikari.connectionTimeout=50000</code></li>
 * <li><code>org.apache.commons.dbcp2.BasicDataSource</code>: Create DBCP2 connection pooling DataSource instances. The
 * Apache Commons DBCP 2 library dependency must be available in classpath. All default configuration properties are
 * supported, and additional DBCP-specific configuration properties can be specified using the <code>dbcp</code> prefix
 * before the actual property name, for example: <code>holon.datasource.dbcp.maxWaitMillis=3000</code></li>
 * <li><code>org.apache.tomcat.jdbc.pool.DataSource</code>: Create Tomcat JDBC connection pooling DataSource instances.
 * The tomcat-jdbc library dependency must be available in classpath. All default configuration properties are
 * supported, and additional Tomcat-specific configuration properties can be specified using the <code>tomcat</code>
 * prefix before the actual property name, for example: <code>holon.datasource.tomcat.maxAge=5000</code></li>
 * <li><code>JNDI</code>: Obtain a DataSource using JNDI. The <code>jndi-name</code> configuration property is required
 * to specify the JNDI name to which the DataSource is bound in the JNDI context.</li>
 * </ul>
 * 
 * <p>
 * When the {@link DataSourceConfigProperties#TYPE} configuration property is not specified, the default DataSource type
 * selection strategy is defined as follows:
 * <ol>
 * <li>If the HikariCP dependecy is present in classpath, the <code>com.zaxxer.hikari.HikariDataSource</code> type will
 * be used;</li>
 * <li>If the Apache Commons DBCP2 dependecy is present in classpath, the
 * <code>org.apache.commons.dbcp2.BasicDataSource</code> type will be used;</li>
 * <li>If the Tomcat JDBC dependecy is present in classpath, the <code>org.apache.tomcat.jdbc.pool.DataSource</code>
 * type will be used;</li>
 * <li>Otherwise, the <code>com.holonplatform.jdbc.BasicDataSource</code> type is used as fallback</li>
 * </ol>
 * 
 * @since 5.0.0
 */
@Configuration
@ConditionalOnClass(DataSource.class)
@AutoConfigureBefore({ DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class })
@Import(DataSourcesAutoConfigurationRegistrar.class)
public class DataSourcesAutoConfiguration {

}
