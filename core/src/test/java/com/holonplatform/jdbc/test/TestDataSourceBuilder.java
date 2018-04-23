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
package com.holonplatform.jdbc.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.Test;

import com.holonplatform.core.internal.utils.ClassUtils;
import com.holonplatform.core.internal.utils.TestUtils;
import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.DatabasePlatform;
import com.holonplatform.jdbc.internal.DefaultBasicDataSource;
import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;

public class TestDataSourceBuilder {

	@Test
	public void testBase() {
		TestUtils.checkEnum(DatabasePlatform.class);

		assertNull(DatabasePlatform.fromUrl(null));

		DatabasePlatform p = DatabasePlatform.fromUrl("jdbc:h2:mem:testdb");
		assertNotNull(p);
		assertNotNull(p.getDriverClassName());
		assertNotNull(p.getXaDriverClassName());
	}

	@Test
	public void testDefaultType() {
		Properties props = new Properties();
		props.put(DataSourceConfigProperties.DEFAULT_NAME + "." + DataSourceConfigProperties.URL.getKey(),
				"jdbc:h2:mem:testdbx0");
		props.put(DataSourceConfigProperties.DEFAULT_NAME + "." + DataSourceConfigProperties.USERNAME.getKey(), "sa");
		props.put(DataSourceConfigProperties.DEFAULT_NAME + "." + DataSourceConfigProperties.PASSWORD.getKey(), "");

		DataSource ds = DataSourceBuilder.create()
				.build(DataSourceConfigProperties.builder().withPropertySource(props).build());
		assertEquals(HikariDataSource.class, ds.getClass());

	}

	@Test
	public void testExplicitType() {
		Properties props = new Properties();
		props.put(DataSourceConfigProperties.DEFAULT_NAME + "." + DataSourceConfigProperties.URL.getKey(),
				"jdbc:h2:mem:testdbx1");
		props.put(DataSourceConfigProperties.DEFAULT_NAME + "." + DataSourceConfigProperties.USERNAME.getKey(), "sa");
		props.put(DataSourceConfigProperties.DEFAULT_NAME + "." + DataSourceConfigProperties.PASSWORD.getKey(), "");
		props.put(DataSourceConfigProperties.DEFAULT_NAME + "." + DataSourceConfigProperties.TYPE.getKey(),
				DataSourceBuilder.TYPE_BASIC);

		DataSource ds = DataSourceBuilder.create()
				.build(DataSourceConfigProperties.builder().withPropertySource(props).build());
		assertEquals(DefaultBasicDataSource.class, ds.getClass());

		props = new Properties();
		props.put(DataSourceConfigProperties.DEFAULT_NAME + "." + DataSourceConfigProperties.URL.getKey(),
				"jdbc:h2:mem:testdbx2");
		props.put(DataSourceConfigProperties.DEFAULT_NAME + "." + DataSourceConfigProperties.USERNAME.getKey(), "sa");
		props.put(DataSourceConfigProperties.DEFAULT_NAME + "." + DataSourceConfigProperties.PASSWORD.getKey(), "");
		props.put(DataSourceConfigProperties.DEFAULT_NAME + "." + DataSourceConfigProperties.TYPE.getKey(),
				DataSourceBuilder.TYPE_TOMCAT);

		ds = DataSourceBuilder.create().build(DataSourceConfigProperties.builder().withPropertySource(props).build());
		assertEquals(org.apache.tomcat.jdbc.pool.DataSource.class, ds.getClass());
	}

	@Test
	public void testDefaultTypeNoContextId() {
		DataSource ds = DataSourceBuilder.create()
				.build(DataSourceConfigProperties.builder().withPropertySource("test_build.properties").build());
		assertNotNull(ds);
	}

	@Test
	public void testBasicType() throws SQLException {
		DataSource ds = DataSourceBuilder.create()
				.build(DataSourceConfigProperties.builder("basic").withPropertySource("test_build.properties").build());
		assertNotNull(ds);

		assertEquals(DefaultBasicDataSource.class, ds.getClass());
		assertEquals("jdbc:h2:mem:testdb0", ((DefaultBasicDataSource) ds).getUrl());
		assertEquals("sa", ((DefaultBasicDataSource) ds).getUsername());

		try (Connection c = ds.getConnection()) {
			assertNotNull(c);
		}
	}

	@Test
	public void testCL() throws SQLException {
		DataSource ds = DataSourceBuilder.create(ClassUtils.getDefaultClassLoader())
				.build(DataSourceConfigProperties.builder("basic").withPropertySource("test_build.properties").build());
		assertNotNull(ds);

		assertEquals(DefaultBasicDataSource.class, ds.getClass());
		assertEquals("jdbc:h2:mem:testdb0", ((DefaultBasicDataSource) ds).getUrl());
		assertEquals("sa", ((DefaultBasicDataSource) ds).getUsername());

		try (Connection c = ds.getConnection()) {
			assertNotNull(c);
		}
	}

	@Test
	public void testTomcatType() throws SQLException {
		DataSource ds = DataSourceBuilder.create().build(
				DataSourceConfigProperties.builder("pooling1").withPropertySource("test_build.properties").build());
		assertNotNull(ds);

		assertEquals(org.apache.tomcat.jdbc.pool.DataSource.class, ds.getClass());
		assertEquals("jdbc:h2:mem:testdb1", ((org.apache.tomcat.jdbc.pool.DataSource) ds).getUrl());
		assertEquals("sa", ((org.apache.tomcat.jdbc.pool.DataSource) ds).getUsername());

		assertEquals(3, ((org.apache.tomcat.jdbc.pool.DataSource) ds).getInitialSize());
		assertEquals(5, ((org.apache.tomcat.jdbc.pool.DataSource) ds).getMaxActive());

		assertEquals(33120L, ((org.apache.tomcat.jdbc.pool.DataSource) ds).getMaxAge());

		assertEquals(DatabasePlatform.H2.getDriverClassName(),
				((org.apache.tomcat.jdbc.pool.DataSource) ds).getDriverClassName());

		assertEquals(DatabasePlatform.H2.getValidationQuery(),
				((org.apache.tomcat.jdbc.pool.DataSource) ds).getValidationQuery());

		try (Connection c = ds.getConnection()) {
			assertNotNull(c);
		}
	}

	@Test
	public void testHikariType() throws SQLException {
		DataSource ds = DataSourceBuilder.create().build(
				DataSourceConfigProperties.builder("pooling2").withPropertySource("test_build.properties").build());
		assertNotNull(ds);

		assertEquals(HikariDataSource.class, ds.getClass());
		HikariConfigMXBean cfg = ((HikariDataSource) ds).getHikariConfigMXBean();

		// assertEquals("jdbc:h2:mem:testdb2", ((HikariDataSource) ds).getJdbcUrl());
		// assertEquals("sa", ((HikariDataSource) ds).getUsername());

		assertEquals(2, cfg.getMinimumIdle());
		assertEquals(7, cfg.getMaximumPoolSize());

		assertEquals(1234L, cfg.getConnectionTimeout());

		// assertEquals(DatabasePlatform.H2.getDriverClassName(), ((HikariDataSource) ds).getDriverClassName());

		// assertEquals(DatabasePlatform.H2.getValidationQuery(), ((HikariDataSource) ds).getConnectionTestQuery());

		try (Connection c = ds.getConnection()) {
			assertNotNull(c);
		}
	}

	@Test
	public void testDBCPType() throws SQLException {
		DataSource ds = DataSourceBuilder.create().build(
				DataSourceConfigProperties.builder("pooling3").withPropertySource("test_build.properties").build());
		assertNotNull(ds);

		assertEquals(org.apache.commons.dbcp2.BasicDataSource.class, ds.getClass());
		assertEquals("jdbc:h2:mem:testdb3", ((org.apache.commons.dbcp2.BasicDataSource) ds).getUrl());
		assertEquals("sa", ((org.apache.commons.dbcp2.BasicDataSource) ds).getUsername());

		assertEquals(8, ((org.apache.commons.dbcp2.BasicDataSource) ds).getInitialSize());
		assertEquals(12, ((org.apache.commons.dbcp2.BasicDataSource) ds).getMaxTotal());

		assertEquals(1000L, ((org.apache.commons.dbcp2.BasicDataSource) ds).getMaxWaitMillis());

		assertEquals(DatabasePlatform.H2.getDriverClassName(),
				((org.apache.commons.dbcp2.BasicDataSource) ds).getDriverClassName());

		assertEquals(DatabasePlatform.H2.getValidationQuery(),
				((org.apache.commons.dbcp2.BasicDataSource) ds).getValidationQuery());

		try (Connection c = ds.getConnection()) {
			assertNotNull(c);
		}
	}

}
