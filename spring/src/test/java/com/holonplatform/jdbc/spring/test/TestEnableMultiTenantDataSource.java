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
package com.holonplatform.jdbc.spring.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.holonplatform.core.tenancy.TenantResolver;
import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.MultiTenantDataSource;
import com.holonplatform.jdbc.TenantDataSourceProvider;
import com.holonplatform.spring.EnableBeanContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestEnableMultiTenantDataSource.Config.class)
public class TestEnableMultiTenantDataSource {

	@Configuration
	@EnableBeanContext
	// @EnableDataSource(dataContextId = "mt1") // TODO EnableMultiTenantDataSource
	protected static class Config {

		@Bean
		public TenantResolver tenantResolver() {
			return TenantResolver.staticTenantResolver("T1");
		}

		@Bean
		public TenantDataSourceProvider tenantDataSourceProvider() {
			Properties properties = new Properties();
			properties.put("holon.datasource.username", "sa");
			properties.put("holon.datasource.url",
					"jdbc:h2:mem:testdbt1;INIT=RUNSCRIPT FROM 'classpath:test-scripts/db1.sql'");
			return (tenantId) -> DataSourceBuilder.create()
					.build(DataSourceConfigProperties.builder().withPropertySource(properties).build());
		}

		@Bean
		public DataSource dataSource() {
			return MultiTenantDataSource.builder().resolver(tenantResolver()).provider(tenantDataSourceProvider())
					.build();
		}

	}

	@Autowired
	private DataSource dataSource;

	@Test
	public void testDataSource() throws SQLException {

		assertNotNull(dataSource);

		try (Connection c = dataSource.getConnection()) {
			assertNotNull(c);

			try (ResultSet rs = c.createStatement().executeQuery("select str from test1 where key=1")) {
				rs.next();
				assertEquals("One", rs.getString(1));
			}
		}

	}

}
