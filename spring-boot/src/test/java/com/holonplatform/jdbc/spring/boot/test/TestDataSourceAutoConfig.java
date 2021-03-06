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
package com.holonplatform.jdbc.spring.boot.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test1")
public class TestDataSourceAutoConfig {

	@Configuration
	@EnableAutoConfiguration
	protected static class Config {

	}

	@Autowired
	@Qualifier("one")
	private DataSource dataSource1;

	@Autowired
	@Qualifier("two")
	private DataSource dataSource2;

	@Transactional("one")
	@Test
	public void testDataSource1() throws SQLException {
		assertNotNull(dataSource1);

		try (Connection c = dataSource1.getConnection()) {
			assertNotNull(c);

			try (ResultSet rs = c.createStatement().executeQuery("select count(*) from test1")) {
				assertTrue(rs.next());
			}
		}
	}

	@Transactional("two")
	@Test
	public void testDataSource2() throws SQLException {
		assertNotNull(dataSource2);

		try (Connection c = dataSource2.getConnection()) {
			assertNotNull(c);

			try (ResultSet rs = c.createStatement().executeQuery("select count(*) from test2")) {
				assertTrue(rs.next());
			}
		}
	}

}
