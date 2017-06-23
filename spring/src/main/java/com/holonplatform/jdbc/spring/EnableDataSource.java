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
package com.holonplatform.jdbc.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.spring.internal.DataSourceRegistrar;
import com.holonplatform.spring.internal.PrimaryMode;

/**
 * Annotation to be used on Spring Configuration classes to setup a {@link DataSource} using external configuration
 * properties according to {@link DataSourceConfigProperties} property names.
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
 * <p>
 * A data context id can be specified using {@link #dataContextId()}, to discriminate configuration properties using
 * given id as property prefix, and setting data context id as Spring bean qualifier to allow bean injection when
 * multiple {@link DataSource} bean instances are registered in context, for example using {@link Qualifier} annotation.
 * </p>
 * 
 * <p>
 * When a data context id is specified, configuration properties must be written using that id as prefix, for example,
 * if data context id is <code>myid</code>: <br>
 * <code>holon.datasource.myid.url=...</code>
 * </p>
 * 
 * <p>
 * The DataSource bean is registered using {@link #DEFAULT_DATASOURCE_BEAN_NAME} as bean name if no data context id is
 * specified. Otherwise, bean name is composed using {@link #DEFAULT_DATASOURCE_BEAN_NAME}, an underscore character and
 * the data context id String, for example: <code>dataSource_mydatacontextid</code>.
 * </p>
 * 
 * <p>
 * In case of multiple DataSources, {@link #primary()} or the external property
 * {@link SpringDataSourceConfigProperties#PRIMARY} can be used to mark one of the DataSources as primary candidate for
 * dependency injection when a qualifier is not specified.
 * </p>
 * 
 * @since 5.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(DataSourceRegistrar.class)
public @interface EnableDataSource {

	/**
	 * Default {@link DataSource} registration bean name.
	 */
	public static final String DEFAULT_DATASOURCE_BEAN_NAME = "dataSource";

	/**
	 * Default {@link PlatformTransactionManager} registration bean name.
	 */
	public static final String DEFAULT_TRANSACTIONMANAGER_BEAN_NAME = "transactionManager";

	/**
	 * Bind DataSource to given data context id.
	 * <p>
	 * The data context id will be used as DataSource bean qualifier, allowing DataSources bean discrimination in case
	 * of multiple data context ids. You must ensure different data context ids are used when configuring multiple data
	 * sources using this annotation.
	 * </p>
	 * @return Data context id
	 */
	String dataContextId() default "";

	/**
	 * Whether to qualify {@link DataSource} bean as <code>primary</code>, i.e. the preferential bean to be injected in
	 * a single-valued dependency when multiple candidates are present.
	 * <p>
	 * When mode is {@link PrimaryMode#AUTO}, the registred DataSource bean is marked as primary or not according to the
	 * {@link SpringDataSourceConfigProperties#PRIMARY} configuration property, if present. If the property is not
	 * specified, the bean is not registered as primary by default.
	 * </p>
	 * @return Primary mode, defaults to {@link PrimaryMode#AUTO}
	 */
	PrimaryMode primary() default PrimaryMode.AUTO;

	/**
	 * Whether to register a JDBC {@link PlatformTransactionManager} to enable transactions management using Spring's
	 * transaction infrastructure, for example to enable {@link Transactional} annotations.
	 * <p>
	 * Default is <code>false</code>.
	 * </p>
	 * @return <code>true</code> to register a {@link PlatformTransactionManager}, <code>false</code> otherwise.
	 */
	boolean enableTransactionManager() default false;

}
