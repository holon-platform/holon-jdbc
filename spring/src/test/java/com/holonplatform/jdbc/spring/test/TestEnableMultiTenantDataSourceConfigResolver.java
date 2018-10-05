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
package com.holonplatform.jdbc.spring.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.holonplatform.core.tenancy.TenantResolver;
import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.MultiTenantDataSource;
import com.holonplatform.jdbc.TenantDataSourceProvider;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestEnableMultiTenantDataSourceConfigResolver.Config.class)
public class TestEnableMultiTenantDataSourceConfigResolver {

	private static final ThreadLocal<String> TENANT = new ThreadLocal<>();

	@Configuration
	// @EnableDataSource(dataContextId = "mt3") // TODO EnableMultiTenantDataSource
	protected static class Config {

		@Bean("test_tenant_resolver")
		public TenantResolver customResolver() {
			return new TenantResolver() {

				@Override
				public Optional<String> getTenantId() {
					return Optional.ofNullable(TENANT.get());
				}

			};
		}

		@Bean("test_tenant_dsprovider")
		public TenantDataSourceProvider customProvider() {
			return new TenantDataSourceProvider() {

				@Override
				public DataSource getDataSource(String tenantId) {
					Properties properties = new Properties();
					properties.put("holon.datasource.username", "sa");
					properties.put("holon.datasource.url",
							"jdbc:h2:mem:testdbt1;INIT=RUNSCRIPT FROM 'classpath:test-scripts/db1.sql'");
					return DataSourceBuilder.create()
							.build(DataSourceConfigProperties.builder().withPropertySource(properties).build());
				}

			};
		}

		@Bean
		public DataSource dataSource() {
			return MultiTenantDataSource.builder().resolver(customResolver()).provider(customProvider()).build();
		}

	}

	@Autowired
	private DataSource dataSource;

	@Test
	public void testDataSource() throws SQLException {

		assertNotNull(dataSource);

		TENANT.set("T1");

		try (Connection c = dataSource.getConnection()) {
			assertNotNull(c);

			try (ResultSet rs = c.createStatement().executeQuery("select str from test1 where key=1")) {
				rs.next();
				assertEquals("One", rs.getString(1));
			}
		}

		TENANT.remove();

	}

}
