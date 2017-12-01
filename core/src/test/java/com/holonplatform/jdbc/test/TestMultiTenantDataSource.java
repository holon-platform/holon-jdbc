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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.Test;

import com.holonplatform.core.Context;
import com.holonplatform.core.tenancy.TenantResolver;
import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.MultiTenantDataSource;
import com.holonplatform.jdbc.TenantDataSourceProvider;
import com.holonplatform.jdbc.internal.DefaultBasicDataSource;

public class TestMultiTenantDataSource {

	class TenantTestDataSourceProvider implements TenantDataSourceProvider {

		@Override
		public DataSource getDataSource(String tenantId) {

			Properties properties = new Properties();
			properties.put("holon.datasource.username", "sa");
			if ("T1".equals(tenantId)) {
				properties.put("holon.datasource.url",
						"jdbc:h2:mem:testdbt1;INIT=RUNSCRIPT FROM 'classpath:scripts/db1.sql'");
			} else if ("T2".equals(tenantId)) {
				properties.put("holon.datasource.url",
						"jdbc:h2:mem:testdbt1;INIT=RUNSCRIPT FROM 'classpath:scripts/db2.sql'");
			} else {
				properties.put("holon.datasource.url",
						"jdbc:h2:mem:testdbt1;INIT=RUNSCRIPT FROM 'classpath:scripts/db3.sql'");
			}

			return DataSourceBuilder.create()
					.build(DataSourceConfigProperties.builder().withPropertySource(properties).build());
		}

	}

	@SuppressWarnings("resource")
	@Test
	public void testMultiTenantDataSource() throws SQLException {

		MultiTenantDataSource ds = MultiTenantDataSource.builder().provider(new TenantTestDataSourceProvider()).build();

		Context.get().threadScope()
				.map(s -> s.put(TenantResolver.CONTEXT_KEY, TenantResolver.staticTenantResolver("T1")));

		try (Connection c = ds.getConnection()) {
			assertNotNull(c);

			try (ResultSet rs = c.createStatement().executeQuery("select str from test1 where key=1")) {
				rs.next();
				assertEquals("One", rs.getString(1));
			}
		}

		Context.get().threadScope().map(s -> s.remove(TenantResolver.CONTEXT_KEY));

		// using execute

		TenantResolver.execute("T2", () -> {

			try (Connection c = ds.getConnection()) {
				assertNotNull(c);

				try (ResultSet rs = c.createStatement().executeQuery("select str from test2 where key=2")) {
					rs.next();
					assertEquals("Two", rs.getString(1));
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}

		});

		// cached

		TenantResolver.execute("T2", () -> {

			try (Connection c = ds.getConnection()) {
				assertNotNull(c);

				try (ResultSet rs = c.createStatement().executeQuery("select str from test2 where key=2")) {
					rs.next();
					assertEquals("Two", rs.getString(1));
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}

		});

		// no tenant

		TenantResolver.execute(null, () -> {

			try (Connection c = ds.getConnection()) {
				assertNotNull(c);

				try (ResultSet rs = c.createStatement().executeQuery("select str from test3 where key=3")) {
					rs.next();
					assertEquals("Three", rs.getString(1));
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}

		});

	}

	@Test
	public void testContext() {

		final TenantDataSourceProvider tdsp = tenantId -> {
			if ("test".equals(tenantId)) {
				return new DefaultBasicDataSource();
			}
			return null;
		};

		DataSource ds = Context.get().executeThreadBound(TenantDataSourceProvider.CONTEXT_KEY, tdsp, () -> {
			return TenantDataSourceProvider.getCurrent().orElseThrow(() -> new IllegalStateException("Missing Realm"))
					.getDataSource("test");
		});

		assertNotNull(ds);

	}

}
