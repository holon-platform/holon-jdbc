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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;

import com.holonplatform.jdbc.BasicDataSource;
import com.holonplatform.jdbc.DatabasePlatform;

public class TestBasicDataSource {

	@SuppressWarnings("resource")
	@Test
	public void testBuilder() throws SQLException {

		DataSource dataSource = BasicDataSource.builder().url("jdbc:h2:mem:testdb_b1").username("sa")
				.driverClassName("org.h2.Driver").build();

		try (Connection connection = dataSource.getConnection()) {
			assertNotNull(connection);
			try (ResultSet rs = connection.createStatement().executeQuery("SELECT 1")) {
				assertTrue(rs.next());
			}
		}

		dataSource = BasicDataSource.builder().url("jdbc:h2:mem:testdb_b2").username("sa").database(DatabasePlatform.H2)
				.build();

		try (Connection connection = dataSource.getConnection()) {
			assertNotNull(connection);
			try (ResultSet rs = connection.createStatement().executeQuery("SELECT 1")) {
				assertTrue(rs.next());
			}
		}

	}

}
