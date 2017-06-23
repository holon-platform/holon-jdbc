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
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.holonplatform.jdbc.spring.EnableDataSource;
import com.zaxxer.hikari.HikariDataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestEnableDataSourceDataContext.Config.class)
public class TestEnableDataSourceDataContext {

	@Configuration
	@PropertySource("test.properties")
	@EnableDataSource(dataContextId = "test")
	protected static class Config {
	}

	@Autowired
	@Qualifier("test")
	private DataSource dataSource;

	@Test
	public void testDataSource() throws SQLException {

		assertNotNull(dataSource);

		assertTrue(dataSource instanceof HikariDataSource);

		try (Connection c = dataSource.getConnection()) {
			assertNotNull(c);

			try (ResultSet rs = c.createStatement().executeQuery("select str from testx where key=1")) {
				rs.next();
				assertEquals("One", rs.getString(1));
			}
		}

	}

}
